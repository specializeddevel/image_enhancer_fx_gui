package com.example.imageprocessorfx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.function.Consumer;

public class ConfirmDialog {
    /**
     * Muestra un diálogo de confirmación sin bloquear el hilo principal.
     *
     * @param title   El título del diálogo.
     * @param message El mensaje del diálogo.
     * @param callback Acción que se ejecuta con la respuesta del usuario (true si acepta, false si cancela).
     */
    public void showConfirmationDialog(String title, String message, Consumer<Boolean> callback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null); // Encabezado opcional
            alert.setContentText(message);

            Optional<ButtonType> result = alert.showAndWait();
            boolean userResponse = result.isPresent() && result.get() == ButtonType.OK;

            callback.accept(userResponse); // Ejecutar el callback con el resultado
        });
    }
}
