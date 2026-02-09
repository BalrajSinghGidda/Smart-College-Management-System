package com.college.management;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminDashboard {
    private Stage stage;
    private int userId;
    private String username;

    public AdminDashboard(Stage stage, int userId, String username) {
        this.stage = stage;
        this.userId = userId;
        this.username = username;
    }

    public void show() {
        BorderPane root = new BorderPane();

        // Sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(200);

        Label brand = new Label("Admin Panel");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnUsers = createSidebarButton("Manage Users");
        Button btnNotices = createSidebarButton("Post Notice");
        Button btnTimetable = createSidebarButton("Timetable");
        Button btnEvents = createSidebarButton("Events");
        Button btnChat = createSidebarButton("Global Chat");
        Button btnPrivateChat = createSidebarButton("Private Chat");
        Button btnReports = createSidebarButton("Reports");
        Button btnLogout = createSidebarButton("Logout");

        sidebar.getChildren().addAll(brand, new Separator(), btnUsers, btnNotices, btnTimetable, btnEvents, btnChat, btnPrivateChat, btnReports, btnLogout);

        root.setLeft(sidebar);

        // Default Content
        showUserManagement(root);

        btnUsers.setOnAction(e -> showUserManagement(root));
        btnNotices.setOnAction(e -> showNoticeManagement(root));
        btnTimetable.setOnAction(e -> showTimetableManagement(root));
        btnEvents.setOnAction(e -> showEventManagement(root));
        btnChat.setOnAction(e -> showChat(root, "Admin"));
        btnPrivateChat.setOnAction(e -> root.setCenter(new PrivateChatUI(userId).getView()));
        btnReports.setOnAction(e -> showReports(root));
        btnLogout.setOnAction(e -> new App().start(stage));

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
    }

    private void showTimetableManagement(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("Manage Timetable");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        
        ComboBox<String> cbDay = new ComboBox<>(FXCollections.observableArrayList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        cbDay.setPromptText("Day");
        TextField txtTime = new TextField(); txtTime.setPromptText("Time (e.g. 09:00-10:00)");
        TextField txtSubject = new TextField(); txtSubject.setPromptText("Subject");
        TextField txtFaculty = new TextField(); txtFaculty.setPromptText("Faculty Name");
        Button btnAdd = new Button("Add Entry");

        form.add(new Label("Day:"), 0, 0); form.add(cbDay, 1, 0);
        form.add(new Label("Time:"), 0, 1); form.add(txtTime, 1, 1);
        form.add(new Label("Subject:"), 0, 2); form.add(txtSubject, 1, 2);
        form.add(new Label("Faculty:"), 0, 3); form.add(txtFaculty, 1, 3);
        form.add(btnAdd, 1, 4);

        btnAdd.setOnAction(e -> {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO timetable (day, time_slot, subject, faculty_name) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, cbDay.getValue());
                pstmt.setString(2, txtTime.getText());
                pstmt.setString(3, txtSubject.getText());
                pstmt.setString(4, txtFaculty.getText());
                pstmt.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Timetable entry added!").show();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        content.getChildren().addAll(title, form);
        root.setCenter(content);
    }

    private void showEventManagement(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("Manage Events");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField txtTitle = new TextField(); txtTitle.setPromptText("Event Title");
        DatePicker datePicker = new DatePicker();
        TextArea txtDesc = new TextArea(); txtDesc.setPromptText("Event Description");
        Button btnAdd = new Button("Add Event");

        btnAdd.setOnAction(e -> {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO events (title, description, event_date) VALUES (?, ?, ?)")) {
                pstmt.setString(1, txtTitle.getText());
                pstmt.setString(2, txtDesc.getText());
                pstmt.setString(3, datePicker.getValue().toString());
                pstmt.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Event added!").show();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        content.getChildren().addAll(title, txtTitle, datePicker, txtDesc, btnAdd);
        root.setCenter(content);
    }

    public static void showChat(BorderPane root, String senderName) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        Label title = new Label("Global Chat / Announcements");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(400);

        HBox inputArea = new HBox(10);
        TextField txtMsg = new TextField();
        txtMsg.setPrefWidth(500);
        Button btnSend = new Button("Send");

        Runnable refreshChat = () -> {
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT sender, content, timestamp FROM messages ORDER BY id ASC")) {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append("[").append(rs.getString("timestamp")).append("] ")
                      .append(rs.getString("sender")).append(": ")
                      .append(rs.getString("content")).append("\n");
                }
                chatArea.setText(sb.toString());
                chatArea.setScrollTop(Double.MAX_VALUE);
            } catch (SQLException ex) { ex.printStackTrace(); }
        };

        btnSend.setOnAction(e -> {
            if (txtMsg.getText().isEmpty()) return;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO messages (sender, content) VALUES (?, ?)")) {
                pstmt.setString(1, senderName);
                pstmt.setString(2, txtMsg.getText());
                pstmt.executeUpdate();
                txtMsg.clear();
                refreshChat.run();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        refreshChat.run();
        inputArea.getChildren().addAll(txtMsg, btnSend);
        content.getChildren().addAll(title, chatArea, inputArea);
        root.setCenter(content);
    }

    private void showReports(BorderPane root) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label title = new Label("System Reports");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button btnCSV = new Button("Export Students to CSV");
        Button btnPDF = new Button("Export Students to PDF");

        btnCSV.setOnAction(e -> {
            try {
                ReportGenerator.exportStudentsCSV("students_report.csv");
                new Alert(Alert.AlertType.INFORMATION, "Exported to students_report.csv").show();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnPDF.setOnAction(e -> {
            try {
                ReportGenerator.exportStudentsPDF("students_report.pdf");
                new Alert(Alert.AlertType.INFORMATION, "Exported to students_report.pdf").show();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        content.getChildren().addAll(title, btnCSV, btnPDF);
        root.setCenter(content);
    }

    private void showNoticeManagement(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Post New Notice");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Notice Title");
        TextArea txtContent = new TextArea();
        txtContent.setPromptText("Notice Content...");
        Button btnPost = new Button("Post Notice");

        btnPost.setOnAction(e -> {
            if (txtTitle.getText().isEmpty() || txtContent.getText().isEmpty()) return;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO notices (title, content, date) VALUES (?, ?, ?)")) {
                pstmt.setString(1, txtTitle.getText());
                pstmt.setString(2, txtContent.getText());
                pstmt.setString(3, java.time.LocalDate.now().toString());
                pstmt.executeUpdate();
                txtTitle.clear();
                txtContent.clear();
                new Alert(Alert.AlertType.INFORMATION, "Notice Posted!").show();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        content.getChildren().addAll(title, txtTitle, txtContent, btnPost);
        root.setCenter(content);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT;");
        return btn;
    }

    private void showUserManagement(BorderPane root) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label title = new Label("User Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Form to add user
        HBox form = new HBox(10);
        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList("STUDENT", "FACULTY", "ADMIN"));
        cbRole.setPromptText("Role");
        Button btnAdd = new Button("Add User");

        form.getChildren().addAll(txtUsername, txtPassword, cbRole, btnAdd);

        // Table to show users
        TableView<User> table = new TableValue();
        setupUserTable(table);
        refreshUserTable(table);

        btnAdd.setOnAction(e -> {
            addUser(txtUsername.getText(), txtPassword.getText(), cbRole.getValue(), table);
            txtUsername.clear();
            txtPassword.clear();
        });

        content.getChildren().addAll(title, form, table);
        root.setCenter(content);
    }

    private void setupUserTable(TableView<User> table) {
        TableColumn<User, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        table.getColumns().addAll(colId, colUser, colRole);
    }

    private void refreshUserTable(TableView<User> table) {
        ObservableList<User> users = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM users")) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"), ""));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        table.setItems(users);
    }

    private void addUser(String user, String pass, String role, TableView<User> table) {
        if (user.isEmpty() || pass.isEmpty() || role == null) return;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            refreshUserTable(table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Inner class fix for TableView
    private static class TableValue extends TableView<User> {}
}
