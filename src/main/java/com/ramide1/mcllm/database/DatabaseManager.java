package com.ramide1.mcllm.database;

import org.slf4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final Connection connection;
    private final Logger logger;

    public DatabaseManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.connection = init(dataFolder);
    }

    private Connection init(File dataFolder) {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            String url = "jdbc:sqlite:" + new File(dataFolder, "history.db").getAbsolutePath();
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id TEXT," +
                        "role TEXT," +
                        "content TEXT," +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            }
            logger.info("SQLite database initialized successfully.");
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to initialize SQLite database", e);
            return null;
        }
    }

    public void saveMessage(String userId, String role, String content) {
        String sql = "INSERT INTO history(user_id, role, content) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, role);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving message to database", e);
        }
    }

    public List<ChatMessage> getHistory(String userId) {
        List<ChatMessage> history = new ArrayList<>();
        String sql = "SELECT role, content FROM history WHERE user_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new ChatMessage(rs.getString("role"), rs.getString("content")));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving history from database", e);
        }
        return history;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

    public static class ChatMessage {
        private final String role;
        private final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
