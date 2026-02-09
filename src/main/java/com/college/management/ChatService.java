package com.college.management;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    public static void sendPrivateMessage(int senderId, int receiverId, String content) {
        String sql = "INSERT INTO private_messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<String> getPrivateConversation(int user1, int user2) {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT u.username as sender_name, pm.content, pm.timestamp " +
                     "FROM private_messages pm " +
                     "JOIN users u ON pm.sender_id = u.id " +
                     "WHERE (pm.sender_id = ? AND pm.receiver_id = ?) " +
                     "OR (pm.sender_id = ? AND pm.receiver_id = ?) " +
                     "ORDER BY pm.timestamp ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user1);
            pstmt.setInt(2, user2);
            pstmt.setInt(3, user2);
            pstmt.setInt(4, user1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add("[" + rs.getString("timestamp") + "] " + 
                             rs.getString("sender_name") + ": " + rs.getString("content"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return messages;
    }

    public static List<User> getChatableUsers(int currentUserId, String roleToFilter) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, role FROM users WHERE id != ?";
        if (roleToFilter != null) sql += " AND role = '" + roleToFilter + "'";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"), ""));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }
}
