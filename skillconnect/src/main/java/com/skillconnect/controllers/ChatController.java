package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.skillconnect.models.Message;
import com.skillconnect.models.User;
import com.skillconnect.utils.SessionManager;
import com.skillconnect.utils.AlertUtils;
import com.skillconnect.components.MessageCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;

import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;

public class ChatController {
    @FXML private CustomTextField searchField;
    @FXML private ListView<User> searchResultsList;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ListView<Message> messagesList;

    private User selectedUser;
    private Timer refreshTimer;
    private final ObservableList<User> searchResults = FXCollections.observableArrayList();
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    public void initialize() {
        System.out.println("ChatController initializing...");

        // Get the current user from the session
        User sessionUser = SessionManager.getInstance().getCurrentUser();
        System.out.println("Session user: " + (sessionUser != null ? sessionUser.getUsername() : "null"));

        if (sessionUser == null) {
            AlertUtils.showErrorAlert("Error", "Not Logged In", "Please log in to use the chat feature.");
            return;
        }

        // If we haven't explicitly set a user, use the one from the session
        if (this.currentUser == null) {
            this.currentUser = sessionUser;
            System.out.println("Using session user: " + this.currentUser.getUsername());
        }

        // Initialize UI components
        messagesList.setItems(messages);
        messagesList.setCellFactory(param -> new MessageCell());
        setupMessageInput();
        setupSearchField();
        setupSearchResultsList();
        setupRefreshTimer();

        // Load initial conversation
        loadConversation();
    }

    public void setUser(User user) {
        System.out.println("Setting user in ChatController: " + (user != null ? user.getUsername() : "null"));
        this.currentUser = user;
        // Update session if needed
        if (user != null && !user.equals(SessionManager.getInstance().getCurrentUser())) {
            SessionManager.getInstance().setCurrentUser(user);
        }
    }

    private void setupMessageField() {
        System.out.println("Setting up message field...");
        // Enable send button only when there's text and we have a selected user
        messageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (sendButton != null) {
                boolean hasText = newValue != null && !newValue.trim().isEmpty();
                boolean hasSelectedUser = selectedUser != null;
                sendButton.setDisable(!hasText || !hasSelectedUser);
            }
        });

        // Focus the message field when a user is selected
        messageField.setOnMouseClicked(event -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AlertUtils.showErrorAlert("Error", "Not Logged In", "Please log in to send messages.");
                return;
            }
            if (selectedUser == null) {
                AlertUtils.showInformationAlert(
                    "No Recipient",
                    null,
                    "Please select a volunteer to chat with first."
                );
            }
        });
    }

    private void setupSearchField() {
        if (searchField == null) {
            System.out.println("Warning: searchField is null during setup");
            return;
        }

        // Set appropriate prompt text based on user role
        if (currentUser.getRole().equals("VOLUNTEER")) {
            searchField.setPromptText("Search for admins...");
        } else {
            searchField.setPromptText("Search for volunteers...");
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                searchResults.clear();
                searchResultsList.setVisible(false);
                searchResultsList.setManaged(false);
                return;
            }
            try {
                List<User> users;
                if (currentUser.getRole().equals("VOLUNTEER")) {
                    users = User.searchAdmins(newValue);
                } else {
                    users = User.searchVolunteers(newValue);
                }
                searchResults.setAll(users);
                boolean hasResults = !users.isEmpty();
                searchResultsList.setVisible(hasResults);
                searchResultsList.setManaged(hasResults);

                if (hasResults) {
                    searchResultsList.toFront();
                }
            } catch (SQLException e) {
                System.out.println("Error searching users: " + e.getMessage());
                AlertUtils.showErrorAlert("Error", "Search Error", "Failed to search for users: " + e.getMessage());
            }
        });
    }

    private void setupSearchResultsList() {
        searchResultsList.setItems(searchResults);
        searchResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(user.getUsername());
                    setOnMouseClicked(event -> selectUser(user));
                }
            }
        });
    }

    public void selectUser(User user) {
        if (user == null) {
            System.out.println("Warning: Attempting to select null user");
            return;
        }
        selectedUser = user;
        System.out.println("Selected user: " + user.getUsername() + " (ID: " + user.getId() + ")");
        searchField.setText("Chatting with: " + user.getUsername());
        searchResults.clear();
        searchResultsList.setVisible(false);
        searchResultsList.setManaged(false);
        messageField.setDisable(false);
        messageField.requestFocus();
        loadConversation();
    }

    public void loadConversation() {
        if (currentUser == null) {
            System.out.println("Cannot load conversation: No current user set");
            return;
        }

        if (selectedUser == null) {
            System.out.println("Cannot load conversation: No selected user");
            return;
        }

        try {
            List<Message> newMessages = Message.getConversation(
                currentUser.getId(),
                selectedUser.getId()
            );

            System.out.println("Loaded " + newMessages.size() + " messages");
            messages.setAll(newMessages);

            // Scroll to bottom
            Platform.runLater(() -> {
                messagesList.scrollTo(messages.size() - 1);
                chatScrollPane.setVvalue(1.0);
            });
        } catch (SQLException e) {
            System.out.println("Error loading conversation: " + e.getMessage());
            AlertUtils.showErrorAlert("Error", "Load Error", "Failed to load conversation: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendMessage() {
        if (currentUser == null || selectedUser == null) {
            AlertUtils.showErrorAlert("Error", "Cannot Send Message",
                "Please select a recipient before sending a message.");
            return;
        }

        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        try {
            // Create and save the message with current timestamp
            Message message = new Message(
                null,
                content,
                currentUser,
                selectedUser,
                LocalDateTime.now()  // Set current timestamp
            );
            message.save();

            // Update UI immediately
            Platform.runLater(() -> {
                messages.add(message);
                messagesList.scrollTo(messages.size() - 1);
                chatScrollPane.setVvalue(1.0);
                messageField.clear();
                messageField.requestFocus(); // Keep focus on message field for continued typing
            });

        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to Send",
                "Could not send message: " + e.getMessage());
        }
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentUser != null && selectedUser != null) {
                    Platform.runLater(() -> {
                        try {
                            List<Message> newMessages = Message.getConversation(
                                currentUser.getId(),
                                selectedUser.getId()
                            );
                            // Only update if there are new messages and they're different from current ones
                            if (newMessages.size() > messages.size()) {
                                messages.setAll(newMessages);
                                messagesList.scrollTo(messages.size() - 1);
                                chatScrollPane.setVvalue(1.0);
                            }
                        } catch (SQLException e) {
                            System.err.println("Error refreshing messages: " + e.getMessage());
                        }
                    });
                }
            }
        }, 1000, 1000); // Check every second instead of 2 seconds
    }

    private void setupMessageInput() {
        messageField.setDisable(true); // Disable until user is selected
        sendButton.setDisable(true);

        messageField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasText = newValue != null && !newValue.trim().isEmpty();
            boolean hasSelectedUser = selectedUser != null;
            sendButton.setDisable(!hasText || !hasSelectedUser);
        });

        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !sendButton.isDisabled()) {
                handleSendMessage();
            }
        });
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }
}