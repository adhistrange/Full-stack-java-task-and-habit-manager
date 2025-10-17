package com.example.taskhabitmanager;

import java.sql.*;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:desktop_taskhabit.db";

    public static void init() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (Statement st = conn.createStatement()) {
                // Tasks
                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS tasks (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "title TEXT NOT NULL," +
                                "description TEXT," +
                                "dueDate TEXT," +
                                "completed INTEGER DEFAULT 0" +
                                ")"
                );

                // Habits
                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS habits (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "name TEXT NOT NULL," +
                                "dayOfWeek TEXT" +
                                ")"
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Example helper: insert a local task (not used by UI by default)
    public static long insertTask(String title, String desc, String dueDate, boolean completed) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO tasks(title, description, dueDate, completed) VALUES(?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, title);
                ps.setString(2, desc);
                ps.setString(3, dueDate);
                ps.setInt(4, completed ? 1 : 0);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        }
        return -1;
    }
}
