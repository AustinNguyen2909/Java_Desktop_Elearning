package com.elearning.ui;

import com.elearning.model.User;
import com.elearning.service.AuthService;
import com.elearning.ui.admin.AdminDashboard;
import com.elearning.ui.components.CardPanel;
import com.elearning.ui.components.ModernButton;
import com.elearning.ui.components.ModernPasswordField;
import com.elearning.ui.components.ModernTextField;
import com.elearning.ui.instructor.InstructorDashboard;
import com.elearning.ui.user.UserDashboard;
import com.elearning.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern login screen with gradient background
 */
public class LoginFrame extends JFrame {
    private ModernTextField usernameField;
    private ModernPasswordField passwordField;
    private ModernButton loginButton;
    private ModernButton registerButton;
    private JLabel errorLabel;
    private AuthService authService;

    public LoginFrame() {
        authService = new AuthService();
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create components
        usernameField = new ModernTextField("Username");
        passwordField = new ModernPasswordField("Password");
        
        loginButton = new ModernButton("Login");
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setPreferredSize(new Dimension(300, 45));
        
        registerButton = new ModernButton("Register");
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setPreferredSize(new Dimension(300, 45));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(231, 76, 60));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void setupLayout() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(240, 248, 255), // Light alice blue
                        0, getHeight(), new Color(224, 242, 254) // Light blue
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Left side - Branding
        JPanel leftPanel = createBrandingPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 20, 20, 10);
        mainPanel.add(leftPanel, gbc);

        // Right side - Login form
        JPanel rightPanel = createLoginPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(20, 10, 20, 20);
        mainPanel.add(rightPanel, gbc);

        setContentPane(mainPanel);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // App Icon/Logo
        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setForeground(new Color(33, 150, 243)); // Material blue for light background

        // App Title
        JLabel titleLabel = new JLabel("E-Learning Platform");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        // Subtitle
        JLabel subtitleLabel = new JLabel("Learn Anytime, Anywhere");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(new Color(66, 66, 66)); // Dark gray for light background

        // Features list
        JPanel featuresPanel = new JPanel();
        featuresPanel.setOpaque(false);
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        String[] features = {
                "✓ Interactive video lessons",
                "✓ Track your progress",
                "✓ Expert instructors",
                "✓ Certificate of completion"
        };

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            featureLabel.setForeground(new Color(88, 88, 88)); // Medium gray for light background
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            featureLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
            featuresPanel.add(featureLabel);
        }

        panel.add(Box.createVerticalGlue());
        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(subtitleLabel);
        panel.add(featuresPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createLoginPanel() {
        CardPanel cardPanel = new CardPanel();
        cardPanel.setPreferredSize(new Dimension(400, 500));
        cardPanel.setMaximumSize(new Dimension(400, 500));

        // Title
        JLabel formTitle = new JLabel("Welcome Back!");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        formTitle.setForeground(new Color(33, 33, 33)); // Dark text
        formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel formSubtitle = new JLabel("Sign in to continue");
        formSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formSubtitle.setForeground(new Color(80, 80, 80)); // Darker gray for better contrast
        formSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        usernameLabel.setForeground(new Color(33, 33, 33)); // Dark text
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passwordLabel.setForeground(new Color(33, 33, 33)); // Dark text
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Forgot password link
        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(new Color(52, 152, 219));
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JLabel dividerLabel = new JLabel("Don't have an account?");
        dividerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dividerLabel.setForeground(new Color(80, 80, 80)); // Darker gray for better contrast
        dividerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Layout
        cardPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        cardPanel.add(formTitle);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(formSubtitle);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        cardPanel.add(usernameLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        cardPanel.add(usernameField);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        cardPanel.add(passwordLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        cardPanel.add(passwordField);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        cardPanel.add(forgotPasswordLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        cardPanel.add(errorLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        cardPanel.add(loginButton);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        cardPanel.add(dividerLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        cardPanel.add(registerButton);
        cardPanel.add(Box.createVerticalGlue());

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(cardPanel);

        return wrapper;
    }

    private void setupListeners() {
        // Login button action
        loginButton.addActionListener(e -> performLogin());

        // Register button action
        registerButton.addActionListener(e -> openRegisterDialog());

        // Enter key on password field
        passwordField.addActionListener(e -> performLogin());

        // Enter key on username field
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        errorLabel.setText(" ");

        // Perform authentication in background
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        SessionManager.getInstance().login(user);
                        openDashboard(user);
                        dispose();
                    } else {
                        showError("Invalid username or password");
                    }
                } catch (Exception ex) {
                    showError("Login failed: " + ex.getMessage());
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        worker.execute();
    }

    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            JFrame dashboard;
            switch (user.getRole()) {
                case "ADMIN":
                    dashboard = new AdminDashboard();
                    break;
                case "INSTRUCTOR":
                    dashboard = new InstructorDashboard();
                    break;
                case "USER":
                    dashboard = new UserDashboard();
                    break;
                default:
                    showError("Unknown user role");
                    return;
            }
            dashboard.setVisible(true);
        });
    }

    private void openRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
