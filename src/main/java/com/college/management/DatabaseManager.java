package com.college.management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:college.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "role TEXT NOT NULL" + // ADMIN, FACULTY, STUDENT
                    ")");

            // Profiles table
            stmt.execute("CREATE TABLE IF NOT EXISTS profiles (" +
                    "user_id INTEGER PRIMARY KEY," +
                    "full_name TEXT," +
                    "email TEXT," +
                    "department TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            // Attendance
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "student_id INTEGER," +
                    "date TEXT," +
                    "status TEXT," + // PRESENT, ABSENT
                    "FOREIGN KEY(student_id) REFERENCES users(id)" +
                    ")");

            // Marks
            stmt.execute("CREATE TABLE IF NOT EXISTS marks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "student_id INTEGER," +
                    "subject TEXT," +
                    "score REAL," +
                    "FOREIGN KEY(student_id) REFERENCES users(id)" +
                    ")");

            // Notices
            stmt.execute("CREATE TABLE IF NOT EXISTS notices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "content TEXT," +
                    "date TEXT," +
                    "posted_by INTEGER," +
                    "FOREIGN KEY(posted_by) REFERENCES users(id)" +
                    ")");

            // Timetable
            stmt.execute("CREATE TABLE IF NOT EXISTS timetable (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "day TEXT," + // Monday, Tuesday, etc.
                    "time_slot TEXT," + // e.g., 09:00 - 10:00
                    "subject TEXT," +
                    "faculty_name TEXT," +
                    "target_role TEXT" + // ALL, STUDENT, FACULTY
                    ")");

            // Events
            stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "description TEXT," +
                    "event_date TEXT" +
                    ")");

            // Messages (Simple Chat)
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender TEXT," +
                    "content TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Private Messages (One-to-One)
            stmt.execute("CREATE TABLE IF NOT EXISTS private_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender_id INTEGER," +
                    "receiver_id INTEGER," +
                    "content TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(sender_id) REFERENCES users(id)," +
                    "FOREIGN KEY(receiver_id) REFERENCES users(id)" +
                    ")");

            // User Settings
            stmt.execute("CREATE TABLE IF NOT EXISTS user_settings (" +
                    "user_id INTEGER PRIMARY KEY," +
                    "theme TEXT DEFAULT 'LIGHT'," +
                    "bubble_color TEXT DEFAULT '#e1ffc7'," +
                    "display_name TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            // Insert default admin if not exists
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')");
            
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
