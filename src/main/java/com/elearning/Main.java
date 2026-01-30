package com.elearning;

import com.elearning.ui.LoginFrame;
import com.elearning.ui.components.UITheme;
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
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            SwingUtilities.invokeLater(() -> showUnexpectedError(throwable));
        });

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
            UITheme.applyDefaults();
            UIManager.put("TabbedPane.selectedBackground", UITheme.SECONDARY);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize Look and Feel. Using default.");
        }

        // Test database connection
        System.out.println("Testing database connection...");
        if (DBConnection.getInstance().testConnection()) {
            System.out.println("\u2713 Database connection successful!");
            String ddlError = DBConnection.getInstance().testDDL();
            if (ddlError != null) {
                showDatabaseDDLError(ddlError);
            }
        } else {
            System.err.println("\u2717 Database connection failed. Please check your configuration.");
            showDatabaseConnectionError();
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
                return config.getProperty("app.theme", "FlatLaf Light");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FlatLaf Light";
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
                FlatLightLaf.setup();
        }
    }

    private static void showDatabaseDDLError(String ddlError) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Database DDL check failed");
        title.setFont(new Font("Fira Sans", Font.BOLD, 16));
        title.setForeground(UITheme.TEXT);

        JLabel subtitle = new JLabel("<html>This usually happens when MariaDB cannot write <b>ddl_recovery.log</b>.</html>");
        subtitle.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        subtitle.setForeground(UITheme.MUTED_TEXT);

        JTextArea steps = new JTextArea(
                "Fix (one-time):\n" +
                "1) Edit: C:\\ProgramData\\MariaDB\\my.ini\n" +
                "2) Add under [mysqld]:\n" +
                "   log-ddl-recovery=D:/MariaDB/data/ddl_recovery.log\n" +
                "3) Restart service:\n" +
                "   sc stop MariaDB\n" +
                "   sc start MariaDB\n"
        );
        steps.setEditable(false);
        steps.setFont(new Font("Consolas", Font.PLAIN, 12));
        steps.setForeground(UITheme.TEXT);
        steps.setBackground(new Color(241, 245, 249));
        steps.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextArea errorArea = new JTextArea(ddlError);
        errorArea.setEditable(false);
        errorArea.setLineWrap(true);
        errorArea.setWrapStyleWord(true);
        errorArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        errorArea.setForeground(new Color(185, 28, 28));
        errorArea.setBackground(new Color(254, 242, 242));
        errorArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createRigidArea(new Dimension(0, 6)));
        top.add(subtitle);

        panel.add(top, BorderLayout.NORTH);
        panel.add(steps, BorderLayout.CENTER);
        panel.add(errorArea, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(null, panel, "Database DDL Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showDatabaseConnectionError() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Database connection failed");
        title.setFont(new Font("Fira Sans", Font.BOLD, 16));
        title.setForeground(UITheme.TEXT);

        JLabel body = new JLabel("<html><div style='width:280px;'>Unable to connect to the database. " +
                "Please check host/port, service status, and credentials.</div></html>");
        body.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        body.setForeground(UITheme.MUTED_TEXT);

        JTextArea steps = new JTextArea(
                "Quick checks:\n" +
                "• Service: sc query MariaDB\n" +
                "• Port: 3306 listening\n" +
                "• Config: src/main/resources/config.properties\n"
        );
        steps.setEditable(false);
        steps.setFont(new Font("Consolas", Font.PLAIN, 12));
        steps.setForeground(UITheme.TEXT);
        steps.setBackground(new Color(241, 245, 249));
        steps.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panel.add(title, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        panel.add(steps, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(null, panel, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showUnexpectedError(Throwable throwable) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Unexpected Error");
        title.setFont(new Font("Fira Sans", Font.BOLD, 16));
        title.setForeground(UITheme.TEXT);

        JLabel body = new JLabel("<html><div style='width:280px;'>The application encountered an unexpected error. " +
                "Please check details below.</div></html>");
        body.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        body.setForeground(UITheme.MUTED_TEXT);

        JTextArea details = new JTextArea(throwable != null ? throwable.toString() : "Unknown error");
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("Consolas", Font.PLAIN, 11));
        details.setForeground(UITheme.DANGER);
        details.setBackground(new Color(254, 242, 242));
        details.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panel.add(title, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        panel.add(details, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(null, panel, "Unexpected Error", JOptionPane.ERROR_MESSAGE);
    }
}
