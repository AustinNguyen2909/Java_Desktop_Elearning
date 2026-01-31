package com.elearning.ui.components;

import com.elearning.model.CourseTest;
import com.elearning.service.TestService;
import com.elearning.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for creating and editing course tests
 */
public class TestCreationDialog extends JDialog {
    private final int courseId;
    private final CourseTest existingTest; // null for creation, populated for editing
    private final TestService testService;
    private boolean success = false;

    // Form components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner passingScoreSpinner;
    private JSpinner timeLimitSpinner;
    private JCheckBox noTimeLimitCheckBox;
    private JCheckBox shuffleQuestionsCheckBox;
    private JCheckBox shuffleOptionsCheckBox;
    private JSpinner maxAttemptsSpinner;
    private JCheckBox unlimitedAttemptsCheckBox;

    public TestCreationDialog(Dialog owner, int courseId, CourseTest existingTest) {
        super(owner, existingTest == null ? "Create Test" : "Edit Test", true);
        this.courseId = courseId;
        this.existingTest = existingTest;
        this.testService = TestService.getInstance();

        initComponents();
        if (existingTest != null) {
            populateFields();
        }
        
        setSize(600, 700);
        setLocationRelativeTo(owner);
    }

    public TestCreationDialog(Frame owner, int courseId, CourseTest existingTest) {
        super(owner, existingTest == null ? "Create Test" : "Edit Test", true);
        this.courseId = courseId;
        this.existingTest = existingTest;
        this.testService = TestService.getInstance();

        initComponents();
        if (existingTest != null) {
            populateFields();
        }
        
        setSize(600, 700);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel(existingTest == null ? "Create New Test" : "Edit Test");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.TEXT);

