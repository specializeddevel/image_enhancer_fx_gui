package com.example.imageprocessorfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;

public class ImageProcessorFX extends Application {

    private TextField inputFolderField;
    private TextField outputFolderField;
    private ComboBox<String> modelComboBox;
    private CheckBox subfoldersCheckBox;
    private ProgressBar progressBar;
    private Process conversionProcess;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Processor with JavaFX");


        // Layout principal
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));


        // Campo para la carpeta de entrada
        HBox inputBox = new HBox(15);
        Label inputLabel = new Label("Input Folder:");
        inputFolderField = new TextField();
        inputFolderField.setPrefWidth(300);
        Button browseInputButton = new Button("Browse");
        browseInputButton.setOnAction(e -> selectFolder(primaryStage, inputFolderField));
        inputBox.getChildren().addAll(inputLabel, inputFolderField, browseInputButton);

        // Campo para la carpeta de salida
        HBox outputBox = new HBox(10);
        Label outputLabel = new Label("Output Folder:");
        outputFolderField = new TextField();
        outputFolderField.setPrefWidth(300);
        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> selectFolder(primaryStage, outputFolderField));
        outputBox.getChildren().addAll(outputLabel, outputFolderField, browseOutputButton);

        // Menú desplegable para seleccionar el modelo
        HBox modelBox = new HBox(10);
        Label modelLabel = new Label("Model:");
        modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll(
                "realesrgan-x4plus",
                "realesr-animevideov3",
                "realesrnet-x4plus"
        );
        modelComboBox.getSelectionModel().selectFirst();
        modelBox.getChildren().addAll(modelLabel, modelComboBox);

        // Checkbox para subcarpetas
        subfoldersCheckBox = new CheckBox("Process Subfolders");

        // Barra de progreso
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        HBox bottomButtons = new HBox(10);
        // Botón para iniciar el procesamiento
        Button processButton = new Button("Process");
        processButton.setOnAction(e -> processFiles());

        // Botón para cerrar
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            if (conversionProcess != null && conversionProcess.isAlive()) {
                conversionProcess.destroy(); // Termina el proceso si está activo
            }
            Platform.exit(); // Cierra la aplicación
        });

        bottomButtons.getChildren().addAll(processButton, closeButton);

        // Agregar todo al layout
        layout.getChildren().addAll(inputBox, outputBox, modelBox, subfoldersCheckBox, progressBar, bottomButtons);

        // Configurar y mostrar la ventana
        Scene scene = new Scene(layout, 500, 200);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            if (conversionProcess != null && conversionProcess.isAlive()) {
                conversionProcess.destroy(); // Detiene el proceso
            }
            Platform.exit(); // Cierra la aplicación
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

        // Procesamiento en un hilo aparte
        new Thread(() -> {
            try {
                processFolder(inputDir, new File(outputFolder), model, processSubfolders);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Processing completed!");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while processing files.");
            }
            progressBar.setProgress(1);
        }).start();
    }

    private void processFolder(File inputDir, File outputDir, String model, boolean processSubfolders) throws IOException, InterruptedException {
        String currentDir = System.getProperty("user.dir");

        // Rutas completas a los ejecutables
        File realesrganExecutable = new File(currentDir, "realesrgan-ncnn-vulkan");
        File cwebpExecutable = new File(currentDir, "cwebp");


        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles();
        if (files == null) return;

        int totalFiles = files.length;
        int processedFiles = 0;

        for (File file : files) {
            if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".webp"))) {
                File outputFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_mejorado.webp"));
                File compressedFile = new File(outputDir, file.getName().replaceFirst("\\.[^.]+$", "_mejoradoc.webp"));

                // Ejecutar realesrgan-ncnn-vulkan
//                new ProcessBuilder(realesrganExecutable.getAbsolutePath(), "-i", file.getAbsolutePath(), "-o", outputFile.getAbsolutePath(), "-n", model, "-f", "webp")
//                        .inheritIO()
//                        .start()
//                        .waitFor();
                try {
                    // Configura y ejecuta el proceso
                    ProcessBuilder realesrganProcessBuilder = new ProcessBuilder(
                            realesrganExecutable.getAbsolutePath(),
                            "-i", file.getAbsolutePath(),
                            "-o", outputFile.getAbsolutePath(),
                            "-n", model,
                            "-f", "webp"
                    );

                    // Inicia el proceso y guarda la referencia
                    conversionProcess = realesrganProcessBuilder.start();

                    // Espera a que el proceso termine (opcional si es necesario)
                    conversionProcess.waitFor();

                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    // Limpia la referencia al proceso una vez que finalice
                    conversionProcess = null;
                }

                // Ejecutar cwebp
//                new ProcessBuilder(cwebpExecutable.getAbsolutePath(), "-q", "80", outputFile.getAbsolutePath(), "-o", compressedFile.getAbsolutePath())
//                        .inheritIO()
//                        .start()
//                        .waitFor();
//
//                // Eliminar archivo temporal
//                outputFile.delete();

                // Actualizar barra de progreso
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
