package com.elearning.ui;

import com.elearning.model.User;
import com.elearning.service.AuthService;
import com.elearning.ui.components.ModernButton;
import com.elearning.ui.components.ModernPasswordField;
import com.elearning.ui.components.ModernTextField;
import com.elearning.ui.components.UITheme;
import com.elearning.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration dialog for new users
 */
public class RegisterDialog extends JDialog {
    private ModernTextField usernameField;
    private ModernTextField emailField;
    private ModernTextField fullNameField;
    private ModernPasswordField passwordField;
    private ModernPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private ModernButton registerButton;
    private ModernButton cancelButton;
    private JLabel errorLabel;
    private AuthService authService;

    public RegisterDialog(JFrame parent) {
        super(parent, "Register New Account", true);
        authService = AuthService.getInstance();
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setSize(500, 700);
        setLocationRelativeTo(getParent());
        setResizable(false);

        usernameField = new ModernTextField("Username (3-20 characters)");
        emailField = new ModernTextField("Email address");
        fullNameField = new ModernTextField("Full name");
        passwordField = new ModernPasswordField("Password (min 6 characters)");
        confirmPasswordField = new ModernPasswordField("Confirm password");

        String[] roles = {"USER", "INSTRUCTOR"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        roleComboBox.setBackground(UITheme.SURFACE);
        roleComboBox.setForeground(UITheme.TEXT);
        roleComboBox.setPreferredSize(new Dimension(300, 45));

        registerButton = new ModernButton("Register");
        registerButton.setBackground(UITheme.ACCENT);

        cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(148, 163, 184));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JPanel createPasswordFieldWithToggle(ModernPasswordField field) {
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BorderLayout());
        passwordPanel.setBackground(UITheme.SURFACE);
        passwordPanel.setMaximumSize(new Dimension(300, 45));
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Adjust password field padding
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 45)
        ));

        // Toggle button with eye icon
        JButton toggleButton = new JButton("Show");
        toggleButton.setFont(new Font("Fira Sans", Font.PLAIN, 11));
        toggleButton.setPreferredSize(new Dimension(60, 45));
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setForeground(UITheme.MUTED_TEXT);

        toggleButton.addActionListener(e -> {
            if (field.getEchoChar() == '\u0000') {
                // Hide password
                field.setEchoChar("\u2022".charAt(0));
                toggleButton.setText("Hide");
                toggleButton.setForeground(UITheme.MUTED_TEXT);
            } else {
                // Show password
                field.setEchoChar("\u2022".charAt(0));
                toggleButton.setText("Hide");
                toggleButton.setForeground(UITheme.PRIMARY);
            }
        });

        passwordPanel.add(field, BorderLayout.CENTER);
        passwordPanel.add(toggleButton, BorderLayout.EAST);

        return passwordPanel;
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join our learning community");
        subtitleLabel.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        subtitleLabel.setForeground(UITheme.MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        mainPanel.add(createFieldLabel("Username"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createFieldLabel("Email"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createFieldLabel("Full Name"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(fullNameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createFieldLabel("Role"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(roleComboBox);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createFieldLabel("Password"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(createPasswordFieldWithToggle(passwordField));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createFieldLabel("Confirm Password"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(createPasswordFieldWithToggle(confirmPasswordField));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(UITheme.BACKGROUND);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Fira Sans", Font.BOLD, 12));
        label.setForeground(UITheme.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void setupListeners() {
        registerButton.addActionListener(e -> performRegistration());
        cancelButton.addActionListener(e -> dispose());
    }

    private void performRegistration() {
        // Validate inputs
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (!ValidationUtil.isValidUsername(username)) {
            showError("Username must be 3-20 alphanumeric characters");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            showError("Invalid email format");
            return;
        }

        if (ValidationUtil.isEmpty(fullName)) {
            showError("Full name is required");
            return;
        }

        if (!ValidationUtil.hasMinLength(password, 6)) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Create user object
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRole(role);

        // Show loading state
        registerButton.setEnabled(false);
        registerButton.setText("Creating account...");
        errorLabel.setText(" ");

        // Perform registration in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return authService.register(newUser, password);
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        JLabel messageLabel = new JLabel("Account created successfully!\nYou can now login.");
                        messageLabel.setForeground(Color.WHITE);
                        JOptionPane.showMessageDialog(RegisterDialog.this,
                                messageLabel,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        showError("Registration failed. Username or email may already exist.");
                    }
                } catch (Exception ex) {
                    showError("Registration error: " + ex.getMessage());
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}

