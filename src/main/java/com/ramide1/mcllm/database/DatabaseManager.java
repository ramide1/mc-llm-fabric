package com.ramide1.mcllm.database;

import org.slf4j.Logger;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final Connection connection;
    private final Logger logger;
    private volatile int maxHistoryMessages;
    private boolean warnedDisconnected;

    public DatabaseManager(File dataFolder, Logger logger, int maxHistoryMessages) {
        this.logger = logger;
        this.maxHistoryMessages = maxHistoryMessages;
        this.connection = init(dataFolder);
    }

    private Connection init(File dataFolder) {
        Connection conn = null;
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            String url = "jdbc:sqlite:" + new File(dataFolder, "history.db").getAbsolutePath();
            conn = DriverManager.getConnection(url);
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
            return null;
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void setMaxHistoryMessages(int maxHistoryMessages) {
        this.maxHistoryMessages = maxHistoryMessages;
    }

    public synchronized void saveMessage(String userId, String role, String content) {
        if (!isConnected()) {
            if (!warnedDisconnected) {
                logger.warn("Database is not available. Messages will not be saved.");
                warnedDisconnected = true;
            }
            return;
        }
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

    public synchronized List<ChatMessage> getHistory(String userId) {
        List<ChatMessage> history = new ArrayList<>();
        if (!isConnected()) {
            if (!warnedDisconnected) {
                logger.warn("Database is not available. History will be empty.");
                warnedDisconnected = true;
            }
            return history;
        }
        String sql = "SELECT role, content FROM (" +
                "SELECT role, content, timestamp FROM history WHERE user_id = ? " +
                "ORDER BY timestamp DESC LIMIT ?) sub " +
                "ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, maxHistoryMessages);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new ChatMessage(rs.getString("role"), rs.getString("content")));
                }
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

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}