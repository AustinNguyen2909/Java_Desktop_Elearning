package com.elearning.ui.components;

import com.elearning.dao.TestAnswerDAO;
import com.elearning.dao.TestAttemptDAO;
import com.elearning.model.CourseTest;
import com.elearning.model.TestAttempt;
import com.elearning.service.TestService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog for viewing test results and analytics
 */
public class TestResultsDialog extends JDialog {
    private final CourseTest test;
    private final TestAttemptDAO attemptDAO;
    private final TestAnswerDAO answerDAO;

    // Components
    private JTable attemptsTable;
    private DefaultTableModel attemptsTableModel;
    private JPanel statisticsPanel;

    public TestResultsDialog(Dialog owner, CourseTest test) {
        super(owner, "Test Results - " + test.getTitle(), true);
        this.test = test;
        this.attemptDAO = new TestAttemptDAO();
        this.answerDAO = new TestAnswerDAO();

        initComponents();
        loadData();
        
        setSize(1200, 800);
        setLocationRelativeTo(owner);
    }

    public TestResultsDialog(Frame owner, CourseTest test) {
        super(owner, "Test Results - " + test.getTitle(), true);
        this.test = test;
        this.attemptDAO = new TestAttemptDAO();
        this.answerDAO = new TestAnswerDAO();

        initComponents();
        loadData();
        
        setSize(1200, 800);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Attempts tab
        JPanel attemptsPanel = createAttemptsPanel();
        tabbedPane.addTab("Student Attempts", attemptsPanel);

        // Statistics tab
        JPanel statsPanel = createStatisticsPanel();
        tabbedPane.addTab("Test Statistics", statsPanel);

        // Question Analytics tab
        JPanel analyticsPanel = createQuestionAnalyticsPanel();
        tabbedPane.addTab("Question Analytics", analyticsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel with close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.setPreferredSize(new Dimension(100, 40));
        refreshButton.addActionListener(e -> loadData());
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setPreferredSize(new Dimension(100, 40));
        closeButton.addActionListener(e -> dispose());
        
        bottomPanel.add(refreshButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Test Results & Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel testTitleLabel = new JLabel("Test: " + test.getTitle());
        testTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        testTitleLabel.setForeground(UITheme.MUTED_TEXT);
        testTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(testTitleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createAttemptsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create attempts table
        String[] columns = {"Student", "Attempt #", "Score (%)", "Status", "Started", "Completed", "Time Spent", "Passed"};
        attemptsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attemptsTable = new JTable(attemptsTableModel);
        attemptsTable.setBackground(Color.WHITE);
        attemptsTable.setRowHeight(30);
        attemptsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attemptsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        attemptsTable.getTableHeader().setBackground(UITheme.BACKGROUND);

        // Set column widths
        attemptsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Student
        attemptsTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Attempt #
        attemptsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Score
        attemptsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        attemptsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Started
        attemptsTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Completed
        attemptsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Time Spent
        attemptsTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Passed

        JScrollPane tableScrollPane = new JScrollPane(attemptsTable);
        tableScrollPane.setBackground(Color.WHITE);
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(tableScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        statisticsPanel = new JPanel();
        statisticsPanel.setLayout(new BoxLayout(statisticsPanel, BoxLayout.Y_AXIS));
        statisticsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(statisticsPanel);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createQuestionAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Question-by-Question Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT);

        // Create question analytics table
        String[] columns = {"Question #", "Question Text", "Correct %", "Total Answers", "Difficulty"};
        DefaultTableModel questionModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable questionTable = new JTable(questionModel);
        questionTable.setBackground(Color.WHITE);
        questionTable.setRowHeight(40);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        questionTable.getTableHeader().setBackground(UITheme.BACKGROUND);

        // Set column widths
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Question #
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(400); // Question Text
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Correct %
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Total Answers
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Difficulty

        // Load question analytics data
        try {
            List<TestAnswerDAO.QuestionStatistics> questionStats = answerDAO.getQuestionStatistics(test.getId());
            for (TestAnswerDAO.QuestionStatistics stat : questionStats) {
                String difficulty = "Easy";
                if (stat.getCorrectPercentage() < 50) {
                    difficulty = "Hard";
                } else if (stat.getCorrectPercentage() < 75) {
                    difficulty = "Medium";
                }

                questionModel.addRow(new Object[]{
                    stat.getOrderIndex(),
                    truncateText(stat.getQuestionText(), 60),
                    String.format("%.1f%%", stat.getCorrectPercentage()),
                    stat.getTotalAnswers(),
                    difficulty
                });
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading question analytics: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.NORTH);
        }

        JScrollPane questionScrollPane = new JScrollPane(questionTable);
        questionScrollPane.setBackground(Color.WHITE);
        questionScrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(questionScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        loadAttempts();
        loadStatistics();
    }

    private void loadAttempts() {
        attemptsTableModel.setRowCount(0);
        try {
            List<TestAttempt> attempts = attemptDAO.findByTestId(test.getId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

            for (TestAttempt attempt : attempts) {
                String timeSpent = formatTimeSpent(attempt.getTimeSpentSeconds());
                String status = attempt.getStatus();
                if ("COMPLETED".equals(status)) {
                    status = attempt.isPassed() ? "Passed" : "Failed";
                }

                attemptsTableModel.addRow(new Object[]{
                    attempt.getUserName() != null ? attempt.getUserName() : "User " + attempt.getUserId(),
                    attempt.getAttemptNumber(),
                    String.format("%.1f", attempt.getScorePercentage()),
                    status,
                    attempt.getStartedAt() != null ? attempt.getStartedAt().format(formatter) : "-",
                    attempt.getCompletedAt() != null ? attempt.getCompletedAt().format(formatter) : "-",
                    timeSpent,
                    attempt.isPassed() ? "Yes" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading attempts: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStatistics() {
        statisticsPanel.removeAll();
        
        try {
            TestAttemptDAO.TestStatistics stats = attemptDAO.getTestStatistics(test.getId());

            // Create statistics cards
            JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
            cardsPanel.setBackground(Color.WHITE);
            cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            cardsPanel.add(createStatCard("Total Students", String.valueOf(stats.getTotalStudents()), UITheme.PRIMARY));
            cardsPanel.add(createStatCard("Total Attempts", String.valueOf(stats.getTotalAttempts()), new Color(155, 89, 182)));
            cardsPanel.add(createStatCard("Average Score", String.format("%.1f%%", stats.getAverageScore()), new Color(241, 196, 15)));
            cardsPanel.add(createStatCard("Students Passed", String.valueOf(stats.getPassedCount()), UITheme.ACCENT));
            cardsPanel.add(createStatCard("Students Failed", String.valueOf(stats.getFailedCount()), new Color(231, 76, 60)));
            cardsPanel.add(createStatCard("Pass Rate", String.format("%.1f%%", stats.getPassRate()), new Color(26, 188, 156)));

            statisticsPanel.add(cardsPanel);

            // Add detailed statistics
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setBackground(Color.WHITE);
            detailsPanel.setBorder(BorderFactory.createTitledBorder("Detailed Statistics"));

            detailsPanel.add(createDetailRow("Average Time Spent:", formatTimeSpent((int) stats.getAverageTimeSeconds())));
            detailsPanel.add(createDetailRow("Test Passing Score:", String.format("%.1f%%", test.getPassingScore())));
            detailsPanel.add(createDetailRow("Test Time Limit:", test.getTimeLimitMinutes() != null ? 
                test.getTimeLimitMinutes() + " minutes" : "No limit"));
            detailsPanel.add(createDetailRow("Max Attempts Allowed:", test.getMaxAttempts() != null ? 
                String.valueOf(test.getMaxAttempts()) : "Unlimited"));

            statisticsPanel.add(detailsPanel);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading statistics: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            statisticsPanel.add(errorLabel);
        }

        statisticsPanel.revalidate();
        statisticsPanel.repaint();
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(UITheme.MUTED_TEXT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setForeground(UITheme.TEXT);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComp.setForeground(UITheme.MUTED_TEXT);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.EAST);

        return row;
    }

    private String formatTimeSpent(int seconds) {
        if (seconds <= 0) return "0 min";
        
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        
        if (minutes == 0) {
            return remainingSeconds + " sec";
        } else if (remainingSeconds == 0) {
            return minutes + " min";
        } else {
            return minutes + " min " + remainingSeconds + " sec";
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}