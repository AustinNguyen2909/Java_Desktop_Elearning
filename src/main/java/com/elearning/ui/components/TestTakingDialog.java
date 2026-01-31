package com.elearning.ui.components;

import com.elearning.dao.TestAnswerDAO;
import com.elearning.dao.TestAttemptDAO;
import com.elearning.model.*;
import com.elearning.service.CertificateService;
import com.elearning.service.TestService;
import com.elearning.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dialog for students to take tests
 */
public class TestTakingDialog extends JDialog {
    private final CourseTest test;
    private final int userId;
    private final TestService testService;
    private final TestAttemptDAO attemptDAO;
    private final TestAnswerDAO answerDAO;
    private final CertificateService certificateService;

    // Test state
    private TestAttempt currentAttempt;
    private List<TestQuestion> questions;
    private int currentQuestionIndex = 0;
    private long startTime;
    private Timer timeTimer;

    // UI Components
    private JPanel headerPanel;
    private JPanel questionPanel;
    private JPanel navigationPanel;
    private JLabel timerLabel;
    private JLabel progressLabel;
    private JLabel questionLabel;
    private JTextArea questionTextArea;
    private ButtonGroup optionGroup;
    private JRadioButton[] optionButtons = new JRadioButton[4];
    private JButton previousButton;
    private JButton nextButton;
    private JButton submitButton;

    // Test answers storage
    private List<Integer> selectedAnswers; // Index corresponds to question index, value is selected option ID

