package fr.mrqsdf.gameengine2dluncher.res;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CopyAssets {

    public static void copyDirectory(File sourceDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs(); // Crée le répertoire de destination s'il n'existe pas
        }

        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                // Copier le dossier récursivement
                copyDirectory(file, new File(destDir, file.getName()));
            } else {
                // Copier le fichier
                copyFile(file, new File(destDir, file.getName()));
            }
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Copié: " + sourceFile.getAbsolutePath() + " -> " + destFile.getAbsolutePath());
    }

}
