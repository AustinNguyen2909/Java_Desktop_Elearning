package com.elearning;

import com.elearning.ui.LoginFrame;
import com.elearning.util.DBConnection;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main application entry point
 */
public class Main {
    public static void main(String[] args) {
        // Set look and feel before creating any GUI components
        try {
            // Load theme from config
            String theme = loadTheme();
            applyTheme(theme);
            
            // Set additional UI properties for better appearance
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackInsets", new Insets(2, 4, 2, 4));
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TabbedPane.selectedBackground", Color.decode("#2E3440"));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize Look and Feel. Using default.");
        }

        // Test database connection
        System.out.println("Testing database connection...");
        if (DBConnection.getInstance().testConnection()) {
            System.out.println("✓ Database connection successful!");
        } else {
            System.err.println("✗ Database connection failed. Please check your configuration.");
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to database.\nPlease check your database configuration.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Launch application on EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    /**
     * Load theme preference from config
     */
    private static String loadTheme() {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties config = new Properties();
            if (input != null) {
                config.load(input);
                return config.getProperty("app.theme", "FlatLaf Dark");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FlatLaf Dark";
    }

    /**
     * Apply theme based on name
     */
    private static void applyTheme(String themeName) throws Exception {
        switch (themeName) {
            case "FlatLaf Light":
                FlatLightLaf.setup();
                break;
            case "FlatLaf Dark":
                FlatDarkLaf.setup();
                break;
            case "Arc Dark":
                FlatArcDarkIJTheme.setup();
                break;
            case "One Dark":
                FlatOneDarkIJTheme.setup();
                break;
            default:
                FlatDarkLaf.setup();
        }
    }
}
