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
import java.time.LocalDate;

public class FacultyDashboard {
    private Stage stage;
    private int userId;
    private String username;

    public FacultyDashboard(Stage stage, int userId, String username) {
        this.stage = stage;
        this.userId = userId;
        this.username = username;
    }

    public void show() {
        BorderPane root = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #34495e;");
        sidebar.setPrefWidth(200);

        Label brand = new Label("Faculty Panel");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnAttendance = createSidebarButton("Attendance");
        Button btnMarks = createSidebarButton("Marks Entry");
        Button btnTimetable = createSidebarButton("My Timetable");
        Button btnEvents = createSidebarButton("Events");
        Button btnNotices = createSidebarButton("Notices");
        Button btnChat = createSidebarButton("Global Chat");
        Button btnPrivateChat = createSidebarButton("Private Chat");
        Button btnSettings = createSidebarButton("Settings");
        Button btnLogout = createSidebarButton("Logout");

        sidebar.getChildren().addAll(brand, new Separator(), btnAttendance, btnMarks, btnTimetable, btnEvents, btnNotices, btnChat, btnPrivateChat, btnSettings, btnLogout);
        root.setLeft(sidebar);

        applyTheme(root);
        showAttendanceManagement(root);

        btnAttendance.setOnAction(e -> showAttendanceManagement(root));
        btnMarks.setOnAction(e -> showMarksEntry(root));
        btnTimetable.setOnAction(e -> showTimetable(root));
        btnEvents.setOnAction(e -> showEvents(root));
        btnNotices.setOnAction(e -> showNotices(root));
        btnChat.setOnAction(e -> AdminDashboard.showChat(root, username, userId));
        btnPrivateChat.setOnAction(e -> root.setCenter(new PrivateChatUI(userId).getView()));
        btnSettings.setOnAction(e -> root.setCenter(new SettingsUI(userId, () -> applyTheme(root)).getView()));
        btnLogout.setOnAction(e -> new App().start(stage));

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Faculty Dashboard");
        stage.setScene(scene);
    }

    private void showEvents(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("College Events");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ListView<String> listView = new ListView<>();
        AdminDashboard.loadEventsIntoList(listView);

        Button btnShowForm = new Button("Create New Event");
        btnShowForm.setStyle("-fx-background-color: #128c7e; -fx-text-fill: white; -fx-font-weight: bold;");
        
        btnShowForm.setOnAction(e -> {
            VBox form = new VBox(10);
            form.setPadding(new Insets(15));
            form.setStyle("-fx-border-color: #dcdcdc; -fx-border-radius: 5;");
            
            TextField txtTitle = new TextField(); txtTitle.setPromptText("Event Title");
            DatePicker datePicker = new DatePicker();
            TextArea txtDesc = new TextArea(); txtDesc.setPromptText("Event Description");
            
            HBox actions = new HBox(10);
            Button btnSave = new Button("Post Event");
            Button btnCancel = new Button("Cancel");
            actions.getChildren().addAll(btnSave, btnCancel);

            btnSave.setOnAction(ev -> {
                if (txtTitle.getText().isEmpty() || datePicker.getValue() == null) return;
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO events (title, description, event_date) VALUES (?, ?, ?)")) {
                    pstmt.setString(1, txtTitle.getText());
                    pstmt.setString(2, txtDesc.getText());
                    pstmt.setString(3, datePicker.getValue().toString());
                    pstmt.executeUpdate();
                    new Alert(Alert.AlertType.INFORMATION, "Event added!").show();
                    showEvents(root);
                } catch (SQLException ex) { ex.printStackTrace(); }
            });

            btnCancel.setOnAction(ev -> content.getChildren().remove(form));

            form.getChildren().addAll(new Label("New Event Details"), txtTitle, datePicker, txtDesc, actions);
            content.getChildren().add(form);
        });

        content.getChildren().addAll(title, listView, btnShowForm);
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

    private void applyTheme(BorderPane root) {
        root.getStylesheets().clear();
        root.getStylesheets().add(getClass().getResource("/com/college/management/css/chat.css").toExternalForm());
        UserSettings settings = UserSettings.getSettings(userId);
        root.getStyleClass().remove("dark-theme");
        if ("DARK".equals(settings.getTheme())) {
            root.getStyleClass().add("dark-theme");
        }
    }