    public TestTakingDialog(Dialog owner, CourseTest test) {
        super(owner, "Take Test - " + test.getTitle(), true);
        this.test = test;
        this.userId = SessionManager.getInstance().getCurrentUser().getId();
        this.testService = TestService.getInstance();
        this.attemptDAO = new TestAttemptDAO();
        this.answerDAO = new TestAnswerDAO();
        this.certificateService = CertificateService.getInstance();

        if (!canTakeTest()) {
            return; // Dialog will be disposed in canTakeTest()
        }

        initComponents();
        startTest();
        
        setSize(900, 700);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleTestExit();
            }
        });
    }

    public TestTakingDialog(Frame owner, CourseTest test) {
        super(owner, "Take Test - " + test.getTitle(), true);
        this.test = test;
        this.userId = SessionManager.getInstance().getCurrentUser().getId();
        this.testService = TestService.getInstance();
        this.attemptDAO = new TestAttemptDAO();
        this.answerDAO = new TestAnswerDAO();
        this.certificateService = CertificateService.getInstance();

        if (!canTakeTest()) {
            return; // Dialog will be disposed in canTakeTest()
        }

        initComponents();
        startTest();
        
        setSize(900, 700);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleTestExit();
            }
        });
    }

    private boolean canTakeTest() {
        JLabel messageLabel1 = new JLabel("You have reached the maximum number of attempts for this test.");
        messageLabel1.setForeground(Color.WHITE);
        // Check if user can take another attempt
        if (!attemptDAO.canTakeAnotherAttempt(userId, test.getId(), test.getMaxAttempts())) {
            JOptionPane.showMessageDialog(this,
                messageLabel1,
                "Cannot Take Test",
                JOptionPane.WARNING_MESSAGE);
            dispose();
            return false;
        }

        JLabel messageLabel2 = new JLabel("This test is not yet available.");
        messageLabel2.setForeground(Color.WHITE);
        // Check if test is published
        if (!test.getIsPublished()) {
            JOptionPane.showMessageDialog(this,
                messageLabel2,
                "Test Not Available",
                JOptionPane.WARNING_MESSAGE);
            dispose();
            return false;
        }

        return true;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel with timer and progress
        createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main question panel
        createQuestionPanel();
        add(questionPanel, BorderLayout.CENTER);

        // Navigation panel
        createNavigationPanel();
        add(navigationPanel, BorderLayout.SOUTH);
    }

    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 250, 252));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Left side - Test info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel testTitleLabel = new JLabel(test.getTitle());
        testTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        testTitleLabel.setForeground(UITheme.TEXT);
        testTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressLabel = new JLabel();
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressLabel.setForeground(UITheme.MUTED_TEXT);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(testTitleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(progressLabel);

        // Right side - Timer
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.setOpaque(false);

        if (test.getTimeLimitMinutes() != null) {
            JLabel timerTitleLabel = new JLabel("Time Remaining:");
            timerTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            timerTitleLabel.setForeground(UITheme.MUTED_TEXT);
            timerTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            timerLabel = new JLabel();
            timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            timerLabel.setForeground(new Color(239, 68, 68));
            timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            timerPanel.add(timerTitleLabel);
            timerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            timerPanel.add(timerLabel);
        }

        headerPanel.add(infoPanel, BorderLayout.WEST);
        if (test.getTimeLimitMinutes() != null) {
            headerPanel.add(timerPanel, BorderLayout.EAST);
        }
    }

    private void createQuestionPanel() {
        questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBackground(Color.WHITE);
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Question number and text
        JPanel questionHeaderPanel = new JPanel(new BorderLayout());
        questionHeaderPanel.setOpaque(false);

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        questionLabel.setForeground(UITheme.TEXT);

        questionHeaderPanel.add(questionLabel, BorderLayout.WEST);

        questionTextArea = new JTextArea();
        questionTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        questionTextArea.setForeground(UITheme.TEXT);
        questionTextArea.setBackground(Color.WHITE);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        // Answer options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);

        optionGroup = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            optionButtons[i].setOpaque(false);
            optionButtons[i].setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            optionGroup.add(optionButtons[i]);

            // Add action listener to save answer when selected
            final int optionIndex = i;
            optionButtons[i].addActionListener(e -> {
                if (optionButtons[optionIndex].isSelected() && currentQuestionIndex < questions.size()) {
                    TestQuestion currentQuestion = questions.get(currentQuestionIndex);
                    List<AnswerOption> options = currentQuestion.getOptions();
                    if (options != null && optionIndex < options.size()) {
                        selectedAnswers.set(currentQuestionIndex, options.get(optionIndex).getId());
                    }
                }
            });

            optionsPanel.add(optionButtons[i]);
        }

        questionPanel.add(questionHeaderPanel, BorderLayout.NORTH);
        questionPanel.add(questionTextArea, BorderLayout.CENTER);
        questionPanel.add(optionsPanel, BorderLayout.SOUTH);
    }

    private void createNavigationPanel() {
        navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBackground(Color.WHITE);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Left side - Previous button
        previousButton = new JButton("← Previous");
        previousButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        previousButton.setPreferredSize(new Dimension(120, 40));
        previousButton.addActionListener(e -> previousQuestion());

        // Right side - Next and Submit buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        nextButton = new JButton("Next →");
        nextButton.setBackground(UITheme.PRIMARY);
        nextButton.setForeground(Color.WHITE);
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.setPreferredSize(new Dimension(120, 40));
        nextButton.addActionListener(e -> nextQuestion());

        submitButton = new JButton("Submit Test");
        submitButton.setBackground(new Color(34, 197, 94));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setPreferredSize(new Dimension(130, 40));
        submitButton.addActionListener(e -> submitTest());
        submitButton.setVisible(false);

        rightPanel.add(nextButton);
        rightPanel.add(submitButton);

        navigationPanel.add(previousButton, BorderLayout.WEST);
        navigationPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void startTest() {
        try {
            // Load questions
            questions = testService.getQuestions(test.getId());
            JLabel messageLabel3 = new JLabel("This test has no questions.");
            messageLabel3.setForeground(Color.WHITE);
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    messageLabel3,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            // Shuffle questions if enabled
            if (test.getShuffleQuestions()) {
                Collections.shuffle(questions);
            }

            // Shuffle options for each question if enabled
            if (test.getShuffleOptions()) {
                for (TestQuestion question : questions) {
                    if (question.getOptions() != null) {
                        Collections.shuffle(question.getOptions());
                    }
                }
            }

            // Initialize selected answers array
            selectedAnswers = new ArrayList<>(Collections.nCopies(questions.size(), null));

            // Create test attempt
            currentAttempt = new TestAttempt(test.getId(), userId, test.getCourseId());
            currentAttempt.setAttemptNumber(attemptDAO.getNextAttemptNumber(userId, test.getId()));
            currentAttempt.setTotalQuestions(questions.size());
            
            // Calculate total points
            double totalPoints = questions.stream()
                .mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 1.0)
                .sum();
            currentAttempt.setTotalPoints(totalPoints);
            currentAttempt.setStartedAt(LocalDateTime.now());

            // Save attempt to database
            currentAttempt = attemptDAO.create(currentAttempt);
            if (currentAttempt == null || currentAttempt.getId() == null) {
                JLabel messageLabel4 = new JLabel("Failed to start test. Please try again.");
                messageLabel4.setForeground(Color.WHITE);
                JOptionPane.showMessageDialog(this,
                    messageLabel4,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            // Start timer
            startTime = System.currentTimeMillis();
            if (test.getTimeLimitMinutes() != null) {
                startTimer();
            }

            // Show first question
            showQuestion(0);

        } catch (Exception e) {
            JLabel messageLabel5 = new JLabel("Error starting test: " + e.getMessage());
            messageLabel5.setForeground(Color.WHITE);
            JOptionPane.showMessageDialog(this,
                messageLabel5,
                "Error",
                JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void startTimer() {
        timeTimer = new Timer(1000, new ActionListener() {
            private int remainingSeconds = test.getTimeLimitMinutes() * 60;

            @Override
            public void actionPerformed(ActionEvent e) {
                remainingSeconds--;
                
                if (remainingSeconds <= 0) {
                    timeTimer.stop();
                    JLabel messageLabel6 = new JLabel("Time's up! The test will be submitted automatically.");
                    messageLabel6.setForeground(Color.WHITE);
                    JOptionPane.showMessageDialog(TestTakingDialog.this,
                        messageLabel6,
                        "Time Up",
                        JOptionPane.WARNING_MESSAGE);
                    submitTest();
                    return;
                }

                // Update timer display
                int minutes = remainingSeconds / 60;
                int seconds = remainingSeconds % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

                // Change color when time is running low
                if (remainingSeconds <= 300) { // 5 minutes
                    timerLabel.setForeground(new Color(239, 68, 68)); // Red
                } else if (remainingSeconds <= 600) { // 10 minutes
                    timerLabel.setForeground(new Color(251, 146, 60)); // Orange
                }
            }
        });
        timeTimer.start();
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        currentQuestionIndex = index;
        TestQuestion question = questions.get(index);

        // Update progress
        progressLabel.setText(String.format("Question %d of %d", index + 1, questions.size()));

        // Update question
        questionLabel.setText(String.format("Question %d:", index + 1));
        questionTextArea.setText(question.getQuestionText());

        // Update options
        List<AnswerOption> options = question.getOptions();
        String[] letters = {"A", "B", "C", "D"};
        
        // Clear previous selection
        optionGroup.clearSelection();
        
        for (int i = 0; i < 4; i++) {
            if (options != null && i < options.size()) {
                AnswerOption option = options.get(i);
                optionButtons[i].setText(letters[i] + ". " + option.getOptionText());
                optionButtons[i].setVisible(true);
                
                // Restore previous selection if any
                Integer selectedOptionId = selectedAnswers.get(currentQuestionIndex);
                if (selectedOptionId != null && selectedOptionId.equals(option.getId())) {
                    optionButtons[i].setSelected(true);
                }
            } else {
                optionButtons[i].setVisible(false);
            }
        }

        // Update navigation buttons
        previousButton.setEnabled(index > 0);
        nextButton.setVisible(index < questions.size() - 1);
        submitButton.setVisible(index == questions.size() - 1);
    }

    private void previousQuestion() {
        if (currentQuestionIndex > 0) {
            showQuestion(currentQuestionIndex - 1);
        }
    }

    private void nextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            showQuestion(currentQuestionIndex + 1);
        }
    }

    private void submitTest() {
        // Check if all questions are answered
        long unansweredCount = selectedAnswers.stream().filter(answer -> answer == null).count();
        
        if (unansweredCount > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                String.format("You have %d unanswered questions.\n\nAre you sure you want to submit the test?", 
                    unansweredCount),
                "Unanswered Questions",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit your test?\n\nYou cannot change your answers after submission.",
                "Submit Test",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            // Stop timer
            if (timeTimer != null) {
                timeTimer.stop();
            }

            // Calculate time spent
            long timeSpentMs = System.currentTimeMillis() - startTime;
            int timeSpentSeconds = (int) (timeSpentMs / 1000);

            // Save all answers and calculate score
            double earnedPoints = 0.0;
            for (int i = 0; i < questions.size(); i++) {
                TestQuestion question = questions.get(i);
                Integer selectedOptionId = selectedAnswers.get(i);
                
                if (selectedOptionId != null) {
                    // Find the selected option
                    AnswerOption selectedOption = question.getOptions().stream()
                        .filter(opt -> opt.getId().equals(selectedOptionId))
                        .findFirst()
                        .orElse(null);
                    
                    if (selectedOption != null) {
                        // Create test answer
                        TestAnswer answer = new TestAnswer(currentAttempt.getId(), question.getId(), selectedOptionId);
                        answer.setCorrect(selectedOption.getIsCorrect());
                        answer.setPointsEarned(selectedOption.getIsCorrect() ? question.getPoints() : 0.0);
                        answer.setAnsweredAt(LocalDateTime.now());
                        
                        // Save answer
                        answerDAO.create(answer);
                        
                        // Add to earned points
                        if (selectedOption.getIsCorrect()) {
                            earnedPoints += question.getPoints();
                        }
                    }
                }
            }

            // Update attempt with final results
            currentAttempt.setEarnedPoints(earnedPoints);
            currentAttempt.calculateScore();
            currentAttempt.setPassed(currentAttempt.getScorePercentage() >= test.getPassingScore());
            currentAttempt.setStatus("COMPLETED");
            currentAttempt.setCompletedAt(LocalDateTime.now());
            currentAttempt.setTimeSpentSeconds(timeSpentSeconds);

            // Save updated attempt
            boolean updated = attemptDAO.update(currentAttempt);
            
            if (updated) {
                // Generate certificate if student passed
                Certificate certificate = null;
                if (currentAttempt.isPassed()) {
                    try {
                        certificate = certificateService.issueTestCertificate(
                            userId, 
                            test.getCourseId(), 
                            test.getId(), 
                            currentAttempt.getId(), 
                            currentAttempt.getScorePercentage()
                        );
                    } catch (Exception e) {
                        // Log error but don't fail the test submission
                        System.err.println("Error generating certificate: " + e.getMessage());
                    }
                }
                
                // Show results
                showTestResults(certificate);
            } else {
                JLabel messageLabel7 = new JLabel("Error saving test results. Please contact support.");
                messageLabel7.setForeground(Color.WHITE);
                JOptionPane.showMessageDialog(this,
                    messageLabel7,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JLabel messageLabel8 = new JLabel("Error submitting test: " + e.getMessage());
            messageLabel8.setForeground(Color.WHITE);
            JOptionPane.showMessageDialog(this,
                messageLabel8,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTestResults(Certificate certificate) {
        // Create results dialog
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Test Completed!\n\n");
        messageBuilder.append(String.format("Your Score: %.1f%% (%.1f/%.1f points)\n", 
            currentAttempt.getScorePercentage(), currentAttempt.getEarnedPoints(), currentAttempt.getTotalPoints()));
        messageBuilder.append(String.format("Passing Score: %.1f%%\n", test.getPassingScore()));
        messageBuilder.append(String.format("Result: %s\n\n", currentAttempt.isPassed() ? "PASSED" : "FAILED"));
        messageBuilder.append(String.format("Time Spent: %s", formatTimeSpent(currentAttempt.getTimeSpentSeconds())));
        
        // Add certificate information if generated
        if (certificate != null) {
            messageBuilder.append("\n\n🎓 CERTIFICATE EARNED!\n");
            messageBuilder.append(String.format("Certificate Code: %s\n", certificate.getCertificateCode()));
            messageBuilder.append("You can view your certificate in your dashboard.");
        }

        String title = currentAttempt.isPassed() ? "Congratulations!" : "Test Results";
        int messageType = currentAttempt.isPassed() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;

        JOptionPane.showMessageDialog(this, messageBuilder.toString(), title, messageType);
        
        // Close dialog
        dispose();
    }

    private void handleTestExit() {
        if (currentAttempt != null && "IN_PROGRESS".equals(currentAttempt.getStatus())) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the test?\n\n" +
                "Your progress will be lost and this will count as an attempt.",
                "Exit Test",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                // Mark attempt as abandoned
                try {
                    currentAttempt.setStatus("ABANDONED");
                    currentAttempt.setCompletedAt(LocalDateTime.now());
                    long timeSpentMs = System.currentTimeMillis() - startTime;
                    currentAttempt.setTimeSpentSeconds((int) (timeSpentMs / 1000));
                    attemptDAO.update(currentAttempt);
                } catch (Exception e) {
                    // Log error but still close
                    e.printStackTrace();
                }
                
                if (timeTimer != null) {
                    timeTimer.stop();
                }
                dispose();
            }
        } else {
            dispose();
        }
    }

    private String formatTimeSpent(int seconds) {
        if (seconds <= 0) return "0 minutes";
        
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        
        if (minutes == 0) {
            return remainingSeconds + " seconds";
        } else if (remainingSeconds == 0) {
            return minutes + " minutes";
        } else {
            return minutes + " minutes " + remainingSeconds + " seconds";
        }
    }
}