package com.elearning.ui.components;

import com.elearning.model.AnswerOption;
import com.elearning.model.CourseTest;
import com.elearning.model.TestQuestion;
import com.elearning.service.TestService;
import com.elearning.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for managing test questions and answers
 */
public class QuestionManagementDialog extends JDialog {
    private final CourseTest test;
    private final TestService testService;
    private final int currentUserId;

    // Components
    private JTable questionsTable;
    private DefaultTableModel questionsTableModel;
    private JButton addQuestionButton;
    private JButton publishButton;
    private JLabel statusLabel;

    public QuestionManagementDialog(Dialog owner, CourseTest test) {
        super(owner, "Manage Questions - " + test.getTitle(), true);
        this.test = test;
        this.testService = TestService.getInstance();
        this.currentUserId = SessionManager.getInstance().getCurrentUser().getId();

        initComponents();
        loadQuestions();
        updateUI();
        
        setSize(1000, 700);
        setLocationRelativeTo(owner);
    }

    public QuestionManagementDialog(Frame owner, CourseTest test) {
        super(owner, "Manage Questions - " + test.getTitle(), true);
        this.test = test;
        this.testService = TestService.getInstance();
        this.currentUserId = SessionManager.getInstance().getCurrentUser().getId();

        initComponents();
        loadQuestions();
        updateUI();
        
        setSize(1000, 700);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Questions table
        createQuestionsTable();
        JScrollPane tableScrollPane = new JScrollPane(questionsTable);
        tableScrollPane.setBackground(Color.WHITE);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Bottom panel with close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(Color.WHITE);
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setPreferredSize(new Dimension(100, 40));
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Manage Test Questions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel testTitleLabel = new JLabel("Test: " + test.getTitle());
        testTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        testTitleLabel.setForeground(UITheme.MUTED_TEXT);
        testTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(testTitleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(statusLabel);

        // Publish button
        publishButton = new JButton();
        publishButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        publishButton.setFocusPainted(false);
        publishButton.setBorderPainted(false);
        publishButton.setPreferredSize(new Dimension(150, 40));
        publishButton.addActionListener(this::togglePublish);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(publishButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void createQuestionsTable() {
        String[] columns = {"#", "Question", "Points", "Options", "Actions"};
        questionsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Actions column
            }
        };

        questionsTable = new JTable(questionsTableModel);
        questionsTable.setBackground(Color.WHITE);
        questionsTable.setRowHeight(40);
        questionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        questionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        questionsTable.getTableHeader().setBackground(UITheme.BACKGROUND);

        // Set column widths
        questionsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        questionsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        questionsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        questionsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        questionsTable.getColumnModel().getColumn(2).setMaxWidth(80);
        questionsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        questionsTable.getColumnModel().getColumn(3).setMaxWidth(100);
        questionsTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        questionsTable.getColumnModel().getColumn(4).setMaxWidth(120);

        // Actions column renderer and editor
        questionsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        questionsTable.getColumn("Actions").setCellEditor(new QuestionActionEditor(new JCheckBox()));
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        addQuestionButton = new JButton("Add Question");
        addQuestionButton.setBackground(UITheme.PRIMARY);
        addQuestionButton.setForeground(Color.WHITE);
        addQuestionButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addQuestionButton.setFocusPainted(false);
        addQuestionButton.setBorderPainted(false);
        addQuestionButton.setPreferredSize(new Dimension(130, 40));
        addQuestionButton.addActionListener(e -> showAddQuestionDialog());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(148, 163, 184));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 40));
        refreshButton.addActionListener(e -> {
            loadQuestions();
            updateUI();
        });

        buttonPanel.add(addQuestionButton);
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private void loadQuestions() {
        questionsTableModel.setRowCount(0);
        try {
            List<TestQuestion> questions = testService.getQuestions(test.getId());
            for (TestQuestion question : questions) {
                questionsTableModel.addRow(new Object[]{
                    question.getOrderIndex(),
                    truncateText(question.getQuestionText(), 60),
                    question.getPoints(),
                    question.getOptions() != null ? question.getOptions().size() : 0,
                    "Actions"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading questions: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUI() {
        // Update status label
        List<TestQuestion> questions = testService.getQuestions(test.getId());
        boolean isValid = testService.validateTest(test.getId());
        
        String status = test.getIsPublished() ? "Published" : "Draft";
        Color statusColor = test.getIsPublished() ? new Color(34, 197, 94) : new Color(251, 146, 60);
        
        statusLabel.setText(String.format("Status: %s | Questions: %d | Valid: %s", 
            status, questions.size(), isValid ? "Yes" : "No"));
        statusLabel.setForeground(statusColor);

        // Update publish button
        if (test.getIsPublished()) {
            publishButton.setText("Unpublish Test");
            publishButton.setBackground(new Color(251, 146, 60));
        } else {
            publishButton.setText("Publish Test");
            publishButton.setBackground(new Color(34, 197, 94));
            publishButton.setEnabled(isValid);
        }
        publishButton.setForeground(Color.WHITE);

        // Update add question button state
        addQuestionButton.setEnabled(!test.getIsPublished());
    }

    private void showAddQuestionDialog() {
        QuestionEditDialog dialog = new QuestionEditDialog(this, test.getId(), null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadQuestions();
            updateUI();
        }
    }

    private void showEditQuestionDialog(int row) {
        try {
            List<TestQuestion> questions = testService.getQuestions(test.getId());
            if (row < questions.size()) {
                TestQuestion question = questions.get(row);
                QuestionEditDialog dialog = new QuestionEditDialog(this, test.getId(), question);
                dialog.setVisible(true);
                if (dialog.isSuccess()) {
                    loadQuestions();
                    updateUI();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading question: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteQuestion(int row) {
        try {
            List<TestQuestion> questions = testService.getQuestions(test.getId());
            if (row < questions.size()) {
                TestQuestion question = questions.get(row);
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this question?\n\n" +
                    "Question: " + truncateText(question.getQuestionText(), 80) + "\n\n" +
                    "This will also delete all answer options.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = testService.deleteQuestion(question.getId(), currentUserId);
                    if (success) {
                        loadQuestions();
                        updateUI();
                        JOptionPane.showMessageDialog(this,
                            "Question deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete question.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting question: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void togglePublish(ActionEvent e) {
        try {
            boolean success;
            if (test.getIsPublished()) {
                success = testService.unpublishTest(test.getId(), currentUserId);
            } else {
                success = testService.publishTest(test.getId(), currentUserId);
            }

            if (success) {
                test.setIsPublished(!test.getIsPublished());
                updateUI();
                JOptionPane.showMessageDialog(this,
                    "Test " + (test.getIsPublished() ? "published" : "unpublished") + " successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to " + (test.getIsPublished() ? "unpublish" : "publish") + " test.",
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

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    // Button renderer for Actions column
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Actions");
            setBackground(UITheme.PRIMARY);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            return this;
        }
    }

    // Action editor for questions table
    class QuestionActionEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;

        public QuestionActionEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Actions");
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showQuestionActionsMenu(currentRow));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText("Actions");
            button.setBackground(UITheme.PRIMARY);
            button.setForeground(Color.WHITE);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }

        private void showQuestionActionsMenu(int row) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem editItem = new JMenuItem("Edit Question");
            editItem.addActionListener(e -> showEditQuestionDialog(row));
            menu.add(editItem);

            if (!test.getIsPublished()) {
                JMenuItem deleteItem = new JMenuItem("Delete Question");
                deleteItem.setForeground(new Color(239, 68, 68));
                deleteItem.addActionListener(e -> deleteQuestion(row));
                menu.add(deleteItem);
            }

            // Show menu at button location
            Rectangle cellRect = questionsTable.getCellRect(row, questionsTable.getColumnCount() - 1, true);
            menu.show(questionsTable, cellRect.x, cellRect.y + cellRect.height);
        }
    }
}

/**
 * Dialog for adding/editing individual questions
 */
class QuestionEditDialog extends JDialog {
    private final int testId;
    private final TestQuestion existingQuestion;
    private final TestService testService;
    private final int currentUserId;
    private boolean success = false;

    // Form components
    private JTextArea questionTextArea;
    private JSpinner pointsSpinner;
    private JTextField[] optionFields = new JTextField[4];
    private JRadioButton[] correctButtons = new JRadioButton[4];
    private ButtonGroup correctGroup;

    public QuestionEditDialog(Dialog owner, int testId, TestQuestion existingQuestion) {
        super(owner, existingQuestion == null ? "Add Question" : "Edit Question", true);
        this.testId = testId;
        this.existingQuestion = existingQuestion;
        this.testService = TestService.getInstance();
        this.currentUserId = SessionManager.getInstance().getCurrentUser().getId();

        initComponents();
        if (existingQuestion != null) {
            populateFields();
        }

        setSize(700, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel(existingQuestion == null ? "Add New Question" : "Edit Question");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.TEXT);

        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Main form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        // Question text
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel questionLabel = new JLabel("Question Text:");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(questionLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        questionTextArea = new JTextArea(4, 40);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        questionTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane questionScrollPane = new JScrollPane(questionTextArea);
        questionScrollPane.setPreferredSize(new Dimension(0, 100));
        formPanel.add(questionScrollPane, gbc);

        row++;

        // Points
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel pointsLabel = new JLabel("Points:");
        pointsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(pointsLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        pointsSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
        pointsSpinner.setPreferredSize(new Dimension(0, 35));
        formPanel.add(pointsSpinner, gbc);

        row++;

        // Answer options
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel optionsLabel = new JLabel("Answer Options (select the correct one):");
        optionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(optionsLabel, gbc);

        correctGroup = new ButtonGroup();
        String[] letters = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            
            JPanel optionPanel = new JPanel(new BorderLayout(10, 0));
            optionPanel.setOpaque(false);
            
            correctButtons[i] = new JRadioButton();
            correctButtons[i].setOpaque(false);
            correctGroup.add(correctButtons[i]);
            
            JLabel letterLabel = new JLabel(letters[i] + ".");
            letterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            letterLabel.setPreferredSize(new Dimension(20, 35));
            
            optionPanel.add(correctButtons[i], BorderLayout.WEST);
            optionPanel.add(letterLabel, BorderLayout.CENTER);
            
            formPanel.add(optionPanel, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
            optionFields[i] = new JTextField();
            optionFields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            optionFields[i].setPreferredSize(new Dimension(0, 35));
            formPanel.add(optionFields[i], gbc);
        }

        // Set default correct answer to A
        if (existingQuestion == null) {
            correctButtons[0].setSelected(true);
        }

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

        JButton saveButton = new JButton(existingQuestion == null ? "Add Question" : "Save Changes");
        saveButton.setBackground(UITheme.PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setPreferredSize(new Dimension(existingQuestion == null ? 130 : 140, 40));
        saveButton.addActionListener(this::saveQuestion);

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        questionTextArea.setText(existingQuestion.getQuestionText());
        pointsSpinner.setValue(existingQuestion.getPoints());

        List<AnswerOption> options = existingQuestion.getOptions();
        if (options != null && options.size() == 4) {
            for (int i = 0; i < 4; i++) {
                AnswerOption option = options.get(i);
                optionFields[i].setText(option.getOptionText());
                if (option.getIsCorrect()) {
                    correctButtons[i].setSelected(true);
                }
            }
        }
    }

    private void saveQuestion(ActionEvent e) {
        // Validate input
        String questionText = questionTextArea.getText().trim();
        if (questionText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter the question text.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            questionTextArea.requestFocus();
            return;
        }

        // Validate options
        List<String> optionTexts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String optionText = optionFields[i].getText().trim();
            if (optionText.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill in all answer options.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                optionFields[i].requestFocus();
                return;
            }
            optionTexts.add(optionText);
        }

        // Validate correct answer selection
        int correctIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (correctButtons[i].isSelected()) {
                correctIndex = i;
                break;
            }
        }

        if (correctIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select the correct answer.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Create or update question
            TestQuestion question = existingQuestion != null ? existingQuestion : new TestQuestion();
            question.setTestId(testId);
            question.setQuestionText(questionText);
            question.setPoints((Double) pointsSpinner.getValue());

            TestQuestion savedQuestion;
            if (existingQuestion == null) {
                savedQuestion = testService.addQuestion(question, currentUserId);
            } else {
                // For updates, we need to handle this through the service
                // This is a simplified approach - in a real app you'd have updateQuestion method
                savedQuestion = question;
                // Update question logic would go here
            }

            if (savedQuestion != null && savedQuestion.getId() > 0) {
                // Create answer options
                List<AnswerOption> options = new ArrayList<>();
                String[] letters = {"A", "B", "C", "D"};
                
                for (int i = 0; i < 4; i++) {
                    AnswerOption option = new AnswerOption();
                    option.setQuestionId(savedQuestion.getId());
                    option.setOptionText(optionTexts.get(i));
                    option.setOptionLetter(letters[i]);
                    option.setIsCorrect(i == correctIndex);
                    options.add(option);
                }

                List<AnswerOption> savedOptions = testService.addOptions(savedQuestion.getId(), options, currentUserId);
                if (savedOptions != null && savedOptions.size() == 4) {
                    this.success = true;
                    JOptionPane.showMessageDialog(this,
                        "Question " + (existingQuestion == null ? "added" : "updated") + " successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Question saved but failed to save answer options.",
                        "Partial Success",
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to save question. Please try again.",
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