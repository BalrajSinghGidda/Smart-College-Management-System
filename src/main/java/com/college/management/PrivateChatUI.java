package com.college.management;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class PrivateChatUI {
    private int currentUserId;
    private User selectedUser;
    private VBox messageContainer;
    private ScrollPane scrollPane;
    private ListView<User> userList;

    public PrivateChatUI(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    public Pane getView() {
        BorderPane layout = new BorderPane();
        
        // Left side: User List
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.setPrefWidth(200);
        leftPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-width: 0 1 0 0;");
        Label userLbl = new Label("Contacts");
        userLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        userList = new ListView<>();
        refreshUserList();
        
        userList.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getUsername() + " (" + item.getRole() + ")");
            }
        });

        leftPane.getChildren().addAll(userLbl, userList);
        layout.setLeft(leftPane);

        // Center: Chat Area
        VBox mainChatPane = new VBox();
        
        scrollPane = new ScrollPane();
        messageContainer = new VBox(15);
        messageContainer.setPadding(new Insets(20));
        messageContainer.getStyleClass().add("chat-container");
        messageContainer.setPrefWidth(550);
        
        scrollPane.setContent(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVvalue(1.0);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        HBox inputPane = new HBox(10);
        inputPane.setPadding(new Insets(10));
        inputPane.setStyle("-fx-background-color: #f0f0f0;");
        inputPane.setAlignment(Pos.CENTER);
        
        TextField txtMsg = new TextField();
        txtMsg.setPromptText("Type a message...");
        txtMsg.setPrefWidth(450);
        txtMsg.setStyle("-fx-background-radius: 20;");
        
        Button btnSend = new Button("Send");
        btnSend.setStyle("-fx-background-color: #128c7e; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");
        inputPane.getChildren().addAll(txtMsg, btnSend);

        mainChatPane.getChildren().addAll(scrollPane, inputPane);
        layout.setCenter(mainChatPane);

        // Logic
        userList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser = newVal;
            refreshMessages();
        });

        btnSend.setOnAction(e -> {
            if (selectedUser != null && !txtMsg.getText().isEmpty()) {
                ChatService.sendPrivateMessage(currentUserId, selectedUser.getId(), txtMsg.getText());
                txtMsg.clear();
                refreshMessages();
            }
        });

        return layout;
    }

    private void refreshUserList() {
        List<User> users = ChatService.getChatableUsers(currentUserId, null);
        userList.getItems().setAll(users);
    }

    private void refreshMessages() {
        messageContainer.getChildren().clear();
        if (selectedUser == null) return;

        List<ChatMessage> history = ChatService.getPrivateConversation(currentUserId, selectedUser.getId());
        for (ChatMessage msg : history) {
            boolean isMine = msg.getSenderId() == currentUserId;
            messageContainer.getChildren().add(createBubble(msg, isMine, currentUserId));
        }
        scrollPane.setVvalue(1.0);
    }

    public static VBox createBubble(ChatMessage msg, boolean isMine, int viewerId) {
        VBox bubbleWrapper = new VBox(2);
        bubbleWrapper.setMaxWidth(Double.MAX_VALUE);
        bubbleWrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(3);
        bubble.setMaxWidth(350);
        bubble.getStyleClass().add(isMine ? "chat-bubble-left" : "chat-bubble-right");

        if (isMine) {
            UserSettings settings = UserSettings.getSettings(viewerId);
            bubble.setStyle("-fx-bubble-color: " + settings.getBubbleColor() + ";");
        } else {
            bubble.setStyle("-fx-bubble-color: #ffffff;");
        }

        if (!isMine) {
            Label sender = new Label(msg.getSenderName());
            sender.getStyleClass().add("chat-sender");
            bubble.getChildren().add(sender);
        }

        Label content = new Label(msg.getContent());
        content.setWrapText(true);
        
        Label time = new Label(msg.getTimestamp());
        time.getStyleClass().add("chat-time");
        
        bubble.getChildren().addAll(content, time);
        bubbleWrapper.getChildren().add(bubble);
        
        return bubbleWrapper;
    }
}