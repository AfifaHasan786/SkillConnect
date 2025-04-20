package com.skillconnect.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try {
            // Read schema.sql file
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    DatabaseInitializer.class.getResourceAsStream("/db/schema.sql")
                )
            );

            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }

            // Split into individual statements
            String[] statements = script.toString().split(";");

            // Execute each statement
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement);
                    }
                }
                System.out.println("Database schema initialized successfully");
            }

        } catch (Exception e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}