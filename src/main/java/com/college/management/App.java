package com.college.management;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseManager.initializeDatabase();
        showLoginScreen(stage);
    }

    private void showLoginScreen(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("College Management System - Login");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        Button loginButton = new Button("Login");
        loginButton.setMinWidth(100);

        Label statusLabel = new Label();

        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            handleLogin(user, pass, stage, statusLabel);
        });

        layout.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, statusLabel);

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    private void handleLogin(String username, String password, Stage stage, Label statusLabel) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT role FROM users WHERE username = ? AND password = ?")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = -1;
                try (PreparedStatement idStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                    idStmt.setString(1, username);
                    ResultSet rsId = idStmt.executeQuery();
                    if (rsId.next()) userId = rsId.getInt("id");
                }

                if ("ADMIN".equals(role)) {
                    new AdminDashboard(stage, userId, username).show();
                } else if ("FACULTY".equals(role)) {
                    new FacultyDashboard(stage, userId, username).show();
                } else if ("STUDENT".equals(role)) {
                    new StudentDashboard(stage, userId, username).show();
                }
            } else {
                statusLabel.setText("Invalid username or password.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}