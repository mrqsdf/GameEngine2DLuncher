package fr.mrqsdf.gameengine2dluncher;


import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GradleInstaller {

    private static final String GRADLE_VERSION = "8.9";
    private static final String GRADLE_URL = "https://services.gradle.org/distributions/gradle-" + GRADLE_VERSION + "-bin.zip";
    private static final String GRADLE_HOME = ".gradle";

    public static void downloadAndInstallGradle(File installDir) throws IOException {
        File gradleZip = new File(installDir, "gradle.zip");

        // Télécharger Gradle
        try (InputStream in = new URL(GRADLE_URL).openStream();
             FileOutputStream out = new FileOutputStream(gradleZip)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // Extraire le fichier ZIP
        try (FileInputStream fis = new FileInputStream(gradleZip);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(installDir, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    new File(entryFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        // Nettoyer le fichier ZIP
        if (!gradleZip.delete()) {
            System.err.println("Failed to delete the temporary Gradle ZIP file.");
        }
    }

}