        JLabel subtitleLabel = new JLabel("Configure test settings and behavior");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(UITheme.MUTED_TEXT);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        // Test Title
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel titleLbl = new JLabel("Test Title:");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(UITheme.TEXT);
        formPanel.add(titleLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        titleField = new JTextField();
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(titleField, gbc);

        row++;

        // Description
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel descLbl = new JLabel("Description:");
        descLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descLbl.setForeground(UITheme.TEXT);
        formPanel.add(descLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(0, 100));
        formPanel.add(descScrollPane, gbc);

        row++;

        // Passing Score
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel passLbl = new JLabel("Passing Score (%):");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLbl.setForeground(UITheme.TEXT);
        formPanel.add(passLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        passingScoreSpinner = new JSpinner(new SpinnerNumberModel(80.0, 0.0, 100.0, 1.0));
        passingScoreSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passingScoreSpinner.setPreferredSize(new Dimension(0, 35));
        formPanel.add(passingScoreSpinner, gbc);

        row++;

        // Time Limit
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel timeLbl = new JLabel("Time Limit (minutes):");
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timeLbl.setForeground(UITheme.TEXT);
        formPanel.add(timeLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timePanel.setOpaque(false);

        timeLimitSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 300, 5));
        timeLimitSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timeLimitSpinner.setPreferredSize(new Dimension(100, 35));

        noTimeLimitCheckBox = new JCheckBox("No time limit");
        noTimeLimitCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noTimeLimitCheckBox.setOpaque(false);
        noTimeLimitCheckBox.addActionListener(e -> {
            timeLimitSpinner.setEnabled(!noTimeLimitCheckBox.isSelected());
        });

        timePanel.add(timeLimitSpinner);
        timePanel.add(Box.createRigidArea(new Dimension(15, 0)));
        timePanel.add(noTimeLimitCheckBox);
        formPanel.add(timePanel, gbc);

        row++;

        // Max Attempts
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel attemptsLbl = new JLabel("Max Attempts:");
        attemptsLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        attemptsLbl.setForeground(UITheme.TEXT);
        formPanel.add(attemptsLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel attemptsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        attemptsPanel.setOpaque(false);

        maxAttemptsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        maxAttemptsSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        maxAttemptsSpinner.setPreferredSize(new Dimension(100, 35));

        unlimitedAttemptsCheckBox = new JCheckBox("Unlimited attempts");
        unlimitedAttemptsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        unlimitedAttemptsCheckBox.setOpaque(false);
        unlimitedAttemptsCheckBox.addActionListener(e -> {
            maxAttemptsSpinner.setEnabled(!unlimitedAttemptsCheckBox.isSelected());
        });

        attemptsPanel.add(maxAttemptsSpinner);
        attemptsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        attemptsPanel.add(unlimitedAttemptsCheckBox);
        formPanel.add(attemptsPanel, gbc);

        row++;

        // Randomization Options
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel randomLbl = new JLabel("Randomization:");
        randomLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        randomLbl.setForeground(UITheme.TEXT);
        formPanel.add(randomLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel randomPanel = new JPanel();
        randomPanel.setLayout(new BoxLayout(randomPanel, BoxLayout.Y_AXIS));
        randomPanel.setOpaque(false);

        shuffleQuestionsCheckBox = new JCheckBox("Shuffle question order");
        shuffleQuestionsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        shuffleQuestionsCheckBox.setOpaque(false);

        shuffleOptionsCheckBox = new JCheckBox("Shuffle answer options");
        shuffleOptionsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        shuffleOptionsCheckBox.setOpaque(false);

        randomPanel.add(shuffleQuestionsCheckBox);
        randomPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        randomPanel.add(shuffleOptionsCheckBox);
        formPanel.add(randomPanel, gbc);

        // Add info panel
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel infoPanel = createInfoPanel();
        formPanel.add(infoPanel, gbc);

        // Scroll pane for form
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBackground(Color.WHITE);
        formScrollPane.getViewport().setBackground(Color.WHITE);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton(existingTest == null ? "Create Test" : "Save Changes");
        saveButton.setBackground(UITheme.PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setPreferredSize(new Dimension(existingTest == null ? 120 : 140, 40));
        saveButton.addActionListener(this::saveTest);

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(240, 249, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel infoIcon = new JLabel("\u2139");  // Info icon
        infoIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        infoIcon.setForeground(new Color(37, 99, 235));
        infoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoTitle = new JLabel("Test Configuration Tips");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoTitle.setForeground(UITheme.TEXT);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoText = new JLabel("<html>" +
            "• After creating the test, you'll be able to add questions<br/>" +
            "• Each question must have exactly 4 answer options (A, B, C, D)<br/>" +
            "• Tests must be published for students to access them<br/>" +
            "• Students need to complete 100% of lessons before taking the test" +
            "</html>");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoText.setForeground(UITheme.MUTED_TEXT);
        infoText.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(infoIcon);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(infoText);

        return infoPanel;
    }

    private void populateFields() {
        titleField.setText(existingTest.getTitle());
        descriptionArea.setText(existingTest.getDescription() != null ? existingTest.getDescription() : "");
        passingScoreSpinner.setValue(existingTest.getPassingScore());
        
        if (existingTest.getTimeLimitMinutes() != null) {
            timeLimitSpinner.setValue(existingTest.getTimeLimitMinutes());
            noTimeLimitCheckBox.setSelected(false);
        } else {
            noTimeLimitCheckBox.setSelected(true);
            timeLimitSpinner.setEnabled(false);
        }
        
        if (existingTest.getMaxAttempts() != null) {
            maxAttemptsSpinner.setValue(existingTest.getMaxAttempts());
            unlimitedAttemptsCheckBox.setSelected(false);
        } else {
            unlimitedAttemptsCheckBox.setSelected(true);
            maxAttemptsSpinner.setEnabled(false);
        }
        
        shuffleQuestionsCheckBox.setSelected(existingTest.getShuffleQuestions());
        shuffleOptionsCheckBox.setSelected(existingTest.getShuffleOptions());
    }

    private void saveTest(ActionEvent e) {
        // Validate input
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a test title.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            titleField.requestFocus();
            return;
        }

        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a test description.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            descriptionArea.requestFocus();
            return;
        }

        try {
            // Create or update test object
            CourseTest test = existingTest != null ? existingTest : new CourseTest();
            test.setCourseId(courseId);
            test.setTitle(title);
            test.setDescription(description);
            test.setPassingScore((Double) passingScoreSpinner.getValue());
            
            // Set time limit
            if (noTimeLimitCheckBox.isSelected()) {
                test.setTimeLimitMinutes(null);
            } else {
                test.setTimeLimitMinutes((Integer) timeLimitSpinner.getValue());
            }
            
            // Set max attempts
            if (unlimitedAttemptsCheckBox.isSelected()) {
                test.setMaxAttempts(null);
            } else {
                test.setMaxAttempts((Integer) maxAttemptsSpinner.getValue());
            }
            
            test.setShuffleQuestions(shuffleQuestionsCheckBox.isSelected());
            test.setShuffleOptions(shuffleOptionsCheckBox.isSelected());

            boolean success;
            int currentUserId = SessionManager.getInstance().getCurrentUser().getId();
            
            if (existingTest == null) {
                // Create new test
                CourseTest createdTest = testService.createTest(test, currentUserId);
                success = createdTest != null;
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Test created successfully!\n\nNext steps:\n" +
                        "1. Add questions to your test\n" +
                        "2. Publish the test for students",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // Update existing test
                success = testService.updateTest(test, currentUserId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Test updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

            if (success) {
                this.success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to " + (existingTest == null ? "create" : "update") + " test. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}