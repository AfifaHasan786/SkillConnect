package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.ProjectApplication;
import com.skillconnect.models.User;
import com.skillconnect.utils.AlertUtils;
import com.skillconnect.utils.StatusCellFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.List;

public class MyApplicationsController implements Initializable {

    @FXML
    private TableView<ProjectApplication> applicationsTable;
    @FXML
    private TableColumn<ProjectApplication, String> dateColumn;
    @FXML
    private TableColumn<ProjectApplication, String> projectColumn;
    @FXML
    private TableColumn<ProjectApplication, String> statusColumn;
    @FXML
    private TableColumn<ProjectApplication, Void> actionsColumn;

    private User currentUser;
    private ObservableList<ProjectApplication> applications;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        applications = FXCollections.observableArrayList();

        // Configure table properties
        applicationsTable.setFixedCellSize(50);
        applicationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupTableColumns();
        applicationsTable.setItems(applications);
    }

    private void setupTableColumns() {
        // Date column setup with PropertyValueFactory
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("applicationDate"));
        dateColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        // Project column setup with PropertyValueFactory
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("projectTitle"));
        projectColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        // Status column setup
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<ProjectApplication, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-pending", "status-approved", "status-rejected", "status-withdrawn");
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-pending", "status-approved", "status-rejected", "status-withdrawn");
                    getStyleClass().add("status-" + item.toLowerCase());
                }
            }
        });

        // Actions column setup
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final JFXButton withdrawButton = new JFXButton("Withdraw");

            {
                withdrawButton.getStyleClass().addAll("button-secondary", "withdraw-button");
                withdrawButton.setOnAction(event -> {
                    ProjectApplication application = getTableView().getItems().get(getIndex());
                    if (application != null) {
                        handleWithdrawApplication(application);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ProjectApplication application = getTableView().getItems().get(getIndex());
                    if (application != null && "PENDING".equals(application.getStatus())) {
                        setGraphic(withdrawButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        // Set column IDs for CSS styling
        dateColumn.setId("appliedOn");
        projectColumn.setId("projectTitle");
        statusColumn.setId("status");
        actionsColumn.setId("action");

        // Set column widths
        dateColumn.setPrefWidth(150);
        projectColumn.setPrefWidth(300);
        statusColumn.setPrefWidth(150);
        actionsColumn.setPrefWidth(120);

        // Prevent column reordering
        applicationsTable.setTableMenuButtonVisible(false);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            loadApplications();
        }
    }

    private void loadApplications() {
        try {
            if (currentUser == null) {
                AlertUtils.showErrorAlert("Error", "User not set", "Cannot load applications without a user.");
                return;
            }
            List<ProjectApplication> userApplications = ProjectApplication.getVolunteerApplications(currentUser.getId());
            applications.setAll(userApplications);
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load applications", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Unexpected error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleWithdrawApplication(ProjectApplication application) {
        try {
            application.updateStatus("WITHDRAWN");
            loadApplications(); // Refresh the table
            AlertUtils.showInformationAlert("Success", "Application Withdrawn",
                "Your application has been successfully withdrawn.");
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to withdraw application", e.getMessage());
            e.printStackTrace();
        }
    }
}