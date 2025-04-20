package com.skillconnect.components;

import com.skillconnect.models.Message;
import com.skillconnect.utils.SessionManager;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.time.format.DateTimeFormatter;

public class MessageCell extends ListCell<Message> {
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Label messageText = new Label();
    private final Label timestampLabel = new Label();
    private final HBox container = new HBox(10);
    private final VBox contentBox = new VBox(5);

    public MessageCell() {
        messageText.setWrapText(true);
        messageText.getStyleClass().add("message-content");
        timestampLabel.getStyleClass().add("message-timestamp");
        contentBox.getChildren().addAll(messageText, timestampLabel);
        container.getChildren().add(contentBox);
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        messageText.setText(message.getContent());

        // Safely handle timestamp
        if (message.getTimestamp() != null) {
            timestampLabel.setText(message.getTimestamp().format(timeFormatter));
            timestampLabel.setVisible(true);
        } else {
            timestampLabel.setVisible(false);
        }

        // Check if the message is from the current user
        boolean isCurrentUserMessage = message.getSender().getId() ==
            SessionManager.getInstance().getCurrentUser().getId();

        // Apply appropriate styles
        container.getStyleClass().clear();
        container.getStyleClass().add(isCurrentUserMessage ? "sent-message" : "received-message");
        container.setAlignment(isCurrentUserMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        setGraphic(container);
    }
}
