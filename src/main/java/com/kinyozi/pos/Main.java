package com.kinyozi.pos;

import com.formdev.flatlaf.FlatDarkLaf;
import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set FlatLaf dark theme before any UI creation
        try {
            FlatDarkLaf.setup();
            UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        } catch (Exception ex) {
            System.err.println("Failed to set Look and Feel: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // Test DB connection, then show login
            if (!DatabaseConfig.testConnection()) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to the database.\n\nPlease check your database settings in:\n" +
                                "db.properties\n\nMake sure PostgreSQL is running and the credentials are correct.",
                        "Database Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                // Still show login - user can configure from settings
            }
            new LoginFrame().setVisible(true);
        });
    }
}