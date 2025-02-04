package com.example.imageprocessorfx;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileManager {

    public static void deleteFile(File file) throws IOException {
        if (file.delete()) {
            System.out.println(file.getName() + " deleted successfully");
        } else {
            throw new IOException("Failed to delete the file: " + file.getName());
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
