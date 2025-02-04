package com.example.imageprocessorfx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.function.Consumer;

public class UIHandler {
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showConfirmationDialog(String title, String message, Consumer<Boolean> callback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            Optional<ButtonType> result = alert.showAndWait();
            boolean userResponse = result.isPresent() && result.get() == ButtonType.OK;

            callback.accept(userResponse); // Execute callback with the result
        });
    }
}
