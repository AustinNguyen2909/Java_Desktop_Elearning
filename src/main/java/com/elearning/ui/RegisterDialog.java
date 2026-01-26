package com.elearning.ui;

import com.elearning.model.User;
import com.elearning.service.AuthService;
import com.elearning.ui.components.ModernButton;
import com.elearning.ui.components.ModernPasswordField;
import com.elearning.ui.components.ModernTextField;
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
        authService = new AuthService();
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setSize(500, 650);
        setLocationRelativeTo(getParent());
        setResizable(false);

        usernameField = new ModernTextField("Username (3-20 characters)");
        emailField = new ModernTextField("Email address");
        fullNameField = new ModernTextField("Full name");
        passwordField = new ModernPasswordField("Password (min 6 characters)");
        confirmPasswordField = new ModernPasswordField("Confirm password");

        String[] roles = {"USER", "INSTRUCTOR"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setForeground(new Color(33, 33, 33));
        roleComboBox.setPreferredSize(new Dimension(300, 45));

        registerButton = new ModernButton("Register");
        registerButton.setBackground(new Color(46, 204, 113));

        cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(149, 165, 166));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(231, 76, 60));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join our learning community");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(80, 80, 80)); // Darker gray for better contrast
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
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createFieldLabel("Confirm Password"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(33, 33, 33)); // Dark text
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
                        JOptionPane.showMessageDialog(RegisterDialog.this,
                                "Account created successfully!\nYou can now login.",
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
