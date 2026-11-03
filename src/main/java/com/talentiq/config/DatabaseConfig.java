package com.talentiq.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database configuration and connection manager.
 * Provides thread-safe access to MySQL JDBC connections with fallback strategies.
 */
public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    private static String url;
    private static String username;
    private static String password;
    private static String driver;
    private static boolean initialized = false;

    private DatabaseConfig() {
        // Singleton / utility
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        Properties props = new Properties();

        // 1. Try external config file in current directory
        File externalConfigFile = new File("config/database.properties");
        if (externalConfigFile.exists()) {
            try (InputStream in = new FileInputStream(externalConfigFile)) {
                props.load(in);
                LOGGER.info("Loaded database configuration from external file: " + externalConfigFile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load external database.properties, falling back to classpath", e);
            }
        }

        // 2. If properties not filled, load from classpath
        if (props.isEmpty()) {
            try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
                if (in != null) {
                    props.load(in);
                    LOGGER.info("Loaded database configuration from classpath resource database.properties");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load classpath database.properties", e);
            }
        }

        // 3. Environment variable overrides (if present)
        String envUrl = System.getenv("TALENTIQ_DB_URL");
        String envUser = System.getenv("TALENTIQ_DB_USER");
        String envPass = System.getenv("TALENTIQ_DB_PASSWORD");

        url = (envUrl != null && !envUrl.isBlank()) ? envUrl : props.getProperty("db.url", "jdbc:mysql://localhost:3306/talentiq_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        username = (envUser != null && !envUser.isBlank()) ? envUser : props.getProperty("db.username", "root");
        password = (envPass != null) ? envPass : props.getProperty("db.password", "root");
        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
            initialized = true;
            LOGGER.info("MySQL JDBC Driver registered successfully: " + driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to register MySQL JDBC driver", e);
            throw new RuntimeException("MySQL JDBC Driver not found in classpath", e);
        }
    }

    /**
     * Obtains an active connection to MySQL.
     *
     * @return Connection object
     * @throws SQLException if a database error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            init();
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Tests if the database connection can be established.
     *
     * @return true if connected and query executes successfully
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static String getUrl() {
        if (!initialized) init();
        return url;
    }

    public static String getUsername() {
        if (!initialized) init();
        return username;
    }
}

