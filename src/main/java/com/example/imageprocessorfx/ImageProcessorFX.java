package com.example.imageprocessorfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageProcessorFX extends Application {

    private VBox layout1, layout2;
    private HBox layout;
    private TextField inputFolderField;
    private TextField outputFolderField;
    private ComboBox<String> modelComboBox;
    private CheckBox subfoldersCheckBox;
    private CheckBox convertToWebpCheckBox;
    private CheckBox upsaceleCheckBox;
    private CheckBox showPreviewCheckBox;
    private CheckBox deleteSourceFileCheckBox;
    private ProgressBar progressBar;
    private Process conversionProcess;
    private Button processButton;
    private Button browseInputButton;
    private Button browseOutputButton;
    private Button closeButton;
    private Boolean flag = true;
    private ProgressIndicator progressIndicator;
    private ImageView imageView;
    private Text textCurrentFolder;
    private Text textCurrentFile;

    private Stage stage;

    private int verticalsize = 270;
    private int horizontalsize = 0;

    private boolean deleteSourceFile = false;

    ConfirmDialog dialog = new ConfirmDialog();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;
        primaryStage.setTitle("Improve Images Quality And Optimize Size With AI");

        // Main layout
        layout = new HBox(10);
        layout.setPadding(new Insets(5));


        layout1 = new VBox(10);
        layout1.setPadding(new Insets(5));

        layout2 = new VBox(10);
        layout2.setPadding(new Insets(5));

        // Field for input folder
        HBox inputBox = new HBox(15);
        Label inputLabel = new Label("Input Folder:");
        inputFolderField = new TextField();
        inputFolderField.setPrefWidth(300);
        //inputFolderField.setEditable(false);
        browseInputButton = new Button("Browse");
        browseInputButton.setOnAction(e -> selectFolder(primaryStage, inputFolderField));

        imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        inputBox.getChildren().addAll(inputLabel, inputFolderField, browseInputButton);
        // Output folder field
        HBox outputBox = new HBox(10);
        Label outputLabel = new Label("Output Folder:");
        outputFolderField = new TextField();
        outputFolderField.setPrefWidth(300);
        //outputFolderField.setEditable(false);
        browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> selectFolder(primaryStage, outputFolderField));
        outputBox.getChildren().addAll(outputLabel, outputFolderField, browseOutputButton);

        // Dropdown menu to select model
        HBox modelBox = new HBox(10);
        Label modelLabel = new Label("Model:");
        modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll(
                "realesrgan-x4plus",
                "realesrnet-x4plus",
                "realesrgan-x4plus-anime",
                "realesr-animevideov3",
                "realesr-animevideov3-x2",
                "realesr-animevideov3-x4"
        );
        modelComboBox.getSelectionModel().selectFirst();
        modelBox.getChildren().addAll(modelLabel, modelComboBox);

        // Checkbox for subfolders
        HBox checkBoxes1 = new HBox(10);
        HBox checkBoxes2 = new HBox(10);
        deleteSourceFileCheckBox = new CheckBox("Delete Source File");
        subfoldersCheckBox = new CheckBox("Process Subfolders");
        upsaceleCheckBox = new CheckBox("Upscale 4x");
        upsaceleCheckBox.setSelected(true);

        convertToWebpCheckBox = new CheckBox("Convert to Webp");
        convertToWebpCheckBox.setSelected(true);
        showPreviewCheckBox = new CheckBox("Preview");
        showPreviewCheckBox.setSelected(false);
        showPreviewCheckBox.setOnAction( e -> {
            if(showPreviewCheckBox.isSelected()) {
                layout.getChildren().add(layout2);
                primaryStage.setWidth(620);
            } else {
                layout.getChildren().remove(layout2);
                primaryStage.setWidth(500);
            }

        });
        deleteSourceFileCheckBox.setSelected(false);
        deleteSourceFileCheckBox.setOnAction( e -> {
            if(deleteSourceFileCheckBox.isSelected()) {
                dialog.showConfirmationDialog(
                        "Confirm Delete Source File",
                        "Are you sure you want to delete the source file?",
                        confirmed -> {
                            if (confirmed) {
                                deleteSourceFile = true;
                            } else {
                                deleteSourceFileCheckBox.setSelected(false);
                                deleteSourceFile = false;
                            }
                        }
                );
            }
        });
        checkBoxes1.getChildren().addAll(deleteSourceFileCheckBox, subfoldersCheckBox);
        checkBoxes2.getChildren().addAll(upsaceleCheckBox, convertToWebpCheckBox, showPreviewCheckBox);

        HBox progressBox = new HBox(10);
        // PROGRESS BAR
        progressBar = new ProgressBar(50);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(30);

        //Progress indicator (spinner)
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefWidth(30);
        progressIndicator.setPrefHeight(30);
        progressIndicator.setVisible(false); // Oculto inicialmente
        progressBox.getChildren().addAll(progressBar, progressIndicator);

        HBox bottomButtons = new HBox(10);
        // Button to start processing
        processButton = new Button("Go");
        processButton.setOnAction(e -> {
            flag = true;
            processFiles();
        });

        // Close button
        closeButton = new Button("Exit");
        closeButton.setOnAction(e -> {
            if (conversionProcess != null && conversionProcess.isAlive()) {
                this.flag = false;
                conversionProcess.destroy(); // Termina el proceso si está activo
            }
            if (closeButton.getText().equals("Exit")) {
                System.exit(0); // Cierra la aplicación
            } else {
                System.out.println("Cancel pressed");
            }
        });

        bottomButtons.getChildren().addAll(processButton, closeButton);

        textCurrentFolder = new Text("Current Folder:");
        textCurrentFile = new Text("Current File:");

        // Add everything to the layout
        layout1.getChildren().addAll(inputBox, outputBox, modelBox, checkBoxes1, checkBoxes2, progressBox, textCurrentFolder, textCurrentFile, bottomButtons);
        layout2.getChildren().addAll(imageView);

        if (showPreviewCheckBox.isSelected())
            layout.getChildren().addAll(layout1, layout2); else
                layout.getChildren().addAll(layout1);

        // Configure and display the window

        if(showPreviewCheckBox.isSelected()){
            horizontalsize=600;
        } else {
            horizontalsize=490;
        }

        Scene scene = new Scene(layout, horizontalsize, verticalsize);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            if (conversionProcess != null && conversionProcess.isAlive()) {
                conversionProcess.destroy(); // Stop the process
            }
            System.exit(0);
        });
    }

    private void selectFolder(Stage stage, TextField targetField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            targetField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void processFiles() {
        String inputFolder = inputFolderField.getText();
        String outputFolder = outputFolderField.getText();
        String model = modelComboBox.getValue();
        boolean processSubfolders = subfoldersCheckBox.isSelected();

        if (inputFolder.isEmpty() || outputFolder.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select both input and output folders.");
            return;
        }

        File inputDir = new File(inputFolder);
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Input folder is not valid.");
            return;
        }

        progressBar.setProgress(0);
        progressIndicator.setVisible(true); // Show the spinner
        processButton.setDisable(true);
        inputFolderField.setDisable(true);
        outputFolderField.setDisable(true);
        browseInputButton.setDisable(true);
        browseOutputButton.setDisable(true);
        deleteSourceFileCheckBox.setDisable(true);
        upsaceleCheckBox.setDisable(true);
        convertToWebpCheckBox.setDisable(true);
        subfoldersCheckBox.setDisable(true);
        modelComboBox.setDisable(true);
        closeButton.setText("Cancel");


        // Processing in a separate thread
        new Thread(() -> {
            try {
                processFolder(inputDir, new File(outputFolder), model, processSubfolders);
                showAlert(Alert.AlertType.INFORMATION, "Process finished", "Processing finished!");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while processing files.");
            }
            Platform.runLater(() -> {
                progressBar.setProgress(1);
                progressIndicator.setVisible(false); // Hide the spinner
                processButton.setDisable(false);
                inputFolderField.setDisable(false);
                outputFolderField.setDisable(false);
                browseInputButton.setDisable(false);
                browseOutputButton.setDisable(false);
                deleteSourceFileCheckBox.setDisable(false);
                deleteSourceFileCheckBox.setSelected(false);
                upsaceleCheckBox.setDisable(false);
                convertToWebpCheckBox.setDisable(false);
                subfoldersCheckBox.setDisable(false);
                modelComboBox.setDisable(false);
                closeButton.setText("Exit");
                textCurrentFolder.setText("Current Folder:");
                textCurrentFile.setText("Current File:");
                updateProgress(0);
                imageView.setImage(null);
            });
        }).start();
    }

    private void processFolder(File inputDir, File outputDir, String model, boolean processSubfolders) throws IOException, InterruptedException {
        String currentDir = System.getProperty("user.dir");

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch");
        File realesrganExecutable;
        File cwebpExecutable;
        boolean convertToWebp = convertToWebpCheckBox.isSelected();
        boolean upscalePicture = upsaceleCheckBox.isSelected();

        // Full paths to executables for each OS
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
            conversionProcess = null;
            return;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles();
        if (files == null) return;
        int totalFiles = files.length;
        int processedFiles = 0;

        textCurrentFolder.setText("Current Folder: " + inputDir.getAbsolutePath());


        for (File file : files) {
            if (!this.flag) { break; }

            if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG")
                    || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png") || file.getName().endsWith(".webp"))) {

                File outputFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_improved.png"));
                File compressedFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_final.webp"));
                textCurrentFile.setText("Current File: " + file.getName() + " Progress/Total Files: " + (processedFiles+1) + "/" + totalFiles);

                try {
                    if (showPreviewCheckBox.isSelected()) {
                        imageView.setImage(null);
                        FileInputStream input = new FileInputStream(file.getAbsolutePath());
                        Image image = new Image(input);
                        imageView.setImage(image);
                        input.close();
                    }

                    if(upscalePicture) {
                        // Configure and run the process
                        ProcessBuilder realesrganProcessBuilder = new ProcessBuilder(
                                realesrganExecutable.getAbsolutePath(),
                                "-i", file.getAbsolutePath(),
                                "-o", outputFile.getAbsolutePath(),
                                "-n", model,
                                "-f", "png"
                        );
                        // Start the process and save the reference
                        conversionProcess = realesrganProcessBuilder.start();
                        // Wait for the process to finish (optional if necessary)
                        conversionProcess.waitFor();
                    }

                    if (convertToWebp) {
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
                    if ((!upscalePicture && convertToWebp) || (upscalePicture && convertToWebp)) {
                        // Delete temporary file
                        outputFile.delete();
                    }
                    if ((deleteSourceFile)) {
                        // Delete source file
                        file.delete();
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    // Clear the reference to the process once it finishes
                    conversionProcess = null;
                }
                // Update progress bar
                processedFiles++;
                double progress = (double) processedFiles / totalFiles;
                updateProgress(progress);
            } else if (processSubfolders && file.isDirectory()) {
                processFolder(file, new File(outputDir, file.getName()), model, true);
            }
        }
    }

    private void updateProgress(double progress) {
        javafx.application.Platform.runLater(() -> progressBar.setProgress(progress));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
