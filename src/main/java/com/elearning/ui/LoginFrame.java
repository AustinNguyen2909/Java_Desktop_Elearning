package com.elearning.ui;

import com.elearning.model.User;
import com.elearning.service.AuthService;
import com.elearning.ui.admin.AdminDashboard;
import com.elearning.ui.components.CardPanel;
import com.elearning.ui.components.ModernButton;
import com.elearning.ui.components.ModernPasswordField;
import com.elearning.ui.components.ModernTextField;
import com.elearning.ui.components.UITheme;
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
        authService = AuthService.getInstance();
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
        usernameField.setPreferredSize(new Dimension(350, 45));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new ModernPasswordField("Password");
        passwordField.setPreferredSize(new Dimension(350, 45));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton = new ModernButton("Login");
        loginButton.setBackground(UITheme.PRIMARY);
        loginButton.setPreferredSize(new Dimension(350, 45));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerButton = new ModernButton("Register");
        registerButton.setBackground(UITheme.ACCENT);
        registerButton.setPreferredSize(new Dimension(350, 45));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
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
                        0, 0, UITheme.BACKGROUND,
                        0, getHeight(), new Color(224, 248, 252)
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
        JLabel iconLabel = new JLabel("EL");
        iconLabel.setFont(new Font("Fira Sans", Font.BOLD, 72));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setForeground(UITheme.PRIMARY);

        // App Title
        JLabel titleLabel = new JLabel("E-Learning Platform");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 34));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UITheme.TEXT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Learn Anytime, Anywhere");
        subtitleLabel.setFont(new Font("Fira Sans", Font.PLAIN, 16));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(UITheme.MUTED_TEXT);

        // Features list
        JPanel featuresPanel = new JPanel();
        featuresPanel.setOpaque(false);
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        String[] features = {
                "\u2713 Interactive video lessons",
                "\u2713 Track your progress",
                "\u2713 Expert instructors",
                "\u2713 Certificate of completion"
        };

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Fira Sans", Font.PLAIN, 14));
            featureLabel.setForeground(UITheme.MUTED_TEXT);
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
        formTitle.setFont(new Font("Fira Sans", Font.BOLD, 26));
        formTitle.setForeground(UITheme.TEXT);
        formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel formSubtitle = new JLabel("Sign in to continue");
        formSubtitle.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        formSubtitle.setForeground(UITheme.MUTED_TEXT);
        formSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Fira Sans", Font.BOLD, 12));
        usernameLabel.setForeground(UITheme.TEXT);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Fira Sans", Font.BOLD, 12));
        passwordLabel.setForeground(UITheme.TEXT);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Forgot password link
        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(UITheme.PRIMARY);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JLabel dividerLabel = new JLabel("Don't have an account?");
        dividerLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        dividerLabel.setForeground(UITheme.MUTED_TEXT);
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
                        boolean opened = openDashboard(user);
                        if (opened) {
                            dispose();
                        }
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

    private boolean openDashboard(User user) {
        try {
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
                    return false;
            }
            dashboard.setVisible(true);
            return true;
        } catch (Exception ex) {
            showLaunchError(ex);
            return false;
        }
    }

    private void openRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void showLaunchError(Exception ex) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Failed to open dashboard");
        title.setFont(new Font("Fira Sans", Font.BOLD, 16));
        title.setForeground(UITheme.TEXT);

        JLabel body = new JLabel("<html><div style='width:280px;'>Login succeeded but the dashboard failed to open. " +
                "Please check the error details below.</div></html>");
        body.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        body.setForeground(UITheme.MUTED_TEXT);

        JTextArea details = new JTextArea(ex.getMessage() != null ? ex.getMessage() : ex.toString());
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

        JOptionPane.showMessageDialog(this, panel, "Launch Error", JOptionPane.ERROR_MESSAGE);
    }
}

