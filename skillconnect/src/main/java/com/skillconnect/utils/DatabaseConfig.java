package com.skillconnect.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class DatabaseConfig {
    private static boolean isInitialized = false;

    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initializeDatabase();
        }
        return DatabaseConnection.getConnection();
    }

    private static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Create database if it doesn't exist
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS skillconnect");
                stmt.execute("USE skillconnect");
            }

            // Execute schema.sql
            executeSqlFile(conn, "/schema.sql");

            // Insert sample data if needed
            insertSampleData(conn);

            isInitialized = true;
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private static void executeSqlFile(Connection conn, String resourcePath) throws SQLException {
        try {
            InputStream inputStream = DatabaseConfig.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Could not find resource: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.trim().startsWith("--")) {
                        sql.append(line);
                        if (line.trim().endsWith(";")) {
                            stmt.execute(sql.toString());
                            sql.setLength(0);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error reading SQL file: " + e.getMessage(), e);
        }
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        // Add your sample data insertion logic here if needed
    }

    private static void handleDatabaseError(SQLException e) {
        String errorMessage = "Database Error: " + e.getMessage();
        System.err.println(errorMessage);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText("Failed to initialize database");
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
}