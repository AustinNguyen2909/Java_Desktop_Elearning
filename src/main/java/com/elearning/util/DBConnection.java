package com.elearning.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton class for managing database connections
 */
public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    private final Properties config;

    private DBConnection() {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            config.load(input);
            Class.forName(config.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Get singleton instance
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    config.getProperty("db.url"),
                    config.getProperty("db.username"),
                    config.getProperty("db.password")
            );
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test connection
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
