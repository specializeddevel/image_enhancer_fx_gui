package com.example.imageprocessorfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {

    private final AtomicReference<Process> conversionProcessRef = new AtomicReference<>();
    private final AtomicBoolean isPaused = new AtomicBoolean(false); // Control if the process is paused

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
    private CheckBox includeWebpFilesCheckBox;

    private Button processButton;
    private Button browseInputButton;
    private Button browseOutputButton;
    private Button closeButton;
    private Button showSourceFolderButton;
    private Button showDestinyFolderButton;
    private Button pauseProcessButton;
    private Boolean flag = true;
    private ImageView imageView;
    private Text textCurrentFolder;
    private Text textCurrentFile;



    private boolean deleteSourceFile = false;
    private boolean includeWebpFiles = false;

    private int totalFoldersProcessed = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        createGUI(primaryStage);
    }

    private void createGUI(Stage primaryStage) {

        int horizontalSize;
        int verticalSize = 265;

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

        HBox checkBoxes1 = new HBox(10);
        HBox checkBoxes2 = new HBox(10);
        deleteSourceFileCheckBox = new CheckBox("Delete Source Files");
        subfoldersCheckBox = new CheckBox("Process Subfolders");
        upsaceleCheckBox = new CheckBox("Apply 4x upscale");
        upsaceleCheckBox.setSelected(true);
        includeWebpFilesCheckBox = new CheckBox("Include .Webp Files");
        convertToWebpCheckBox = new CheckBox("Convert to .Webp");
        convertToWebpCheckBox.setSelected(true);
        showPreviewCheckBox = new CheckBox("Show Preview");
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
                UIHandler.showConfirmationDialog(
                        "Confirm Source File Deletion",
                        "Do you want to delete the source files?",
                        confirmed -> {
                            if (confirmed) {
                                deleteSourceFile = true;
                            } else {
                                deleteSourceFileCheckBox.setSelected(false);
                                deleteSourceFile = false;
                            }
                        }
                );
            } else {
                deleteSourceFile = false;
            }
        });
        includeWebpFilesCheckBox.setSelected(true);
        checkBoxes1.getChildren().addAll(deleteSourceFileCheckBox, subfoldersCheckBox, includeWebpFilesCheckBox);
        checkBoxes2.getChildren().addAll(upsaceleCheckBox, convertToWebpCheckBox, showPreviewCheckBox);

        HBox bottomButtons = new HBox(10);
        // Button to start processing
        processButton = new Button("Start");
        processButton.setOnAction(e -> {
            flag = true;
            processFiles();
        });

        // Close button
        closeButton = new Button("Exit");
        closeButton.setOnAction(e -> {
            Process process = conversionProcessRef.get(); // Get the current process
            if (process != null && process.isAlive()) {
                this.flag = false;
                process.destroy(); // Finish the process if it is active
                System.out.println("Canceled process.");
            }
            if (closeButton.getText().equals("Exit")) {
                System.exit(0); // Close the application
            } else {
                System.out.println("Cancel pressed");
            }
        });

        showSourceFolderButton = new Button("Show Source");
        showSourceFolderButton.setDisable(true);
        showSourceFolderButton.setOnAction(e -> System.out.println("No Folder Processing"));

        showDestinyFolderButton = new Button("Show Destiny");
        showDestinyFolderButton.setDisable(true);
        showDestinyFolderButton.setOnAction(e -> System.out.println("No Folder Processing"));

        pauseProcessButton = new Button("Pause");
        pauseProcessButton.setDisable(true);
        pauseProcessButton.setOnAction(e -> {
            if (flag) {
                isPaused.set(!isPaused.get()); // Alternate between pause and resumption
                pauseProcessButton.setText(isPaused.get() ? "Continue" : "Pause");
                closeButton.setDisable(isPaused.get());
                System.out.println(isPaused.get()
                        ? "Paused process"
                        : "Resumed process");
            }
        });

        bottomButtons.getChildren().addAll(processButton,  pauseProcessButton, closeButton, showSourceFolderButton, showDestinyFolderButton);

        textCurrentFolder = new Text("No folders processed yet.");
        textCurrentFile = new Text("No files processed yet.");

        // Add everything to the layout
        layout1.getChildren().addAll(inputBox, outputBox, modelBox, checkBoxes1, checkBoxes2,  textCurrentFolder, textCurrentFile, bottomButtons);
        layout2.getChildren().addAll(imageView);

        if (showPreviewCheckBox.isSelected())
            layout.getChildren().addAll(layout1, layout2); else
                layout.getChildren().addAll(layout1);

        // Configure and display the window

        if(showPreviewCheckBox.isSelected()){
            horizontalSize =600;
        } else {
            horizontalSize =500;
        }

        Scene scene = new Scene(layout, horizontalSize, verticalSize);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            Process process = conversionProcessRef.get(); // Get the current process
            if (process != null && process.isAlive()) {
                process.destroy(); // Stop the process
                System.out.println("Process canceled");
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
            UIHandler.showAlert(Alert.AlertType.ERROR, "Error", "Please select both input and output folders.");
            return;
        }

        File inputDir = new File(inputFolder);
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            UIHandler.showAlert(Alert.AlertType.ERROR, "Error", "Input folder is not valid.");
            return;
        }
        initControls();
        // Processing in a separate thread
        new Thread(() -> {
            try {
                processFolder(inputDir, new File(outputFolder), model, processSubfolders);
                UIHandler.showAlert(Alert.AlertType.INFORMATION, "Process finished", "Processing finished!");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                UIHandler.showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while processing files.");
            }
            Platform.runLater(() -> enableControls());
        }).start();
    }

    private void processFolder(File inputDir, File outputDir, String model, boolean processSubfolders) throws IOException, InterruptedException {
        String currentDir = System.getProperty("user.dir");

                boolean convertToWebp = convertToWebpCheckBox.isSelected();
        boolean upscalePicture = upsaceleCheckBox.isSelected();

        if (!outputDir.exists()) outputDir.mkdirs();

        File[] files = inputDir.listFiles();
        if (files == null) {
            return;
        }
        int totalFiles = files.length;
        int processedFiles = 1;
        double percentageDone = (1/(double)totalFiles)*100;
        String processedString;

        textCurrentFolder.setText("Current Folder: " + inputDir.getName());

        showSourceFolderButton.setOnAction(e -> FileManager.openFolder(inputDir.getAbsolutePath()));

        showDestinyFolderButton.setOnAction(e -> FileManager.openFolder(outputDir.getAbsolutePath()));

        for (File file : files) {

            if (!this.flag) { break; }

            while (isPaused.get()) {
                try {
                    System.out.println("Waiting ...");
                    Thread.sleep(500); // Wait while process is paused
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted thread.");
                    return;
                }
            }

            //Verify if the file extension is .webp and status of the checkbox that include webp files in the processing
            includeWebpFiles = includeWebpFilesCheckBox.isSelected() || !file.getName().endsWith("webp");

            //Original file must have one of these extension: jpg, jpeg, png, webp
            String fileName = file.getName().toLowerCase();
            if (file.isFile() && includeWebpFiles && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".webp"))) {

                File outputFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_improved.png"));
                File compressedFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_final.webp"));

                percentageDone = ((double) processedFiles/ (double) totalFiles)*100;
                processedString = String.format("Total folders processed: %d, processing file %d of %d in current folder (%.0f%%)", totalFoldersProcessed, processedFiles , totalFiles, percentageDone);
                textCurrentFile.setText(processedString);

                try {
                    if (showPreviewCheckBox.isSelected()) {
                        imageView.setImage(null);
                        FileInputStream input = new FileInputStream(file.getAbsolutePath());
                        Image image = new Image(input);
                        imageView.setImage(image);
                        input.close();
                    }

                    final Boolean excludePreEnhancedFiles = (fileName.contains("megapixel") || fileName.contains("resize") || fileName.contains("edit") || fileName.contains("final"));
                    ImageProcessor imageProcessor = new ImageProcessor(currentDir);
                    // Improve the image
                    if(!excludePreEnhancedFiles) {
                        if(upscalePicture && flag) imageProcessor.upscaleImage(file,outputFile,model,conversionProcessRef);
                    }
                    // Convert to Webp
                    if (convertToWebp && flag) imageProcessor.convertToWebP(file, outputFile, compressedFile, conversionProcessRef, upscalePicture, excludePreEnhancedFiles);
                    // Delete temporary improved 4x PNG file
                    if(!excludePreEnhancedFiles) {
                        if ((upscalePicture && convertToWebp && flag))
                            FileManager.deleteFile(outputFile.getAbsoluteFile());
                        // Delete original source file
                        if ((deleteSourceFile && (upscalePicture || convertToWebp) && flag))
                            FileManager.deleteFile(file.getAbsoluteFile());
                    } else {
                        if ((deleteSourceFile && (upscalePicture || convertToWebp) && flag))
                            FileManager.deleteFile(file.getAbsoluteFile());
                        else
                            if ((!deleteSourceFile && (upscalePicture || convertToWebp) && flag))
                                FileManager.copyFile(file, outputDir);
                            else {
                                FileManager.copyFile(file, outputDir);
                                FileManager.deleteFile(file.getAbsoluteFile());
                            }

                    }

                } catch (IOException e) {
                    System.err.println("IO error: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                } catch (InterruptedException e) {
                    System.err.println("The process was interrupted: " + e.getMessage());
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    throw e;
                } catch (RuntimeException e) {
                    System.err.println("RuntimeException: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                } catch (Error e) {
                    // Catch fatal errors like OutOfMemoryError
                    System.err.println("Critical system error: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                } catch (Exception e) {
                    System.err.println("Unexpected exception: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                // Update progress bar
                processedFiles++;
            } else if (processSubfolders && file.isDirectory()) {
                totalFoldersProcessed++;
                Thread.sleep(50);
                processFolder(file, new File(outputDir, file.getName()), model, true);
            }
        }
    }

    private void enableControls() {
        processButton.setDisable(false);
        inputFolderField.setDisable(false);
        outputFolderField.setDisable(false);
        browseInputButton.setDisable(false);
        browseOutputButton.setDisable(false);
        deleteSourceFileCheckBox.setDisable(false);
        deleteSourceFileCheckBox.setSelected(false);
        deleteSourceFile=false;
        upsaceleCheckBox.setDisable(false);
        convertToWebpCheckBox.setDisable(false);
        subfoldersCheckBox.setDisable(false);
        includeWebpFilesCheckBox.setDisable(false);
        modelComboBox.setDisable(false);
        closeButton.setText("Exit");
        showSourceFolderButton.setDisable(true);
        showDestinyFolderButton.setDisable(true);
        pauseProcessButton.setDisable(true);
        textCurrentFolder.setText("Current Folder:");
        textCurrentFile.setText("Current File:");
        imageView.setImage(null);
    }

    private void initControls() {
        processButton.setDisable(true);
        inputFolderField.setDisable(true);
        outputFolderField.setDisable(true);
        browseInputButton.setDisable(true);
        browseOutputButton.setDisable(true);
        deleteSourceFileCheckBox.setDisable(true);
        upsaceleCheckBox.setDisable(true);
        convertToWebpCheckBox.setDisable(true);
        subfoldersCheckBox.setDisable(true);
        includeWebpFilesCheckBox.setDisable(true);
        modelComboBox.setDisable(true);
        closeButton.setText("Cancel");
        showSourceFolderButton.setDisable(false);
        showDestinyFolderButton.setDisable(false);
        pauseProcessButton.setDisable(false);
    }
}
