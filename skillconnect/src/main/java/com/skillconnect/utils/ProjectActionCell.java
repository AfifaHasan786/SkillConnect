package com.skillconnect.utils;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.Project;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public class ProjectActionCell extends TableCell<Project, String> {
    private final JFXButton applyButton;
    private final HBox container;

    public ProjectActionCell() {
        applyButton = new JFXButton("Apply");
        applyButton.getStyleClass().add("primary-button");

        container = new HBox(5);
        container.getChildren().add(applyButton);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(container);
        }
    }

    public JFXButton getApplyButton() {
        return applyButton;
    }
}