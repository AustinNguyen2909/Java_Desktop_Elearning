package com.elearning.ui.user;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LessonService;
import com.elearning.ui.components.VideoPlayerPanel;
import com.elearning.util.SessionManager;
import com.elearning.util.VideoUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Student dashboard for browsing and enrolling in courses
 */
public class UserDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;

    private JTabbedPane tabbedPane;
    private JTable availableCoursesTable;
    private DefaultTableModel availableCoursesModel;
    private JTable myCoursesTable;
    private DefaultTableModel myCoursesModel;
    private JTextField searchField;

    public UserDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();
        this.lessonService = new LessonService();

        initComponents();
        loadAvailableCourses();
        loadMyCourses();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE); // Light background

        // Header
        JPanel headerPanel = createHeader();

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE); // Light background
        tabbedPane.addTab("Browse Courses", createBrowseCoursesPanel());
        tabbedPane.addTab("My Courses", createMyCoursesPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));

        JLabel titleLabel = new JLabel("  Student Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 33, 33));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 53, 69)); // Red for logout
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createBrowseCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with search
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(Color.WHITE); // Light background

        JLabel titleLabel = new JLabel("Available Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE); // Light background
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(new Color(33, 33, 33)); // Dark text
        searchPanel.add(searchLbl);
        searchField = new JTextField(20);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(new Color(33, 33, 33));
        searchField.setCaretColor(new Color(33, 33, 33));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(30, 64, 175)); // Navy blue
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchCourses());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadAvailableCourses());
        searchPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // Courses table
        String[] columnNames = {"ID", "Title", "Instructor", "Category", "Difficulty", "Hours", "Rating", "Students", "Actions"};
        availableCoursesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only Actions column
            }
        };
        availableCoursesTable = new JTable(availableCoursesModel);
        availableCoursesTable.setBackground(Color.WHITE);
        availableCoursesTable.setForeground(new Color(33, 33, 33));
        availableCoursesTable.setGridColor(new Color(230, 230, 230));
        availableCoursesTable.setRowHeight(30);
        availableCoursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        availableCoursesTable.getColumn("Actions").setCellEditor(new AvailableCourseButtonEditor(new JCheckBox()));

        // Add double-click to view details
        availableCoursesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    int row = availableCoursesTable.getSelectedRow();
                    if (row >= 0 && availableCoursesTable.columnAtPoint(e.getPoint()) != 8) {
                        int courseId = (Integer) availableCoursesModel.getValueAt(row, 0);
                        viewCourseDetails(courseId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(availableCoursesTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMyCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE); // Light background
        JLabel titleLabel = new JLabel("My Enrolled Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadMyCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Courses table
        String[] columnNames = {"ID", "Title", "Category", "Progress", "Enrolled Date", "Last Accessed", "Actions"};
        myCoursesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column
            }
        };
        myCoursesTable = new JTable(myCoursesModel);
        myCoursesTable.setBackground(Color.WHITE);
        myCoursesTable.setForeground(new Color(33, 33, 33));
        myCoursesTable.setGridColor(new Color(230, 230, 230));
        myCoursesTable.setRowHeight(30);
        myCoursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        myCoursesTable.getColumn("Actions").setCellEditor(new MyEnrollmentButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(myCoursesTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(Color.WHITE); // Light background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Username
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel usernameLbl = new JLabel("Username:");
        usernameLbl.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(usernameLbl, gbc);
        gbc.gridx = 1;
        JLabel usernameVal = new JLabel(currentUser.getUsername());
        usernameVal.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(usernameVal, gbc);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(nameLbl, gbc);
        gbc.gridx = 1;
        JLabel nameVal = new JLabel(currentUser.getFullName());
        nameVal.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(nameVal, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(emailLbl, gbc);
        gbc.gridx = 1;
        JLabel emailVal = new JLabel(currentUser.getEmail());
        emailVal.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(emailVal, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel phoneLbl = new JLabel("Phone:");
        phoneLbl.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(phoneLbl, gbc);
        gbc.gridx = 1;
        JLabel phoneVal = new JLabel(currentUser.getPhone() != null ? currentUser.getPhone() : "N/A");
        phoneVal.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(phoneVal, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel roleLbl = new JLabel("Role:");
        roleLbl.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(roleLbl, gbc);
        gbc.gridx = 1;
        JLabel roleVal = new JLabel(currentUser.getRole());
        roleVal.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(roleVal, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(statusLbl, gbc);
        gbc.gridx = 1;
        JLabel statusVal = new JLabel(currentUser.getStatus());
        statusVal.setForeground(new Color(33, 33, 33)); // Dark text
        profilePanel.add(statusVal, gbc);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(profilePanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadAvailableCourses() {
        searchField.setText("");
        try {
            List<Course> courses = courseService.getAvailableCourses();
            displayAvailableCourses(courses);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading courses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCourses() {
        String keyword = searchField.getText().trim();
        try {
            List<Course> courses = courseService.searchCourses(keyword);
            displayAvailableCourses(courses);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching courses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayAvailableCourses(List<Course> courses) {
        availableCoursesModel.setRowCount(0);

        for (Course course : courses) {
            // Check if already enrolled
            boolean enrolled = enrollmentService.isEnrolled(currentUser.getId(), course.getId());

            Object[] row = {
                course.getId(),
                course.getTitle(),
                course.getInstructorName(),
                course.getCategory(),
                course.getDifficultyLevel(),
                course.getEstimatedHours(),
                String.format("%.1f", course.getAverageRating()),
                course.getEnrollmentCount(),
                enrolled ? "Enrolled" : "Enroll"
            };
            availableCoursesModel.addRow(row);
        }
    }

    private void loadMyCourses() {
        try {
            List<Enrollment> enrollments = enrollmentService.getUserEnrollments(currentUser.getId());

            myCoursesModel.setRowCount(0);

            for (Enrollment enrollment : enrollments) {
                Object[] row = {
                    enrollment.getCourseId(),
                    enrollment.getCourseTitle(),
                    enrollment.getCourseCategory() != null ? enrollment.getCourseCategory() : "N/A",
                    String.format("%.1f%%", enrollment.getProgressPercent()),
                    enrollment.getEnrolledAt(),
                    enrollment.getLastAccessedAt() != null ? enrollment.getLastAccessedAt() : "Never",
                    "Actions"
                };
                myCoursesModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading enrollments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewCourseDetails(int courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course != null) {
                boolean enrolled = enrollmentService.isEnrolled(currentUser.getId(), courseId);

                String details = String.format(
                    "Course Details:\n\n" +
                    "Title: %s\n" +
                    "Instructor: %s\n" +
                    "Category: %s\n" +
                    "Difficulty: %s\n" +
                    "Estimated Hours: %d\n" +
                    "Average Rating: %.1f / 5.0\n" +
                    "Students Enrolled: %d\n" +
                    "Your Status: %s\n\n" +
                    "Description:\n%s",
                    course.getTitle(),
                    course.getInstructorName(),
                    course.getCategory(),
                    course.getDifficultyLevel(),
                    course.getEstimatedHours(),
                    course.getAverageRating(),
                    course.getEnrollmentCount(),
                    enrolled ? "Enrolled" : "Not Enrolled",
                    course.getDescription()
                );

                JTextArea textArea = new JTextArea(details);
                textArea.setBackground(Color.WHITE);
                textArea.setForeground(new Color(33, 33, 33));
                textArea.setCaretColor(new Color(33, 33, 33));
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setBackground(Color.WHITE);
                scrollPane.getViewport().setBackground(Color.WHITE);
                scrollPane.setPreferredSize(new Dimension(500, 400));

                JOptionPane.showMessageDialog(this, scrollPane, "Course Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enrollInCourse(int courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Do you want to enroll in this course?",
            "Confirm Enrollment",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = enrollmentService.enrollInCourse(currentUser.getId(), courseId, currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Successfully enrolled in the course!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAvailableCourses();
                    loadMyCourses();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to enroll in course",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void continueCourse(int courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Course not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check enrollment
        Enrollment enrollment = enrollmentService.getEnrollment(currentUser.getId(), courseId);
        if (enrollment == null) {
            JOptionPane.showMessageDialog(this, "You are not enrolled in this course", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create learning interface dialog
        JDialog dialog = new JDialog(this, "Learning: " + course.getTitle(), true);
        dialog.setSize(1200, 800);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Left panel - Lesson list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JLabel lessonsTitle = new JLabel("  Course Content");
        lessonsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lessonsTitle.setForeground(new Color(33, 33, 33));
        lessonsTitle.setPreferredSize(new Dimension(0, 40));
        lessonsTitle.setOpaque(true);
        lessonsTitle.setBackground(new Color(240, 240, 240));

        // Lessons list
        DefaultListModel<Lesson> lessonListModel = new DefaultListModel<>();
        JList<Lesson> lessonList = new JList<>(lessonListModel);
        lessonList.setCellRenderer(new LessonListCellRenderer());
        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Load lessons
        try {
            List<Lesson> lessons = lessonService.getCourseLessons(courseId, currentUser.getRole(), currentUser.getId(), true);
            for (Lesson lesson : lessons) {
                lessonListModel.addElement(lesson);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Error loading lessons: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane lessonsScrollPane = new JScrollPane(lessonList);
        lessonsScrollPane.setBackground(Color.WHITE);
        lessonsScrollPane.getViewport().setBackground(Color.WHITE);
        leftPanel.add(lessonsTitle, BorderLayout.NORTH);
        leftPanel.add(lessonsScrollPane, BorderLayout.CENTER);

        // Progress panel
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        progressPanel.setBackground(Color.WHITE);
        JLabel progressLabel = new JLabel(String.format("Progress: %.1f%%", enrollment.getProgressPercent()));
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        progressLabel.setForeground(new Color(88, 88, 88));
        progressPanel.add(progressLabel);
        leftPanel.add(progressPanel, BorderLayout.SOUTH);

        // Right panel - Lesson viewer
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);

        JLabel instructionLabel = new JLabel("<html><div style='text-align: center; padding: 50px;'>" +
            "<h2>Select a lesson from the left to start learning</h2>" +
            "<p>Click on any lesson to view its content</p></div></html>",
            SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(88, 88, 88));
        rightPanel.add(instructionLabel, BorderLayout.CENTER);

        // Lesson selection listener
        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Lesson selectedLesson = lessonList.getSelectedValue();
                if (selectedLesson != null) {
                    showLessonContent(dialog, rightPanel, selectedLesson, courseId, enrollment, progressLabel);
                }
            }
        });

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        dialog.add(mainPanel);
        dialog.setVisible(true);

        // Refresh course list when dialog closes
        loadMyCourses();
    }

    private void showLessonContent(JDialog parentDialog, JPanel contentPanel, Lesson lesson, int courseId, Enrollment enrollment, JLabel progressLabel) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout(10, 10));

        // Header with lesson info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 248, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Lesson " + lesson.getOrderIndex() + ": " + lesson.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 33, 33));

        JLabel durationLabel = new JLabel("Duration: " + (lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0) + " minutes");
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        durationLabel.setForeground(new Color(88, 88, 88));

        JPanel headerTextPanel = new JPanel(new BorderLayout());
        headerTextPanel.setBackground(new Color(240, 248, 255));
        headerTextPanel.add(titleLabel, BorderLayout.NORTH);
        headerTextPanel.add(durationLabel, BorderLayout.SOUTH);
        headerPanel.add(headerTextPanel, BorderLayout.CENTER);

        // Video panel
        JPanel videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(Color.BLACK);
        videoPanel.setPreferredSize(new Dimension(0, 450));

        if (lesson.getVideoPath() != null && !lesson.getVideoPath().isEmpty() &&
            VideoUtil.videoExists(lesson.getVideoPath())) {
            try {
                // Create video player
                VideoPlayerPanel playerPanel = new VideoPlayerPanel(lesson.getVideoPath());
                videoPanel.add(playerPanel, BorderLayout.CENTER);

                // Add disposal handler for when dialog closes
                parentDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        playerPanel.dispose();
                    }
                });
            } catch (Exception e) {
                JLabel errorLabel = new JLabel(
                    "<html><div style='text-align: center; color: white;'>" +
                    "<h2>Video Player Error</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "</div></html>",
                    SwingConstants.CENTER);
                errorLabel.setForeground(Color.WHITE);
                videoPanel.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noVideoLabel = new JLabel("No video available for this lesson", SwingConstants.CENTER);
            noVideoLabel.setForeground(Color.WHITE);
            videoPanel.add(noVideoLabel, BorderLayout.CENTER);
        }

        // Description panel
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBackground(Color.WHITE);
        descriptionPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setForeground(new Color(33, 33, 33));

        JTextArea descriptionArea = new JTextArea(lesson.getDescription() != null ? lesson.getDescription() : "No description available");
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setForeground(new Color(33, 33, 33));
        descriptionArea.setCaretColor(new Color(33, 33, 33));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBackground(Color.WHITE);
        descScrollPane.getViewport().setBackground(Color.WHITE);
        descScrollPane.setPreferredSize(new Dimension(0, 120));

        JPanel descContainerPanel = new JPanel(new BorderLayout(5, 5));
        descContainerPanel.setBackground(Color.WHITE);
        descContainerPanel.add(descLabel, BorderLayout.NORTH);
        descContainerPanel.add(descScrollPane, BorderLayout.CENTER);
        descriptionPanel.add(descContainerPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBackground(Color.WHITE);

        JButton completeButton = new JButton("Mark as Complete");
        completeButton.setBackground(new Color(46, 204, 113));
        completeButton.setForeground(Color.WHITE);
        completeButton.setFocusPainted(false);
        completeButton.setBorderPainted(false);
        completeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        completeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        completeButton.setPreferredSize(new Dimension(200, 40));
        completeButton.addActionListener(e -> {
            try {
                boolean success = enrollmentService.completeLesson(currentUser.getId(), lesson.getId());
                if (success) {
                    JOptionPane.showMessageDialog(parentDialog,
                        "Lesson marked as complete!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Update progress label
                    Enrollment updatedEnrollment = enrollmentService.getEnrollment(currentUser.getId(), courseId);
                    if (updatedEnrollment != null) {
                        progressLabel.setText(String.format("Progress: %.1f%%", updatedEnrollment.getProgressPercent()));
                    }

                    completeButton.setEnabled(false);
                    completeButton.setText("Completed ✓");
                } else {
                    JOptionPane.showMessageDialog(parentDialog,
                        "Failed to mark lesson as complete",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentDialog,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Check if lesson is already completed
        try {
            // This would need a method in enrollmentService to check if lesson is completed
            // For now, we'll just enable the button
        } catch (Exception e) {
            // Ignore
        }

        actionPanel.add(completeButton);

        // Assemble content panel
        JPanel contentArea = new JPanel(new BorderLayout(10, 10));
        contentArea.setBackground(Color.WHITE);
        contentArea.add(videoPanel, BorderLayout.NORTH);
        contentArea.add(descriptionPanel, BorderLayout.CENTER);
        contentArea.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(contentArea, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Custom cell renderer for lesson list
    class LessonListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Lesson) {
                Lesson lesson = (Lesson) value;
                label.setText(String.format("<html><b>%d. %s</b><br/><small>%d min%s</small></html>",
                    lesson.getOrderIndex(),
                    lesson.getTitle(),
                    lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0,
                    lesson.isPreview() ? " • Preview" : ""));
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            }

            if (!isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(33, 33, 33));
            } else {
                label.setBackground(new Color(52, 152, 219));
                label.setForeground(Color.WHITE);
            }

            return label;
        }
    }

    private void unenrollFromCourse(int courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to unenroll from this course?\nYou can only unenroll if you have completed less than 10% of the course.",
            "Confirm Unenrollment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = enrollmentService.unenrollFromCourse(currentUser.getId(), courseId, currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Successfully unenrolled from the course",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadMyCourses();
                    loadAvailableCourses();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to unenroll. You may have completed more than 10% of the course.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        SessionManager.getInstance().logout();
        dispose();
        SwingUtilities.invokeLater(() -> {
            com.elearning.ui.LoginFrame loginFrame = new com.elearning.ui.LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    // Button renderer
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Actions" : value.toString());
            return this;
        }
    }

    // Button editor for available courses
    class AvailableCourseButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int currentRow;

        public AvailableCourseButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                handleAction();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                      boolean isSelected, int row, int column) {
            label = (value == null) ? "Actions" : value.toString();
            button.setText(label);
            clicked = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            clicked = false;
            return label;
        }

        private void handleAction() {
            if (clicked && currentRow >= 0) {
                int courseId = (Integer) availableCoursesModel.getValueAt(currentRow, 0);
                String actionText = (String) availableCoursesModel.getValueAt(currentRow, 8);

                if ("Enroll".equals(actionText)) {
                    enrollInCourse(courseId);
                } else if ("Enrolled".equals(actionText)) {
                    viewCourseDetails(courseId);
                }
            }
        }
    }

    // Button editor for my courses
    class MyEnrollmentButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int currentRow;

        public MyEnrollmentButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                showActionsMenu();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                      boolean isSelected, int row, int column) {
            label = (value == null) ? "Actions" : value.toString();
            button.setText(label);
            clicked = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            clicked = false;
            return label;
        }

        private void showActionsMenu() {
            if (clicked && currentRow >= 0) {
                int courseId = (Integer) myCoursesModel.getValueAt(currentRow, 0);

                JPopupMenu menu = new JPopupMenu();

                JMenuItem continueItem = new JMenuItem("Continue Learning");
                continueItem.addActionListener(e -> continueCourse(courseId));
                menu.add(continueItem);

                JMenuItem detailsItem = new JMenuItem("View Details");
                detailsItem.addActionListener(e -> viewCourseDetails(courseId));
                menu.add(detailsItem);

                menu.addSeparator();

                JMenuItem unenrollItem = new JMenuItem("Unenroll");
                unenrollItem.addActionListener(e -> unenrollFromCourse(courseId));
                menu.add(unenrollItem);

                menu.show(button, 0, button.getHeight());
            }
        }
    }
}
