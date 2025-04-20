package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.skillconnect.models.Project;
import com.skillconnect.models.ProjectApplication;
import com.skillconnect.models.User;
import com.skillconnect.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewApplicationsController {
    @FXML
    private JFXComboBox<Project> projectFilter;
    @FXML
    private JFXComboBox<String> statusFilter;
    @FXML
    private JFXTextField searchField;
    @FXML
    private TableView<ProjectApplication> applicationsTable;
    @FXML
    private TableColumn<ProjectApplication, String> projectColumn;
    @FXML
    private TableColumn<ProjectApplication, String> volunteerColumn;
    @FXML
    private TableColumn<ProjectApplication, String> statusColumn;
    @FXML
    private TableColumn<ProjectApplication, String> dateColumn;
    @FXML
    private VBox detailsPane;
    @FXML
    private Text projectTitle;
    @FXML
    private Text projectDescription;
    @FXML
    private Text volunteerName;
    @FXML
    private Text volunteerSkills;
    @FXML
    private JFXButton approveButton;
    @FXML
    private JFXButton rejectButton;
    @FXML
    private JFXButton chatButton;

    private User currentUser;
    private ObservableList<ProjectApplication> allApplications;

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        setupSearch();
        setupDetailsPane();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadApplications();
        loadProjects();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected"));
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> filterApplications());

        projectFilter.setOnAction(e -> filterApplications());
    }

    private void setupTable() {
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("projectTitle"));
        volunteerColumn.setCellValueFactory(new PropertyValueFactory<>("volunteerName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        applicationsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showApplicationDetails(newSelection);
                }
            });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> filterApplications());
    }

    private void setupDetailsPane() {
        detailsPane.setVisible(false);
    }

    private void loadApplications() {
        try {
            List<ProjectApplication> applications = ProjectApplication.getAdminApplications(currentUser.getId());
            allApplications = FXCollections.observableArrayList(applications);
            applicationsTable.setItems(allApplications);
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load applications", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProjects() {
        try {
            List<Project> projects = Project.getProjectsByAdmin(currentUser.getId());
            projectFilter.setItems(FXCollections.observableArrayList(projects));
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load projects", e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterApplications() {
        if (allApplications == null) return;

        String searchText = searchField.getText().toLowerCase();
        Project selectedProject = projectFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        List<ProjectApplication> filtered = allApplications.stream()
            .filter(app -> {
                boolean matchesSearch = searchText.isEmpty() ||
                    app.getVolunteerName().toLowerCase().contains(searchText) ||
                    app.getProjectTitle().toLowerCase().contains(searchText);

                boolean matchesProject = selectedProject == null ||
                    app.getProjectId() == selectedProject.getId();

                boolean matchesStatus = "All".equals(selectedStatus) ||
                    app.getStatus().equalsIgnoreCase(selectedStatus);

                return matchesSearch && matchesProject && matchesStatus;
            })
            .collect(Collectors.toList());

        applicationsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showApplicationDetails(ProjectApplication application) {
        projectTitle.setText(application.getProjectTitle());
        projectDescription.setText(application.getProjectDescription());
        volunteerName.setText(application.getVolunteerName());
        volunteerSkills.setText(application.getVolunteerSkills());

        boolean isPending = "PENDING".equals(application.getStatus());
        approveButton.setVisible(isPending);
        rejectButton.setVisible(isPending);

        approveButton.setOnAction(e -> updateApplicationStatus(application, "APPROVED"));
        rejectButton.setOnAction(e -> updateApplicationStatus(application, "REJECTED"));
        chatButton.setOnAction(e -> openChat());

        detailsPane.setVisible(true);
    }

    private void updateApplicationStatus(ProjectApplication application, String newStatus) {
        try {
            application.updateStatus(newStatus);
            loadApplications();
            AlertUtils.showInformationAlert("Success", "Application Updated",
                "The application status has been updated to " + newStatus);
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to update application", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openChat() {
        ProjectApplication selectedApplication = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApplication == null) {
            AlertUtils.showErrorAlert("Error", "No Selection", "Please select an application first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
            Parent chatView = loader.load();
            ChatController chatController = loader.getController();

            // Set the current user in the chat controller
            chatController.setUser(currentUser);

            // Replace the content in the main content area
            AnchorPane mainContent = (AnchorPane) applicationsTable.getScene().getRoot();
            mainContent.getChildren().clear();
            mainContent.getChildren().add(chatView);

            // Make sure the chat view fills the space
            AnchorPane.setTopAnchor(chatView, 0.0);
            AnchorPane.setBottomAnchor(chatView, 0.0);
            AnchorPane.setLeftAnchor(chatView, 0.0);
            AnchorPane.setRightAnchor(chatView, 0.0);

            // Let the user search for and select the volunteer they want to chat with
            try {
                User volunteer = selectedApplication.getVolunteerUser();
                if (volunteer != null) {
                    chatController.setUser(currentUser);
                    chatController.selectUser(volunteer);
                } else {
                    AlertUtils.showErrorAlert("Error", "User Not Found", "Could not find the volunteer user.");
                    return;
                }
            } catch (SQLException e) {
                AlertUtils.showErrorAlert("Error", "Database Error", "Could not load volunteer information: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } catch (IOException e) {
            AlertUtils.showErrorAlert("Error", "Load Error", "Failed to load chat view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}