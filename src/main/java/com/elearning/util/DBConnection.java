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
            String rawUrl = config.getProperty("db.url");
            if (rawUrl != null) {
                config.setProperty("db.url", normalizeDbUrl(rawUrl));
            }
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

    /**
     * Test DDL capability by creating and dropping a temp table.
     * Returns null when successful, otherwise returns error message.
     */
    public String testDDL() {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS _ddl_smoke (id INT)");
            stmt.execute("DROP TABLE _ddl_smoke");
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    private String normalizeDbUrl(String url) {
        if (!url.startsWith("jdbc:mysql://")) {
            return url;
        }

        String prefix = "jdbc:mysql://";
        String rest = url.substring(prefix.length());
        int slashIndex = rest.indexOf('/');
        String hostPort = slashIndex >= 0 ? rest.substring(0, slashIndex) : rest;
        String path = slashIndex >= 0 ? rest.substring(slashIndex) : "";

        String host;
        if (hostPort.contains(":")) {
            host = hostPort.substring(0, hostPort.indexOf(':'));
        } else {
            host = hostPort;
        }

        return prefix + host + ":3306" + path;
    }
}
