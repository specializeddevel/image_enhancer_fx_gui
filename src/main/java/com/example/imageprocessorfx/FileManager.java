package com.example.imageprocessorfx;

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

}
