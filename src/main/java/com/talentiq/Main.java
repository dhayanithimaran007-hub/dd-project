package com.talentiq;

import com.talentiq.config.DatabaseConfig;
import com.talentiq.ui.MainFrame;
import com.talentiq.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TalentIQ – Resume & Skill Matching Management System
 * Application Main Entry Point.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Initialize modern Look and Feel
        ThemeManager.setupTheme();

        // 2. Initialize database subsystem
        DatabaseConfig.init();

        // 3. Test database connectivity with automatic diagnostic assistance
        if (!DatabaseConfig.testConnection()) {
            LOGGER.warning("Could not connect to MySQL at startup. Prompting database configuration...");
            boolean retrySuccess = promptDatabaseSetup();
            if (!retrySuccess) {
                JOptionPane.showMessageDialog(null,
                        "TalentIQ requires an active MySQL 8.x database.\n" +
                        "Please verify your MySQL service is running and 'talentiq_db' exists.\n" +
                        "See README.md for schema execution instructions.",
                        "Database Connection Notice", JOptionPane.WARNING_MESSAGE);
            }
        }

        // 4. Launch desktop interface on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.showLogin();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fatal error launching TalentIQ interface", e);
                JOptionPane.showMessageDialog(null,
                        "Fatal error during application startup:\n" + e.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static boolean promptDatabaseSetup() {
        JTextField txtUrl = new JTextField(DatabaseConfig.getUrl(), 26);
        JTextField txtUser = new JTextField(DatabaseConfig.getUsername(), 16);
        JPasswordField txtPass = new JPasswordField(16);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("JDBC URL:"));
        p.add(txtUrl);
        p.add(new JLabel("Username:"));
        p.add(txtUser);
        p.add(new JLabel("Password:"));
        p.add(txtPass);

        int res = JOptionPane.showConfirmDialog(null, p,
                "MySQL Database Connection Settings", JOptionPane.OK_CANCEL_OPTION);

        if (res == JOptionPane.OK_OPTION) {
            System.setProperty("TALENTIQ_DB_URL", txtUrl.getText().trim());
            System.setProperty("TALENTIQ_DB_USER", txtUser.getText().trim());
            System.setProperty("TALENTIQ_DB_PASSWORD", new String(txtPass.getPassword()));
            DatabaseConfig.init();
            return DatabaseConfig.testConnection();
        }
        return false;
    }
}

