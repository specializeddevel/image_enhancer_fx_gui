package com.example.imageprocessorfx;

import java.io.File;
import java.io.IOException;

public class ImageProcessor {

    private File realesrganExecutable;
    private File cwebpExecutable;

    public ImageProcessor(String currentDir) {
        // Full paths to executables for each OS
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            realesrganExecutable = new File(currentDir, "realesrgan-ncnn-vulkan.exe");
            cwebpExecutable = new File(currentDir, "cwebp.exe");
        } else if (osName.contains("mac")) {
            realesrganExecutable = new File(currentDir, "realesrgan-ncnn-vulkan-mac");
            cwebpExecutable = new File(currentDir, "cwebp-mac");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            realesrganExecutable = new File(currentDir, "realesrgan-ncnn-vulkan");
            cwebpExecutable = new File(currentDir, "cwebp");
        } else {
            System.out.println("Unknown operating system");
            realesrganExecutable = null;
            cwebpExecutable = null;
        }
    }

    public void upscaleImage(File inputFile, File outputFile, String model, Process conversionProcess) throws IOException, InterruptedException {
        // Configure and run the process
        ProcessBuilder realesrganProcessBuilder = new ProcessBuilder(
                realesrganExecutable.getAbsolutePath(),
                "-i", inputFile.getAbsolutePath(),
                "-o", outputFile.getAbsolutePath(),
                "-n", model,
                "-f", "png"
        );
        // Start the process and save the reference
        conversionProcess = realesrganProcessBuilder.start();
        // Wait for the process to finish (optional if necessary)
        conversionProcess.waitFor();
    }

    public void convertToWebP(File file, File outputFile, File compressedFile, Process conversionProcess, boolean upscalePicture) throws IOException, IOException, InterruptedException {
        ProcessBuilder cwebpProcessBuilder;
        if (upscalePicture) {
            cwebpProcessBuilder = new ProcessBuilder(
                    cwebpExecutable.getAbsolutePath(),
                    "-q", "80",
                    outputFile.getAbsolutePath(),
                    "-o", compressedFile.getAbsolutePath()
            );
        } else {
            cwebpProcessBuilder = new ProcessBuilder(
                    cwebpExecutable.getAbsolutePath(),
                    "-q", "80",
                    file.getAbsolutePath(),
                    "-o", compressedFile.getAbsolutePath()
            );
        }
        conversionProcess = cwebpProcessBuilder.start();
        conversionProcess.waitFor();
    }

}
