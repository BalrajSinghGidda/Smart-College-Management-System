package com.college.management;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class PrivateChatUI {
    private int currentUserId;
    private User selectedUser;
    private TextArea chatArea;
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
        Label userLbl = new Label("Contacts");
        userLbl.setStyle("-fx-font-weight: bold;");
        
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
        VBox chatPane = new VBox(10);
        chatPane.setPadding(new Insets(10));
        
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(400);
        chatArea.setWrapText(true);

        HBox inputPane = new HBox(10);
        TextField txtMsg = new TextField();
        txtMsg.setPrefWidth(400);
        Button btnSend = new Button("Send");
        inputPane.getChildren().addAll(txtMsg, btnSend);

        chatPane.getChildren().addAll(new Label("Chat Window"), chatArea, inputPane);
        layout.setCenter(chatPane);

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
        if (selectedUser == null) {
            chatArea.setText("Select a contact to start chatting.");
            return;
        }
        List<String> history = ChatService.getPrivateConversation(currentUserId, selectedUser.getId());
        StringBuilder sb = new StringBuilder();
        for (String msg : history) {
            sb.append(msg).append("\n");
        }
        chatArea.setText(sb.toString());
        chatArea.setScrollTop(Double.MAX_VALUE);
    }
}
