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
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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

    //Summary panel showing the sizes, below the progress bar
    private VBox summaryPane = new VBox(5);

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
        gc.fillText("Preview", 15, 90);

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

        summaryPane.setPadding(new Insets(5));
        summaryPane.setPrefHeight(80);          // scroll if it's full
        summaryPane.setStyle("-fx-border-color: gray; -fx-border-radius: 4;");

        ScrollPane scroll = new ScrollPane(summaryPane);
        scroll.setPrefHeight(80);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

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
            scroll,
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

    private void processFolder(File rootInput, File rootOutput, String model,
                               boolean processSubfolders) throws IOException, InterruptedException {

        String currentDir = System.getProperty("user.dir");
        boolean convertToWebp   = convertToWebpCheckBox.isSelected();
        boolean upscalePicture  = upsaceleCheckBox.isSelected();

        // Queue that save pairs (carpetaEntrada , carpetaSalida)
        Queue<Pair<File, File>> queue = new LinkedList<>();
        queue.add(new Pair<>(rootInput, rootOutput));

        Platform.runLater(() -> summaryPane.getChildren().clear());  //Clear pannel

        while (!queue.isEmpty()) {
            Pair<File, File> pair = queue.poll();
            File inputDir  = pair.getKey();
            File outputDir = pair.getValue();


            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }




            // --- Update UI ----------------------------------------------------
            Platform.runLater(() -> {
                textCurrentFolder.setText("Current Folder: " + inputDir.getName());
                showSourceFolderButton.setOnAction(e ->
                        FileManager.openFolder(inputDir.getAbsolutePath()));
                showDestinationFolderButton.setOnAction(e ->
                        FileManager.openFolder(outputDir.getAbsolutePath()));
                summaryPane.getChildren().add(
                        new Label("Processing Folder: " + inputDir.getName()));
            });

            // --- List files and folders --------------------------------------
            File[] files = inputDir.listFiles();
            if (files == null) continue;

            // Filter valid files
            List<File> imageFiles = new ArrayList<>();
            List<File> subDirs    = new ArrayList<>();

            for (File f : files) {
                if (f.isFile()) {
                    String name = f.getName().toLowerCase();
                    if ((name.endsWith(".jpg")  || name.endsWith(".jpeg") ||
                            name.endsWith(".png")   || name.endsWith(".webp"))) {
                        imageFiles.add(f);
                    }
                } else if (processSubfolders && f.isDirectory()) {
                    subDirs.add(f);
                }
            }

            // --- Process files -------------------------------------------------
            int totalFiles = imageFiles.size();
            int processed  = 0;

            for (File file : imageFiles) {

                if (!flag) return;                 // Cancelado
                while (isPaused.get()) {           // Pausado
                    Thread.sleep(500);
                }

                processed++;
                double progress = (double) processed / totalFiles;
                int finalProcessed = processed;
                Platform.runLater(() -> {
                    progressBar.setProgress(progress);
                    textCurrentFile.setText(String.format(
                            "Total folders processed: %d, file %d/%d (%.0f%%)",
                            totalFoldersProcessed, finalProcessed, totalFiles, progress * 100));
                });

                // Rest of logic (Preview, Upscale, Conversion, etc.)
                processSingleFile(file, outputDir, currentDir, model,
                        convertToWebp, upscalePicture);
            }

            // --- Resumen de la carpeta -----------------------------------------------
            final long inSize = folderSize(inputDir);
            final long outSize = folderSize(outputDir);
            final long saved = inSize - outSize;
            Platform.runLater(() -> {
                Text text = new Text("Folder: " + inputDir.getName() +
                        "  |  Original: " + bytesToMiB(inSize) + " MiB" +
                        "  |  Final: " + bytesToMiB(outSize) + " MiB" +
                        "  |  Δ: " + bytesToMiB(saved) + " MiB");
                text.setStyle(saved < 0 ? "-fx-fill: red;" : "-fx-fill: green;");
                text.setWrappingWidth(summaryPane.getWidth() - 20);  // Ajusta el ancho del texto

                ScrollPane scrollPane = new ScrollPane(text);
                scrollPane.setPrefHeight(40);  // Altura del scrollPane
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);  // Barra horizontal si es necesario

                summaryPane.getChildren().add(scrollPane);
            });

            // --- Enqueue subfolders ---------------------------------------------
            if (processSubfolders) {
                for (File subDir : subDirs) {
                    totalFoldersProcessed++;
                    queue.add(new Pair<>(subDir,
                            new File(outputDir, subDir.getName())));
                }
            }
        }
    }

    /* -------------------------------------------------------------------------- */
    /*  Auxiliary method containing all individual files processing               */
    /* -------------------------------------------------------------------------- */
    private void processSingleFile(File file, File outputDir,
                                   String currentDir, String model,
                                   boolean convertToWebp, boolean upscalePicture)
            throws IOException, InterruptedException {

        includeWebpFiles = includeWebpFilesCheckBox.isSelected()
                || !file.getName().toLowerCase().endsWith("webp");

        if (!includeWebpFiles) return;

        File outputFile   = new File(outputDir,
                file.getName().replaceFirst("\\.[^.]+$", "_improved.png"));
        File compressedFile = new File(outputDir,
                file.getName().replaceFirst("\\.[^.]+$", "_final.webp"));

        // Preview (If it is activated)
        if (showPreviewCheckBox.isSelected()) {
            showPreview(file);
        }

        String fileName = file.getName().toLowerCase();
        boolean excludePreEnhanced = fileName.contains("megapixel") ||
                fileName.contains("gigapixel") ||
                fileName.contains("resize") ||
                fileName.contains("edit") ||
                fileName.contains("final") ||
                fileName.contains("ignore");

        ImageProcessor ip = new ImageProcessor(currentDir);

        if (!excludePreEnhanced) {
            if (upscalePicture && flag)
                ip.upscaleImage(file, outputFile, model, conversionProcessRef);
        }

        // Conversion to Webp
        if (convertToWebp && flag) {
            ip.convertToWebP(file, outputFile, compressedFile, conversionProcessRef,
                    upscalePicture, excludePreEnhanced);
        }

        // Delete temporary / original files
        manageCleanup(file, outputFile, compressedFile, excludePreEnhanced);
    }

    /* -------------------------------------------------------------------------- */
    /*  Auxiliary Methods (preview and clean)                                   */
    /* -------------------------------------------------------------------------- */
    private void showPreview(File file) {
        try {
            Image img;
            if (file.getName().toLowerCase().endsWith(".webp")) {
                try (FileInputStream in = new FileInputStream(file)) {
                    img = new Image(in);
                    if (img.isError()) img = webpPlaceholderImage;
                }
            } else {
                try (FileInputStream in = new FileInputStream(file)) {
                    img = new Image(in);
                }
            }
            final Image finalImg = img;
            Platform.runLater(() -> imageView.setImage(finalImg));
        } catch (IOException ex) {
            System.err.println("Error showing preview: " + ex.getMessage());
        }
    }

    private void manageCleanup(File source, File improvedPng, File webp,
                               boolean excludePreEnhanced) {
        try {
            if (!excludePreEnhanced) {
                if (convertToWebpCheckBox.isSelected()
                        && upsaceleCheckBox.isSelected()
                        && improvedPng.exists()) {
                    FileManager.deleteFile(improvedPng);
                }
                if (deleteSourceFile
                        && (convertToWebpCheckBox.isSelected() || upsaceleCheckBox.isSelected())) {
                    FileManager.deleteFile(source);
                }
            } else {
                if (deleteSourceFile) {
                    FileManager.deleteFile(source);
                } else if (!deleteSourceFile && upsaceleCheckBox.isSelected()) {
                    FileManager.copyFile(source, improvedPng.getParentFile());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    private long folderSize(File dir) {
        if (!dir.isDirectory()) return 0L;
        long len = 0;
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isFile()) {
                    len += f.length();
                } else if (f.isDirectory()
                        && subfoldersCheckBox.isSelected()) {  // <- use the check
                    len += folderSize(f);
                }
            }
        }
        return len;
    }

    private String bytesToMiB(long bytes) {
        return String.format("%.2f", bytes / (1024.0 * 1024.0));
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
