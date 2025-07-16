package com.example.imageprocessorfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
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
    private CheckBox darkThemeCheckBox;

    private Button processButton;
    private Button browseInputButton;
    private Button browseOutputButton;
    private Button closeButton;
    private Button showSourceFolderButton;
    private Button showDestinationFolderButton; // Fixed naming from "Destiny" to "Destination"
    private Button pauseProcessButton;
    private boolean flag = true;
    private ImageView imageView;
    private Label textCurrentFolder;
    private Label textCurrentFile;
    private ProgressBar progressBar;

    // Placeholder image for WebP files
    private Image webpPlaceholderImage;



    private boolean deleteSourceFile = false;
    private boolean includeWebpFiles = false;
    private boolean isDarkTheme = false;

    private int totalFoldersProcessed = 1;

    // CSS paths for light and dark themes
    private String lightThemeCssPath;
    private String darkThemeCssPath;

    // Helper method to create a placeholder image for WebP files
    private void createPlaceholderWebpImage() {
        // Create a simple colored rectangle with text
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(100, 150);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.LIGHTBLUE);
        gc.fillRect(0, 0, 100, 150);
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillText("No",30, 75);
        gc.fillText("Preview", 25, 90);

        // Convert canvas to image
        javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
        canvas.snapshot(null, writableImage);
        webpPlaceholderImage = writableImage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize CSS paths
        lightThemeCssPath = getClass().getResource("/styles/application.css").toExternalForm();
        darkThemeCssPath = getClass().getResource("/styles/dark-theme.css").toExternalForm();

        createGUI(primaryStage);

        // Apply initial CSS theme to the scene
        if (isDarkTheme) {
            primaryStage.getScene().getStylesheets().add(darkThemeCssPath);
        } else {
            primaryStage.getScene().getStylesheets().add(lightThemeCssPath);
        }
    }

    // Method to switch between light and dark themes
    private void toggleTheme(Stage stage) {
        Scene scene = stage.getScene();
        scene.getStylesheets().clear();

        if (isDarkTheme) {
            scene.getStylesheets().add(darkThemeCssPath);
        } else {
            scene.getStylesheets().add(lightThemeCssPath);
        }
    }

    private void createGUI(Stage primaryStage) {

        int horizontalSize;
        int verticalSize = 600; // Increased to accommodate the new layout

        // Create a placeholder image for WebP files
        try {
            // Check if the WebP icon resource exists
            java.io.InputStream iconStream = getClass().getResourceAsStream("/webp-icon.png");

            if (iconStream != null) {
                // If the resource exists, load it
                webpPlaceholderImage = new Image(iconStream);
                iconStream.close();
            } else {
                // If the resource doesn't exist, create a placeholder directly
                createPlaceholderWebpImage();
            }

            // If the image is null or has an error, create a placeholder
            if (webpPlaceholderImage == null || webpPlaceholderImage.isError()) {
                createPlaceholderWebpImage();
            }
        } catch (Exception e) {
            System.err.println("Error creating WebP placeholder image: " + e.getMessage());
            // Ensure we always have a placeholder image even if an error occurs
            createPlaceholderWebpImage();
        }

        primaryStage.setTitle("Improve Images Quality And Optimize Size With AI");

        // Main layout with modern styling
        layout = new HBox(15);
        layout.setPadding(new Insets(15));
        layout.getStyleClass().add("container");

        layout1 = new VBox(15);
        layout1.setPadding(new Insets(10));
        layout1.getStyleClass().add("container");

        layout2 = new VBox(15);
        layout2.setPadding(new Insets(10));
        layout2.getStyleClass().add("container");

        // Add a title label
        Label titleLabel = new Label("Image Enhancer");
        titleLabel.getStyleClass().add("title-label");

        // Field for input folder
        HBox inputBox = new HBox(15);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        Label inputLabel = new Label("Input Folder:");
        inputFolderField = new TextField();
        inputFolderField.setPrefWidth(300);
        browseInputButton = new Button("Browse");
        browseInputButton.getStyleClass().add("secondary-button");
        browseInputButton.setOnAction(e -> selectFolder(primaryStage, inputFolderField));

        imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("image-preview");

        inputBox.getChildren().addAll(inputLabel, inputFolderField, browseInputButton);

        // Output folder field
        HBox outputBox = new HBox(15);
        outputBox.setAlignment(Pos.CENTER_LEFT);
        Label outputLabel = new Label("Output Folder:");
        outputFolderField = new TextField();
        outputFolderField.setPrefWidth(300);
        browseOutputButton = new Button("Browse");
        browseOutputButton.getStyleClass().add("secondary-button");
        browseOutputButton.setOnAction(e -> selectFolder(primaryStage, outputFolderField));
        outputBox.getChildren().addAll(outputLabel, outputFolderField, browseOutputButton);

        // Dropdown menu to select model
        HBox modelBox = new HBox(15);
        modelBox.setAlignment(Pos.CENTER_LEFT);
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
        modelComboBox.setPrefWidth(200);
        modelComboBox.getSelectionModel().selectFirst();
        modelBox.getChildren().addAll(modelLabel, modelComboBox);

        // Options section with label
        Label optionsLabel = new Label("Processing Options");
        optionsLabel.getStyleClass().add("section-header");

        HBox checkBoxes1 = new HBox(20);
        checkBoxes1.setAlignment(Pos.CENTER_LEFT);
        HBox checkBoxes2 = new HBox(20);
        checkBoxes2.setAlignment(Pos.CENTER_LEFT);

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
                primaryStage.setWidth(710); // Increased to accommodate the preview
            } else {
                layout.getChildren().remove(layout2);
                primaryStage.setWidth(560); // Increased to accommodate the new layout
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

        // Create dark theme toggle
        darkThemeCheckBox = new CheckBox("Dark Theme");
        darkThemeCheckBox.setSelected(isDarkTheme);
        darkThemeCheckBox.setOnAction(e -> {
            isDarkTheme = darkThemeCheckBox.isSelected();
            toggleTheme(primaryStage);
        });

        checkBoxes1.getChildren().addAll(deleteSourceFileCheckBox, subfoldersCheckBox, includeWebpFilesCheckBox);
        checkBoxes2.getChildren().addAll(upsaceleCheckBox, convertToWebpCheckBox, showPreviewCheckBox, darkThemeCheckBox);

        HBox bottomButtons = new HBox(10);
        // Button to start processing/
        processButton = new Button("Start");
        processButton.getStyleClass().add("success-button");
        processButton.setOnAction(e -> {
            flag = true;
            processFiles();
        });

        // Close button
        closeButton = new Button("Exit");
        closeButton.getStyleClass().add("danger-button");
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
        showSourceFolderButton.getStyleClass().add("secondary-button");
        showSourceFolderButton.setDisable(true);
        showSourceFolderButton.setOnAction(e -> System.out.println("No Folder Processing"));

        showDestinationFolderButton = new Button("Show Destination");
        showDestinationFolderButton.getStyleClass().add("secondary-button");
        showDestinationFolderButton.setDisable(true);
        showDestinationFolderButton.setOnAction(e -> System.out.println("No Folder Processing"));

        pauseProcessButton = new Button("Pause");
        pauseProcessButton.getStyleClass().add("secondary-button");
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

        bottomButtons.getChildren().addAll(processButton,  pauseProcessButton, closeButton, showSourceFolderButton, showDestinationFolderButton);

        // Create status section
        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("section-header");

        textCurrentFolder = new Label("No folders processed yet.");
        textCurrentFile = new Label("No files processed yet.");
        textCurrentFolder.getStyleClass().add("status-text");
        textCurrentFile.getStyleClass().add("status-text");

        // Create progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setVisible(false);

        // Add everything to the layout
        layout1.getChildren().addAll(
            titleLabel,
            inputBox, 
            outputBox, 
            modelBox, 
            new Separator(),
            optionsLabel,
            checkBoxes1, 
            checkBoxes2, 
            new Separator(),
            statusLabel,
            progressBar, 
            textCurrentFolder, 
            textCurrentFile, 
            bottomButtons
        );

        // Add image preview to right panel
        layout2.getChildren().addAll(
            new Label("Image Preview"),
            imageView
        );

        if (showPreviewCheckBox.isSelected())
            layout.getChildren().addAll(layout1, layout2); else
                layout.getChildren().addAll(layout1);

        // Configure and display the window

        if(showPreviewCheckBox.isSelected()){
            horizontalSize = 710; // Increased to accommodate the preview
        } else {
            horizontalSize = 550; // Increased to accommodate the new layout
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

        Platform.runLater(() -> {
            textCurrentFolder.setText("Current Folder: " + inputDir.getName());
            showSourceFolderButton.setOnAction(e -> FileManager.openFolder(inputDir.getAbsolutePath()));
            showDestinationFolderButton.setOnAction(e -> FileManager.openFolder(outputDir.getAbsolutePath()));
        });

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

            //Verify if the file extension is .webp and the status of the checkbox that include webp files in the processing
            includeWebpFiles = includeWebpFilesCheckBox.isSelected() || !file.getName().endsWith("webp");

            //Original file must have one of these extension: jpg, jpeg, png, webp
            String fileName = file.getName().toLowerCase();
            if (file.isFile() && includeWebpFiles && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".webp"))) {

                File outputFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_improved.png"));
                File compressedFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_final.webp"));

                percentageDone = ((double) processedFiles/ (double) totalFiles)*100;
                processedString = String.format("Total folders processed: %d, processing file %d of %d in current folder (%.0f%%)", totalFoldersProcessed, processedFiles , totalFiles, percentageDone);
                final String finalProcessedString = processedString;
                Platform.runLater(() -> {
                    textCurrentFile.setText(finalProcessedString);
                });

                // Update progress bar - create final copies for lambda
                final double progress = (double) processedFiles / totalFiles;
                Platform.runLater(() -> {
                    progressBar.setProgress(progress);
                });

                try {
                    if (showPreviewCheckBox.isSelected()) {
                        try {
                            if (file.getName().toLowerCase().endsWith(".webp")) {
                                // For WebP files, we'll use a special approach
                                // First, try to load it directly - this might work on some systems
                                try {
                                    FileInputStream input = new FileInputStream(file.getAbsolutePath());
                                    Image image = new Image(input);
                                    input.close();

                                    if (!image.isError()) {
                                        // If the image loaded successfully, display it
                                        Platform.runLater(() -> {
                                            imageView.setImage(null);
                                            imageView.setImage(image);
                                        });
                                    } else {
                                        // If there was an error loading the WebP image, show the placeholder
                                        Platform.runLater(() -> {
                                            imageView.setImage(null);
                                            imageView.setImage(webpPlaceholderImage);
                                            textCurrentFile.setText(textCurrentFile.getText() + " (WebP preview using placeholder)");
                                        });
                                    }
                                } catch (Exception e) {
                                    // If there was an exception loading the WebP image, show the placeholder
                                    Platform.runLater(() -> {
                                        imageView.setImage(null);
                                        imageView.setImage(webpPlaceholderImage);
                                        textCurrentFile.setText(textCurrentFile.getText() + " (WebP preview using placeholder)");
                                    });
                                }
                            } else {
                                // For other supported formats, load directly
                                FileInputStream input = new FileInputStream(file.getAbsolutePath());
                                Image image = new Image(input);
                                input.close();

                                Platform.runLater(() -> {
                                    imageView.setImage(null);
                                    imageView.setImage(image);
                                });
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading image preview: " + e.getMessage());
                        }
                    }

                    final boolean excludePreEnhancedFiles = (fileName.contains("megapixel") || fileName.contains("gigapixel") || fileName.contains("resize") || fileName.contains("edit") || fileName.contains("final") || fileName.contains("ignore"));

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
        showDestinationFolderButton.setDisable(true);
        pauseProcessButton.setDisable(true);
        textCurrentFolder.setText("Current Folder:");
        textCurrentFile.setText("Current File:");
        imageView.setImage(null);

        // Hide the progress bar
        progressBar.setVisible(false);
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
        showDestinationFolderButton.setDisable(false);
        pauseProcessButton.setDisable(false);

        // Show the progress bar
        progressBar.setVisible(true);
        progressBar.setProgress(0);
    }
}
