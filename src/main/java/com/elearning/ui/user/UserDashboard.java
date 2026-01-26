package com.elearning.ui.user;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student dashboard for browsing and enrolling in courses
 */
public class UserDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

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
        headerPanel.setBackground(new Color(129, 199, 132)); // Light green
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("  Student Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
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
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCourses());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
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
        myCoursesTable.setRowHeight(30);
        myCoursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        myCoursesTable.getColumn("Actions").setCellEditor(new MyEnrollmentButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(myCoursesTable);

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
                    "N/A", // Category - would need to join with course
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
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(textArea);
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
        JOptionPane.showMessageDialog(this,
            "Course learning interface will be implemented in Phase 5",
            "Info",
            JOptionPane.INFORMATION_MESSAGE);
        // TODO: Open course learning interface
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
