package com.example.imageprocessorfx;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileManager {

    public static void deleteFile(File file) throws IOException {
        if (file.delete()) {
            System.out.println(file.getName() + " deleted successfully");
        } else {
            throw new IOException("Failed to delete the file: " + file.getName());
        }
    }

    public static void copyFile(File sourceFile, File targetDirectory) throws IOException {
        if (!sourceFile.exists()) {
            throw new IOException("The origin file does not exist: " + sourceFile.getName());
        }

        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new IOException("The destiny folder could not be created: " + targetDirectory.getAbsolutePath());
            }
        }

        File targetFile = new File(targetDirectory, sourceFile.getName());

        try {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println(sourceFile.getName() + " copiado exitosamente a " + targetDirectory.getPath());
        } catch (IOException e) {
            throw new IOException("Error al copiar el archivo " + sourceFile.getName() + ": " + e.getMessage());
        }
    }

    public static void moveFile(File sourceFile, File targetDirectory) throws IOException {
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourceFile.getName());
        }

        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new IOException("Failed to create target directory: " + targetDirectory.getAbsolutePath());
            }
        }

        File targetFile = new File(targetDirectory, sourceFile.getName());

        try {
            Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println(sourceFile.getName() + " moved successfully to " + targetDirectory.getPath());
        } catch (IOException e) {
            throw new IOException("Error moving file " + sourceFile.getName() + ": " + e.getMessage());
        }
    }

    public static void openFolder(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("The folder does not exist or is invalid: " + folderPath);
            return;
        }

        // Use Desktop to open the folder
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(folder);
            } catch (IOException e) {
                System.out.println("Error when trying to open the folder: " + e.getMessage());
            }
        } else {
            System.out.println("Opening folders is not supported on this system.");
        }

    }

}
