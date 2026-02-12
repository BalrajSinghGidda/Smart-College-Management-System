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

public class StudentDashboard {
    private Stage stage;
    private int studentId;
    private String studentName;

    public StudentDashboard(Stage stage, int studentId, String studentName) {
        this.stage = stage;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    public void show() {
        BorderPane root = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #16a085;");
        sidebar.setPrefWidth(200);

        Label brand = new Label("Student Panel");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label nameLbl = new Label("Welcome, " + studentName);
        nameLbl.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 12px;");

        Button btnMarks = createSidebarButton("My Marks");
        Button btnAttendance = createSidebarButton("My Attendance");
        Button btnTimetable = createSidebarButton("Time Table");
        Button btnEvents = createSidebarButton("Upcoming Events");
        Button btnChat = createSidebarButton("Global Chat");
        Button btnPrivateChat = createSidebarButton("Private Chat");
        Button btnNotices = createSidebarButton("Notices");
        Button btnSettings = createSidebarButton("Settings");
        Button btnLogout = createSidebarButton("Logout");

        sidebar.getChildren().addAll(brand, nameLbl, new Separator(), btnMarks, btnAttendance, btnTimetable, btnEvents, btnChat, btnPrivateChat, btnNotices, btnSettings, btnLogout);
        root.setLeft(sidebar);

        applyTheme(root);
        showMarks(root);

        btnMarks.setOnAction(e -> showMarks(root));
        btnAttendance.setOnAction(e -> showAttendance(root));
        btnTimetable.setOnAction(e -> showTimetable(root));
        btnEvents.setOnAction(e -> showEvents(root));
        btnChat.setOnAction(e -> AdminDashboard.showChat(root, studentName, studentId));
        btnPrivateChat.setOnAction(e -> root.setCenter(new PrivateChatUI(studentId).getView()));
        btnNotices.setOnAction(e -> showNotices(root));
        btnSettings.setOnAction(e -> root.setCenter(new SettingsUI(studentId, () -> applyTheme(root)).getView()));
        btnLogout.setOnAction(e -> new App().start(stage));

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Student Dashboard - " + studentName);
        stage.setScene(scene);
    }

    private void applyTheme(BorderPane root) {
        root.getStylesheets().clear();
        root.getStylesheets().add(getClass().getResource("/com/college/management/css/chat.css").toExternalForm());
        UserSettings settings = UserSettings.getSettings(studentId);
        root.getStyleClass().remove("dark-theme");
        if ("DARK".equals(settings.getTheme())) {
            root.getStyleClass().add("dark-theme");
        }
    }

    private void showTimetable(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("Class Schedule");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<FacultyDashboard.TimetableEntry> table = new TableView<>();
        setupTimetableTable(table);
        loadTimetable(table);

        content.getChildren().addAll(title, table);
        root.setCenter(content);
    }

    private void setupTimetableTable(TableView<FacultyDashboard.TimetableEntry> table) {
        TableColumn<FacultyDashboard.TimetableEntry, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        TableColumn<FacultyDashboard.TimetableEntry, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        TableColumn<FacultyDashboard.TimetableEntry, String> colSub = new TableColumn<>("Subject");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subject"));
        TableColumn<FacultyDashboard.TimetableEntry, String> colFac = new TableColumn<>("Faculty");
        colFac.setCellValueFactory(new PropertyValueFactory<>("facultyName"));
        table.getColumns().addAll(colDay, colTime, colSub, colFac);
    }

    private void loadTimetable(TableView<FacultyDashboard.TimetableEntry> table) {
        ObservableList<FacultyDashboard.TimetableEntry> data = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT day, time_slot, subject, faculty_name FROM timetable")) {
            while (rs.next()) {
                data.add(new FacultyDashboard.TimetableEntry(rs.getString("day"), rs.getString("time_slot"), rs.getString("subject"), rs.getString("faculty_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        table.setItems(data);
    }

    private void showEvents(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("Upcoming Events");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        ListView<String> listView = new ListView<>();
        AdminDashboard.loadEventsIntoList(listView);

        content.getChildren().addAll(title, listView);
        root.setCenter(content);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT;");
        return btn;
    }

    private void showMarks(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("Academic Results");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<MarkRecord> table = new TableView<>();
        TableColumn<MarkRecord, String> colSub = new TableColumn<>("Subject");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subject"));
        TableColumn<MarkRecord, Double> colScore = new TableColumn<>("Score");
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        table.getColumns().addAll(colSub, colScore);

        ObservableList<MarkRecord> data = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT subject, score FROM marks WHERE student_id = ?")) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.add(new MarkRecord(rs.getString("subject"), rs.getDouble("score")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        table.setItems(data);

        content.getChildren().addAll(title, table);
        root.setCenter(content);
    }

    private void showAttendance(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("Attendance Records");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        int present = 0, total = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT status FROM attendance WHERE student_id = ?")) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                total++;
                if ("PRESENT".equals(rs.getString("status"))) present++;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        double percent = total == 0 ? 0 : (double) present / total * 100;
        Label stats = new Label(String.format("Attendance: %d/%d (%.2f%%)", present, total, percent));
        stats.setStyle("-fx-font-size: 18px;");

        content.getChildren().addAll(title, stats);
        root.setCenter(content);
    }

    private void showNotices(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("College Notices");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        ListView<String> listView = new ListView<>();
        AdminDashboard.loadNoticesIntoList(listView);

        content.getChildren().addAll(title, listView);
        root.setCenter(content);
    }

    public static class MarkRecord {
        private String subject;
        private double score;
        public MarkRecord(String subject, double score) { this.subject = subject; this.score = score; }
        public String getSubject() { return subject; }
        public double getScore() { return score; }
    }
}
