package com.college.management;

import java.sql.*;

public class UserSettings {
    private int userId;
    private String theme;
    private String bubbleColor;
    private String displayName;

    public UserSettings(int userId, String theme, String bubbleColor, String displayName) {
        this.userId = userId;
        this.theme = theme;
        this.bubbleColor = bubbleColor;
        this.displayName = displayName;
    }

    public static UserSettings getSettings(int userId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user_settings WHERE user_id = ?")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserSettings(userId, rs.getString("theme"), rs.getString("bubble_color"), rs.getString("display_name"));
            } else {
                // Default settings
                return new UserSettings(userId, "LIGHT", "#e1ffc7", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new UserSettings(userId, "LIGHT", "#e1ffc7", null);
        }
    }

    public void save() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO user_settings (user_id, theme, bubble_color, display_name) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT(user_id) DO UPDATE SET theme=excluded.theme, bubble_color=excluded.bubble_color, display_name=excluded.display_name")) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, theme);
            pstmt.setString(3, bubbleColor);
            pstmt.setString(4, displayName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getBubbleColor() { return bubbleColor; }
    public void setBubbleColor(String bubbleColor) { this.bubbleColor = bubbleColor; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
