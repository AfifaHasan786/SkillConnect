package com.skillconnect.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

public class StatusCellFactory<S> extends TableCell<S, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        Label statusLabel = new Label(item);
        statusLabel.getStyleClass().add("status-label");

        switch (item.toUpperCase()) {
            case "PENDING":
                statusLabel.getStyleClass().add("status-pending");
                break;
            case "APPROVED":
                statusLabel.getStyleClass().add("status-approved");
                break;
            case "REJECTED":
                statusLabel.getStyleClass().add("status-rejected");
                break;
            case "WITHDRAWN":
                statusLabel.getStyleClass().add("status-withdrawn");
                break;
            default:
                statusLabel.getStyleClass().add("status-default");
                break;
        }

        setGraphic(statusLabel);
        setText(null);
    }
}