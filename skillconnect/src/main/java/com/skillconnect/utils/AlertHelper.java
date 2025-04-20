package com.skillconnect.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

public class AlertHelper {
    public static void showErrorAlert(String title, String header, String content) {
        showAlert(AlertType.ERROR, title, header, content, null);
    }

    public static void showInformationAlert(String title, String header, String content) {
        showAlert(AlertType.INFORMATION, title, header, content, null);
    }

    public static void showWarningAlert(String title, String header, String content) {
        showAlert(AlertType.WARNING, title, header, content, null);
    }

    public static void showConfirmationAlert(String title, String header, String content) {
        showAlert(AlertType.CONFIRMATION, title, header, content, null);
    }

    private static void showAlert(AlertType alertType, String title, String header, String content, Window owner) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.showAndWait();
    }
}