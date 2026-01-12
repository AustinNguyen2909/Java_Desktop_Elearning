package com.elearning.ui.user;

import com.elearning.util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * User (Student) dashboard - placeholder for now
 */
public class UserDashboard extends JFrame {
    
    public UserDashboard() {
        initComponents();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 204, 113));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("  Student Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        // Content
        JLabel contentLabel = new JLabel("Student Dashboard - Coming Soon", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));

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
