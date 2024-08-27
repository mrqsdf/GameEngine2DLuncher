package fr.mrqsdf.gameengine2dluncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.mrqsdf.gameengine2dluncher.res.CopyAssets;
import fr.mrqsdf.gameengine2dluncher.res.ProjectData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ProjectManager {

    public static void createProject(String projectName, String groupId, File saveLocation) {
        if (saveLocation == null || !saveLocation.exists()) {
            JOptionPane.showMessageDialog(null, "Impossible de créer un projet. L'emplacement de sauvegarde n'a pas été configuré ou n'existe pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File projectDir = new File(saveLocation, projectName);
        if (!projectDir.exists()) {
            projectDir.mkdirs();
            File gradleDir = new File(projectDir, ".gradle");
            gradleDir.mkdirs();

            try {
                // Télécharger et installer Gradle
                GradleInstaller.downloadAndInstallGradle(gradleDir);

                // Crée la structure de répertoires pour le code source
                File srcDir = new File(projectDir, "src/main/java/" + groupId.replace('.', '/') + "/" + projectName.toLowerCase());                srcDir.mkdirs();

                // Crée la classe Main dans src/main/java/com/exemple/projectname
                try {
                    createMainClass(srcDir, projectName, groupId);
                    // Crée un fichier build.gradle basique
                    createGradleBuildFile(projectDir, projectName, groupId);

                    createGradleWrapper(projectDir);

                    copyAssets(projectDir);

                    saveProjectInfo(projectName, groupId, saveLocation);

                    JOptionPane.showMessageDialog(null, "Le projet " + projectName + " a été créé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erreur lors de la création du projet : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erreur lors de l'installation de Gradle : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void copyAssets(File projectDir) {
        try {
            CopyAssets.copyDirectory(new File("assets"), new File(projectDir, "assets"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createGradleWrapper(File projectDir) {
        try {
            // Spécifiez le chemin de gradle.bat
            String gradleBatPath = projectDir.getAbsolutePath() + "\\.gradle\\gradle-8.9\\bin\\gradle.bat";

            // Commande pour créer le wrapper Gradle dans le répertoire projectDir
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", gradleBatPath, "wrapper");

            // Définir le répertoire de travail pour le processus
            processBuilder.directory(projectDir);

            // Lancer le processus
            Process process = processBuilder.start();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }

            // Lire la sortie du processus
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Gradle wrapper créé avec succès dans : " + projectDir.getAbsolutePath());
            } else {
                System.err.println("Erreur lors de la création du Gradle wrapper, code de sortie : " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createMainClass(File srcDir, String projectName, String groupId) {
        String className = projectName + "Main";
        File mainClassFile = new File(srcDir, className + ".java");

        String packageName = groupId + "." + projectName.toLowerCase();
        String code = "package " + packageName + ";\n\n" +
                "import fr.mrqsdf.engine2d.MainEngine;\n\n" +
                "public class " + className + " {\n\n" +
                "    public static void main(String[] args) {\n" +
                "        MainEngine.main(args);\n" +
                "    }\n" +
                "}";

        try (PrintWriter writer = new PrintWriter(new FileWriter(mainClassFile))) {
            writer.println(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createGradleBuildFile(File projectDir, String projectName, String groupId) {
        File buildFile = new File(projectDir, "build.gradle");
        File settingsFile = new File(projectDir, "settings.gradle");

        String buildScript = "plugins {\n" +
                "    id 'java'\n" +
                "    id 'application'\n" +
                "}\n\n" +
                "group '"+groupId+"'//groupe Id\n" +
                "version '1.0-SNAPSHOT'//Version\n\n" +
                "mainClassName = '" + groupId + "." + projectName.toLowerCase() + "." + projectName + "Main'//MainClassPath\n" +
                "repositories {\n" +
                "    mavenCentral()\n" +
                "    maven{\n" +
                "        allowInsecureProtocol = true\n" +
                "        url = \"http://vps.mrqsdf.eu:8081/repository/maven-releases/\"\n" +
                "        credentials {\n" +
                "            username = project.findProperty(\"nexusPersoGetUsername\") ?: \"\"\n" +
                "            password = project.findProperty(\"nexusPersoGetPassword\") ?: \"\"\n" +
                "        }\n" +
                "    }\n" +
                "}\n\n" +
                "dependencies {\n" +
                "        implementation 'org.springframework.boot:spring-boot-starter:3.1.2' \n" +
                "    testImplementation 'junit:junit:4.12'\n" +
                "    implementation 'fr.mrqsdf:GameEngine2D:1.0'\n" +
                "}\n\n" +
                "jar {\n" +
                "    manifest {\n" +
                "        attributes(\n" +
                "            'Main-Class': '" + groupId + "." + projectName.toLowerCase() + "." + projectName + "Main'\n" +
                "        )\n" +
                "    }\n" +
                "}\n" +
                "java {\n" +
                "    toolchain {\n" +
                "        languageVersion = JavaLanguageVersion.of(21)\n" +
                "    }\n" +
                "}\n" +
                "sourceCompatibility = JavaVersion.VERSION_21\n" +
                "targetCompatibility = JavaVersion.VERSION_21\n";

        String settingsScript = "rootProject.name = '" + projectName + "'";

        try (PrintWriter writer = new PrintWriter(new FileWriter(buildFile)); PrintWriter settingsWriter = new PrintWriter(new FileWriter(settingsFile)) ) {
            writer.println(buildScript);
            settingsWriter.println(settingsScript);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveProjectInfo(String projectName, String groupId, File saveLocation) {
        ProjectData projectData = new ProjectData(projectName, groupId, saveLocation.getAbsolutePath());
        MainApp.mainApp.luncherData.addProject(projectData);
        MainApp.mainApp.listModel.addElement(projectData);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("saveFile.json")) {
            gson.toJson(MainApp.mainApp.luncherData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openProject(String projectName, String projectPath, String groupeId) {
        File projectDir = new File(projectPath, projectName);
        if (projectDir.exists()) {
            File gradleFile = new File(projectDir, "build.gradle");
            if (!gradleFile.exists()) {
                JOptionPane.showMessageDialog(null, "Le fichier build.gradle n'existe pas : " + gradleFile.getAbsolutePath(), "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            executeGradleBuild(projectDir);
        } else {
            JOptionPane.showMessageDialog(null, "Le projet sélectionné n'existe pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void executeGradleBuild(File projectDir) {
        System.out.println("Hello World");
        try {
            // Spécifiez la commande pour exécuter le programme A
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", "build", "run");

            // Définissez le répertoire de travail sur le répertoire du programme A
            processBuilder.directory(new java.io.File(projectDir.getAbsolutePath()));

            // Lancer le processus
            Process process = processBuilder.start();

            // Lire et afficher la sortie du programme A
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Lire et afficher les erreurs du programme A
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }

            // Attendre la fin du processus
            int exitCode = process.waitFor();
            System.out.println("Le programme A s'est terminé avec le code de sortie : " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