    private void showTimetable(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label("College Timetable");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<TimetableEntry> table = new TableView<>();
        setupTimetableTable(table);
        loadTimetable(table);

        content.getChildren().addAll(title, table);
        root.setCenter(content);
    }

    private void setupTimetableTable(TableView<TimetableEntry> table) {
        TableColumn<TimetableEntry, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        TableColumn<TimetableEntry, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        TableColumn<TimetableEntry, String> colSub = new TableColumn<>("Subject");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subject"));
        TableColumn<TimetableEntry, String> colFac = new TableColumn<>("Faculty");
        colFac.setCellValueFactory(new PropertyValueFactory<>("facultyName"));
        table.getColumns().addAll(colDay, colTime, colSub, colFac);
    }

    private void loadTimetable(TableView<TimetableEntry> table) {
        ObservableList<TimetableEntry> data = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT day, time_slot, subject, faculty_name FROM timetable")) {
            while (rs.next()) {
                data.add(new TimetableEntry(rs.getString("day"), rs.getString("time_slot"), rs.getString("subject"), rs.getString("faculty_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        table.setItems(data);
    }

    public static class TimetableEntry {
        private String day, timeSlot, subject, facultyName;
        public TimetableEntry(String d, String t, String s, String f) { day = d; timeSlot = t; subject = s; facultyName = f; }
        public String getDay() { return day; }
        public String getTimeSlot() { return timeSlot; }
        public String getSubject() { return subject; }
        public String getFacultyName() { return facultyName; }
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT;");
        return btn;
    }

    private void showAttendanceManagement(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Mark Attendance");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TableView<User> table = new TableView<>();
        setupStudentTable(table);
        loadStudents(table);

        HBox controls = new HBox(10);
        Button btnPresent = new Button("Mark Present");
        Button btnAbsent = new Button("Mark Absent");
        controls.getChildren().addAll(new Label("Date:"), datePicker, btnPresent, btnAbsent);

        btnPresent.setOnAction(e -> markAttendance(table, datePicker.getValue(), "PRESENT"));
        btnAbsent.setOnAction(e -> markAttendance(table, datePicker.getValue(), "ABSENT"));

        content.getChildren().addAll(title, controls, table);
        root.setCenter(content);
    }

    private void showMarksEntry(BorderPane root) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Enter Marks");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TableView<User> table = new TableView<>();
        setupStudentTable(table);
        loadStudents(table);

        HBox controls = new HBox(10);
        TextField txtSubject = new TextField();
        txtSubject.setPromptText("Subject");
        TextField txtScore = new TextField();
        txtScore.setPromptText("Score");
        Button btnSave = new Button("Save Mark");

        controls.getChildren().addAll(txtSubject, txtScore, btnSave);

        btnSave.setOnAction(e -> saveMark(table, txtSubject.getText(), txtScore.getText()));

        content.getChildren().addAll(title, controls, table);
        root.setCenter(content);
    }

    private void setupStudentTable(TableView<User> table) {
        TableColumn<User, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<User, String> colUser = new TableColumn<>("Student Name");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        table.getColumns().addAll(colId, colUser);
    }

    private void loadStudents(TableView<User> table) {
        ObservableList<User> students = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, username FROM users WHERE role = 'STUDENT'")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(new User(rs.getInt("id"), rs.getString("username"), "STUDENT", ""));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        table.setItems(students);
    }

    private void markAttendance(TableView<User> table, LocalDate date, String status) {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null || date == null) return;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, selected.getId());
            pstmt.setString(2, date.toString());
            pstmt.setString(3, status);
            pstmt.executeUpdate();
            showAlert("Success", "Attendance marked as " + status);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void saveMark(TableView<User> table, String subject, String scoreStr) {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null || subject.isEmpty() || scoreStr.isEmpty()) return;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO marks (student_id, subject, score) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, selected.getId());
            pstmt.setString(2, subject);
            pstmt.setDouble(3, Double.parseDouble(scoreStr));
            pstmt.executeUpdate();
            showAlert("Success", "Marks saved for " + subject);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
