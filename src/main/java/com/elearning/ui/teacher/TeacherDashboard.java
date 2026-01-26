package com.elearning.ui.teacher;

import com.elearning.util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Teacher dashboard - placeholder for now
 */
public class TeacherDashboard extends JFrame {
    
    public TeacherDashboard() {
        initComponents();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Teacher Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE); // Light background

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(129, 212, 250)); // Light cyan blue
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("  Teacher Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        // Content
        JLabel contentLabel = new JLabel("Teacher Dashboard - Coming Soon", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        contentLabel.setForeground(new Color(33, 33, 33)); // Dark text

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentLabel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }

    private void logout() {
        SessionManager.getInstance().logout();
        dispose();
        // Return to login
        SwingUtilities.invokeLater(() -> {
            com.elearning.ui.LoginFrame loginFrame = new com.elearning.ui.LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
