package com.elearning.ui.instructor;

import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.AnalyticsService;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LessonService;
import com.elearning.ui.components.ModernButton;
import com.elearning.ui.components.ModernTextField;
import com.elearning.util.ChartUtil;
import com.elearning.util.SessionManager;
import com.elearning.util.FileUtil;
import com.elearning.util.VideoUtil;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Instructor dashboard for course management
 */
public class InstructorDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;
    private final AnalyticsService analyticsService;

    private JPanel contentPanel;
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;

    public InstructorDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();
        this.lessonService = new LessonService();
        this.analyticsService = new AnalyticsService();

        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = createHeader();

        // Sidebar + Content layout
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Color.WHITE);

        // Create sidebar
        JPanel sidebar = createSidebar();

        // Create content panel with CardLayout
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(createCoursesPanel(), "My Courses");
        contentPanel.add(createNewCoursePanel(), "Create Course");
        contentPanel.add(createStatisticsPanel(), "Statistics");

        contentArea.add(sidebar, BorderLayout.WEST);
        contentArea.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));

        JLabel titleLabel = new JLabel("  Instructor Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 33, 33));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(248, 249, 250));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // Add menu items
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createMenuItem("My Courses", "My Courses"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createMenuItem("Create Course", "Create Course"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createMenuItem("Statistics", "Statistics"));

        // Push logout button to bottom
        sidebar.add(Box.createVerticalGlue());

        // Logout button at bottom
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(220, 45));
        logoutButton.addActionListener(e -> logout());

        sidebar.add(logoutButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        return sidebar;
    }

    private JButton createMenuItem(String text, String panelName) {
        JButton menuItem = new JButton(text);
        menuItem.setBackground(new Color(30, 64, 175));
        menuItem.setForeground(Color.WHITE);
        menuItem.setFocusPainted(false);
        menuItem.setBorderPainted(false);
        menuItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuItem.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuItem.setMaximumSize(new Dimension(220, 45));
        menuItem.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, panelName);
        });
        return menuItem;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with refresh button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE); // Light background
        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Courses table
        String[] columnNames = {"ID", "Title", "Category", "Status", "Published", "Enrollments", "Rating", "Actions"};
        coursesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only Actions column is editable
            }
        };
        coursesTable = new JTable(coursesTableModel);
        coursesTable.setBackground(Color.WHITE);
        coursesTable.setForeground(new Color(33, 33, 33));
        coursesTable.setGridColor(new Color(230, 230, 230));
        coursesTable.setRowHeight(30);
        coursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        coursesTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNewCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Create New Course");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE); // Light background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLbl = new JLabel("Title:");
        titleLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(titleLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField titleField = new JTextField(30);
        titleField.setBackground(Color.WHITE);
        titleField.setForeground(new Color(33, 33, 33));
        titleField.setCaretColor(new Color(33, 33, 33));
        formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel descLbl = new JLabel("Description:");
        descLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(descLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextArea descriptionArea = new JTextArea(5, 30);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setForeground(new Color(33, 33, 33));
        descriptionArea.setCaretColor(new Color(33, 33, 33));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBackground(Color.WHITE);
        descScroll.getViewport().setBackground(Color.WHITE);
        formPanel.add(descScroll, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel catLbl = new JLabel("Category:");
        catLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(catLbl, gbc);

        gbc.gridx = 1;
        String[] categories = {"Programming", "Web Development", "Database", "Mobile Development",
                "Data Science", "DevOps", "Security", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setBackground(Color.WHITE);
        categoryCombo.setForeground(new Color(33, 33, 33));
        formPanel.add(categoryCombo, gbc);

        // Difficulty Level
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel diffLbl = new JLabel("Difficulty:");
        diffLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(diffLbl, gbc);

        gbc.gridx = 1;
        String[] difficulties = {"BEGINNER", "INTERMEDIATE", "ADVANCED"};
        JComboBox<String> difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setBackground(Color.WHITE);
        difficultyCombo.setForeground(new Color(33, 33, 33));
        formPanel.add(difficultyCombo, gbc);

        // Estimated Hours
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel hoursLbl = new JLabel("Estimated Hours:");
        hoursLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(hoursLbl, gbc);

        gbc.gridx = 1;
        JSpinner hoursSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        formPanel.add(hoursSpinner, gbc);

        // Hero Image Upload
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel thumbLbl = new JLabel("Hero Image:");
        thumbLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(thumbLbl, gbc);

        gbc.gridx = 1;
        JPanel thumbnailPanel = new JPanel(new BorderLayout(10, 0));
        thumbnailPanel.setBackground(Color.WHITE);

        JLabel thumbnailInfoLabel = new JLabel("No image selected");
        thumbnailInfoLabel.setForeground(new Color(100, 100, 100));

        JButton uploadThumbnailBtn = new JButton("Upload Image");
        uploadThumbnailBtn.setBackground(new Color(30, 64, 175));
        uploadThumbnailBtn.setForeground(Color.WHITE);
        uploadThumbnailBtn.setFocusPainted(false);
        uploadThumbnailBtn.setBorderPainted(false);
        uploadThumbnailBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadThumbnailBtn.setPreferredSize(new Dimension(120, 30));

        final String[] thumbnailPath = {null};

        uploadThumbnailBtn.addActionListener(e -> {
            String path = FileUtil.uploadThumbnailTemp(this);
            if (path != null) {
                thumbnailPath[0] = path;
                thumbnailInfoLabel.setText(FileUtil.getImageInfo(path));
            }
        });

        thumbnailPanel.add(thumbnailInfoLabel, BorderLayout.CENTER);
        thumbnailPanel.add(uploadThumbnailBtn, BorderLayout.EAST);
        formPanel.add(thumbnailPanel, gbc);

        // Submit button
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        JButton createButton = new JButton("Create Course");
        createButton.setBackground(new Color(30, 64, 175)); // Navy blue
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setBorderPainted(false);
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createButton.setPreferredSize(new Dimension(150, 35));
        createButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String difficulty = (String) difficultyCombo.getSelectedItem();
            int hours = (Integer) hoursSpinner.getValue();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a course title", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a course description", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Course course = new Course();
                course.setInstructorId(currentUser.getId());
                course.setTitle(title);
                course.setDescription(description);
                course.setCategory(category);
                course.setDifficultyLevel(difficulty);
                course.setEstimatedHours(hours);
                course.setThumbnailPath(thumbnailPath[0]); // Temp path for now

                boolean success = courseService.createCourse(course, currentUser.getRole());

                if (success) {
                    // Move thumbnail from temp to course directory if uploaded
                    if (thumbnailPath[0] != null) {
                        String finalPath = FileUtil.moveThumbnailFromTemp(thumbnailPath[0], course.getId());
                        if (finalPath != null) {
                            course.setThumbnailPath(finalPath);
                            courseService.updateCourse(course, currentUser.getId(), currentUser.getRole());
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                            "Course created successfully! It will be PENDING until admin approval.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Clear form
                    titleField.setText("");
                    descriptionArea.setText("");
                    thumbnailPath[0] = null;
                    thumbnailInfoLabel.setText("No image selected");
                    hoursSpinner.setValue(10);

                    // Refresh courses list
                    loadCourses();

                    // Switch to courses panel
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, "My Courses");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create course", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(createButton, gbc);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadCourses() {
        try {
            List<Course> courses = courseService.getInstructorCourses(
                    currentUser.getId(),
                    currentUser.getRole(),
                    currentUser.getId()
            );

            // Clear existing rows
            coursesTableModel.setRowCount(0);

            // Add courses to table
            for (Course course : courses) {
                Object[] row = {
                        course.getId(),
                        course.getTitle(),
                        course.getCategory(),
                        course.getStatus(),
                        course.isPublished() ? "Yes" : "No",
                        course.getEnrollmentCount(),
                        String.format("%.1f", course.getAverageRating()),
                        "Actions"
                };
                coursesTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading courses: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with title and refresh button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Instructor Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 33, 33));

        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> refreshStatistics());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Main content panel with scrolling
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        try {
            AnalyticsService.InstructorStatistics stats = analyticsService.getInstructorStatistics(
                    currentUser.getId(),
                    currentUser.getRole()
            );

            // Statistics cards panel
            JPanel statsCardsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
            statsCardsPanel.setBackground(Color.WHITE);
            statsCardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Create stat cards
            statsCardsPanel.add(createStatCard("Total Courses", String.valueOf(stats.totalCourses),
                    new Color(52, 152, 219))); // Blue
            statsCardsPanel.add(createStatCard("Approved Courses", String.valueOf(stats.approvedCourses),
                    new Color(46, 204, 113))); // Green
            statsCardsPanel.add(createStatCard("Published Courses", String.valueOf(stats.publishedCourses),
                    new Color(155, 89, 182))); // Purple
            statsCardsPanel.add(createStatCard("Total Students", String.valueOf(stats.totalStudents),
                    new Color(52, 152, 219))); // Blue
            statsCardsPanel.add(createStatCard("Total Reviews", String.valueOf(stats.totalReviews),
                    new Color(241, 196, 15))); // Yellow
            statsCardsPanel.add(createStatCard("Average Rating", String.format("%.1f / 5.0", stats.averageRating),
                    new Color(230, 126, 34))); // Orange
            statsCardsPanel.add(createStatCard("Avg Enrollments/Course", String.format("%.1f", stats.averageEnrollmentsPerCourse),
                    new Color(26, 188, 156))); // Teal
            statsCardsPanel.add(createStatCard("Completion Rate", String.format("%.1f%%", stats.completionRate),
                    new Color(46, 204, 113))); // Green

            contentPanel.add(statsCardsPanel);

            // Charts panel
            JPanel chartsPanel = new JPanel(new GridLayout(1, 1, 15, 15));
            chartsPanel.setBackground(Color.WHITE);
            chartsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

            // Course performance chart
            ChartPanel performanceChart = ChartUtil.createInstructorPerformanceChart(
                    stats.totalCourses,
                    stats.approvedCourses,
                    stats.publishedCourses
            );
            chartsPanel.add(performanceChart);

            contentPanel.add(chartsPanel);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading statistics: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            contentPanel.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void refreshStatistics() {
        // Remove and recreate the statistics panel
        Component[] components = contentPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if ("Statistics".equals(components[i].getName())) {
                contentPanel.remove(i);
                break;
            }
        }
        JPanel statsPanel = createStatisticsPanel();
        statsPanel.setName("Statistics");
        contentPanel.add(statsPanel, "Statistics");
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "Statistics");
    }

    private void logout() {
        SessionManager.getInstance().logout();
        dispose();
        SwingUtilities.invokeLater(() -> {
            com.elearning.ui.LoginFrame loginFrame = new com.elearning.ui.LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    // Button renderer for Actions column
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

    // Button editor for Actions column
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private int currentRow;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                int row = currentRow;
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showActionsMenu(row));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            label = (value == null) ? "Actions" : value.toString();
            button.setText(label);
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        private void showActionsMenu(int row) {
            if (row >= 0 && table != null) {
                int courseId = (Integer) coursesTableModel.getValueAt(row, 0);
                String status = (String) coursesTableModel.getValueAt(row, 3);
                boolean published = coursesTableModel.getValueAt(row, 4).equals("Yes");

                JPopupMenu menu = new JPopupMenu();

                JMenuItem viewItem = new JMenuItem("View Details");
                viewItem.addActionListener(e -> {
                    com.elearning.ui.components.CourseDetailsDialog dialog =
                            new com.elearning.ui.components.CourseDetailsDialog(InstructorDashboard.this, courseId);
                    dialog.setVisible(true);
                    loadCourses(); // Refresh list after dialog closes
                });
                menu.add(viewItem);

                JMenuItem lessonsItem = new JMenuItem("Manage Lessons");
                lessonsItem.addActionListener(e -> {
                    com.elearning.ui.components.CourseDetailsDialog dialog =
                            new com.elearning.ui.components.CourseDetailsDialog(InstructorDashboard.this, courseId);
                    dialog.setVisible(true);
                    loadCourses();
                });
                menu.add(lessonsItem);

                if ("APPROVED".equals(status)) {
                    JMenuItem publishItem = new JMenuItem(published ? "Unpublish" : "Publish");
                    publishItem.addActionListener(e -> togglePublish(courseId, !published));
                    menu.add(publishItem);
                }

                if (!"APPROVED".equals(status) || !published) {
                    JMenuItem editItem = new JMenuItem("Edit");
                    editItem.addActionListener(e -> editCourse(courseId));
                    menu.add(editItem);

                    JMenuItem deleteItem = new JMenuItem("Delete");
                    deleteItem.addActionListener(e -> deleteCourse(courseId));
                    menu.add(deleteItem);
                }

                // Show popup relative to the table cell location
                Rectangle cellRect = table.getCellRect(row, table.getColumnCount() - 1, true);
                menu.show(table, cellRect.x, cellRect.y + cellRect.height);
            }
        }
    }

    private void viewCourse(int courseId) {
        com.elearning.ui.components.CourseDetailsDialog dialog =
                new com.elearning.ui.components.CourseDetailsDialog(this, courseId);
        dialog.setVisible(true);
        loadCourses();
    }

    private void editCourse(int courseId) {
        com.elearning.ui.components.CourseDetailsDialog dialog =
                new com.elearning.ui.components.CourseDetailsDialog(this, courseId);
        dialog.setVisible(true);
        loadCourses();
    }

    private void deleteCourse(int courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this course?\nThis cannot be undone if there are enrollments.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = courseService.deleteCourse(courseId, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Course deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete course. It may have enrollments.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void togglePublish(int courseId, boolean publish) {
        try {
            boolean success = courseService.togglePublishCourse(courseId, currentUser.getId(), publish, currentUser.getRole());
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Course " + (publish ? "published" : "unpublished") + " successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update course", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manageLessons(int courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Course not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create lesson management dialog
        JDialog dialog = new JDialog(this, "Manage Lessons - " + course.getTitle(), true);
        dialog.setSize(1000, 700);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel with course info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Lessons for: " + course.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 33, 33));

        JButton createLessonButton = new JButton("Create New Lesson");
        createLessonButton.setBackground(new Color(46, 204, 113));
        createLessonButton.setForeground(Color.WHITE);
        createLessonButton.setFocusPainted(false);
        createLessonButton.setBorderPainted(false);
        createLessonButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createLessonButton.addActionListener(e -> showCreateLessonDialog(dialog, courseId));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> {
            dialog.dispose();
            manageLessons(courseId);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(createLessonButton);
        buttonPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Lessons table
        String[] columnNames = {"Order", "ID", "Title", "Duration (min)", "Preview", "Video", "Actions"};
        DefaultTableModel lessonsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column
            }
        };

        JTable lessonsTable = new JTable(lessonsModel);
        lessonsTable.setBackground(Color.WHITE);
        lessonsTable.setForeground(new Color(33, 33, 33));
        lessonsTable.setGridColor(new Color(230, 230, 230));
        lessonsTable.setRowHeight(35);
        lessonsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        lessonsTable.getColumn("Actions").setCellEditor(new LessonButtonEditor(new JCheckBox(), lessonsModel, courseId, dialog));

        // Load lessons
        try {
            List<Lesson> lessons = lessonService.getCourseLessons(courseId, currentUser.getRole(), currentUser.getId(), false);
            for (Lesson lesson : lessons) {
                Object[] row = {
                        lesson.getOrderIndex(),
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0,
                        lesson.isPreview() ? "Yes" : "No",
                        lesson.getVideoPath() != null ? "✓" : "✗",
                        "Actions"
                };
                lessonsModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                    "Error loading lessons: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(lessonsTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Bottom panel with statistics
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        try {
            LessonService.CourseStatistics stats = lessonService.getCourseStatistics(courseId);
            JLabel statsLabel = new JLabel(String.format("Total Lessons: %d | Total Duration: %.1f hours",
                    stats.getLessonCount(), stats.getTotalDurationHours()));
            statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statsLabel.setForeground(new Color(88, 88, 88));
            bottomPanel.add(statsLabel);
        } catch (Exception e) {
            // Ignore statistics error
        }

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showCreateLessonDialog(JDialog parentDialog, int courseId) {
        JDialog dialog = new JDialog(parentDialog, "Create New Lesson", true);
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        ModernTextField titleField = new ModernTextField("Lesson Title");
        JTextArea descriptionArea = new JTextArea(5, 30);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setForeground(new Color(33, 33, 33));
        descriptionArea.setCaretColor(new Color(33, 33, 33));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBackground(Color.WHITE);
        descScrollPane.getViewport().setBackground(Color.WHITE);
        descScrollPane.setPreferredSize(new Dimension(400, 120));

        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));
        durationSpinner.setPreferredSize(new Dimension(300, 40));

        JCheckBox previewCheckbox = new JCheckBox("Allow preview (accessible without enrollment)");
        previewCheckbox.setBackground(Color.WHITE);

        JLabel videoLabel = new JLabel("No video selected");
        videoLabel.setForeground(new Color(88, 88, 88));
        final String[] selectedVideoPath = {null};

        JButton chooseVideoButton = new JButton("Choose & Upload Video File");
        chooseVideoButton.setBackground(new Color(30, 64, 175)); // Navy blue
        chooseVideoButton.setForeground(Color.WHITE);
        chooseVideoButton.setFocusPainted(false);
        chooseVideoButton.setBorderPainted(false);
        chooseVideoButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chooseVideoButton.addActionListener(e -> {
            // Note: We need lesson ID for proper upload, so we'll upload after lesson creation
            // For now, just select the file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".mp4") || name.endsWith(".avi") ||
                            name.endsWith(".mov") || name.endsWith(".mkv") ||
                            name.endsWith(".flv") || name.endsWith(".wmv");
                }

                @Override
                public String getDescription() {
                    return "Video Files (*.mp4, *.avi, *.mov, *.mkv, *.flv, *.wmv)";
                }
            });

            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // Validate file size
                long maxSize = VideoUtil.getMaxFileSize();
                if (selectedFile.length() > maxSize) {
                    JOptionPane.showMessageDialog(dialog,
                            "File size exceeds maximum allowed size (" + VideoUtil.formatFileSize(maxSize) + ")",
                            "File Too Large",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                selectedVideoPath[0] = selectedFile.getAbsolutePath();
                videoLabel.setText("Selected: " + selectedFile.getName() + " (" +
                        VideoUtil.formatFileSize(selectedFile.length()) + ")");
            }
        });

        ModernButton createButton = new ModernButton("Create Lesson");
        createButton.setBackground(new Color(46, 204, 113));
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(149, 165, 166));

        panel.add(createLabel("Lesson Title:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(titleField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Description:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(descScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Duration (minutes):"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(durationSpinner);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(previewCheckbox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Video File:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(chooseVideoButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(videoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        createButton.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String description = descriptionArea.getText().trim();

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a lesson title", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create lesson first (without video path)
                Lesson lesson = new Lesson();
                lesson.setCourseId(courseId);
                lesson.setTitle(title);
                lesson.setDescription(description);
                lesson.setDurationMinutes((Integer) durationSpinner.getValue());
                lesson.setPreview(previewCheckbox.isSelected());
                lesson.setVideoPath(null); // Will be updated after upload

                boolean success = lessonService.createLesson(lesson, currentUser.getId(), currentUser.getRole());
                if (success && lesson.getId() > 0) {
                    // Now upload video if one was selected
                    if (selectedVideoPath[0] != null && !selectedVideoPath[0].isEmpty()) {
                        try {
                            // Copy video file to project directory
                            File sourceFile = new File(selectedVideoPath[0]);
                            String uploadedPath = VideoUtil.uploadVideo(dialog, courseId, lesson.getId());

                            if (uploadedPath != null) {
                                // Update lesson with video path
                                lesson.setVideoPath(uploadedPath);
                                lessonService.updateLesson(lesson, currentUser.getId(), currentUser.getRole());
                            } else {
                                JOptionPane.showMessageDialog(dialog,
                                        "Lesson created but video upload failed. You can edit the lesson to upload the video later.",
                                        "Partial Success",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Lesson created but video upload failed: " + ex.getMessage(),
                                    "Partial Success",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    JOptionPane.showMessageDialog(dialog,
                            "Lesson created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    parentDialog.dispose();
                    manageLessons(courseId);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to create lesson",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JScrollPane dialogScrollPane = new JScrollPane(panel);
        dialogScrollPane.setBackground(Color.WHITE);
        dialogScrollPane.getViewport().setBackground(Color.WHITE);
        dialog.add(dialogScrollPane);
        dialog.setVisible(true);
    }

    private void showEditLessonDialog(JDialog parentDialog, int lessonId, int courseId) {
        Lesson lesson = lessonService.getLessonById(lessonId, currentUser.getRole(), currentUser.getId(), false);
        if (lesson == null) {
            JOptionPane.showMessageDialog(parentDialog, "Lesson not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentDialog, "Edit Lesson", true);
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        ModernTextField titleField = new ModernTextField("Lesson Title");
        titleField.setText(lesson.getTitle());

        JTextArea descriptionArea = new JTextArea(5, 30);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setForeground(new Color(33, 33, 33));
        descriptionArea.setCaretColor(new Color(33, 33, 33));
        descriptionArea.setText(lesson.getDescription() != null ? lesson.getDescription() : "");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBackground(Color.WHITE);
        descScrollPane.getViewport().setBackground(Color.WHITE);
        descScrollPane.setPreferredSize(new Dimension(400, 120));

        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(
                lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 30, 1, 300, 5));
        durationSpinner.setPreferredSize(new Dimension(300, 40));

        JCheckBox previewCheckbox = new JCheckBox("Allow preview (accessible without enrollment)");
        previewCheckbox.setSelected(lesson.isPreview());
        previewCheckbox.setBackground(Color.WHITE);

        JLabel videoLabel = new JLabel(lesson.getVideoPath() != null ?
                "Current: " + new File(lesson.getVideoPath()).getName() : "No video selected");
        videoLabel.setForeground(new Color(88, 88, 88));
        final String[] selectedVideoPath = {lesson.getVideoPath()};

        JButton chooseVideoButton = new JButton("Change Video File");
        chooseVideoButton.setBackground(new Color(30, 64, 175)); // Navy blue
        chooseVideoButton.setForeground(Color.WHITE);
        chooseVideoButton.setFocusPainted(false);
        chooseVideoButton.setBorderPainted(false);
        chooseVideoButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chooseVideoButton.addActionListener(e -> {
            String newVideoPath = VideoUtil.updateVideo(dialog, courseId, lessonId, lesson.getVideoPath());
            if (newVideoPath != null) {
                selectedVideoPath[0] = newVideoPath;
                videoLabel.setText("New video: " + new File(newVideoPath).getName() +
                        " (" + VideoUtil.getVideoInfo(newVideoPath) + ")");
            }
        });

        ModernButton saveButton = new ModernButton("Save Changes");
        saveButton.setBackground(new Color(52, 152, 219));
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(149, 165, 166));

        panel.add(createLabel("Lesson Title:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(titleField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Description:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(descScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Duration (minutes):"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(durationSpinner);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(previewCheckbox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Video File:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(chooseVideoButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(videoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        saveButton.addActionListener(e -> {
            try {
                lesson.setTitle(titleField.getText().trim());
                lesson.setDescription(descriptionArea.getText().trim());
                lesson.setDurationMinutes((Integer) durationSpinner.getValue());
                lesson.setPreview(previewCheckbox.isSelected());
                lesson.setVideoPath(selectedVideoPath[0]);

                boolean success = lessonService.updateLesson(lesson, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "Lesson updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    parentDialog.dispose();
                    manageLessons(courseId);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to update lesson",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JScrollPane dialogScrollPane = new JScrollPane(panel);
        dialogScrollPane.setBackground(Color.WHITE);
        dialogScrollPane.getViewport().setBackground(Color.WHITE);
        dialog.add(dialogScrollPane);
        dialog.setVisible(true);
    }

    private void deleteLesson(JDialog parentDialog, int lessonId, int courseId) {
        int confirm = JOptionPane.showConfirmDialog(parentDialog,
                "Are you sure you want to delete this lesson?\nThis cannot be undone if students have progress.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = lessonService.deleteLesson(lessonId, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(parentDialog,
                            "Lesson deleted successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    parentDialog.dispose();
                    manageLessons(courseId);
                } else {
                    JOptionPane.showMessageDialog(parentDialog,
                            "Failed to delete lesson. It may have student progress.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentDialog,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(33, 33, 33));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Lesson button editor
    class LessonButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private int currentRow;
        private DefaultTableModel tableModel;
        private int courseId;
        private JDialog parentDialog;
        private JTable table;

        public LessonButtonEditor(JCheckBox checkBox, DefaultTableModel model, int courseId, JDialog parentDialog) {
            super(checkBox);
            this.tableModel = model;
            this.courseId = courseId;
            this.parentDialog = parentDialog;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                int row = currentRow;
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showLessonActionsMenu(row));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            label = (value == null) ? "Actions" : value.toString();
            button.setText(label);
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        private void showLessonActionsMenu(int row) {
            if (row >= 0 && table != null) {
                int lessonId = (Integer) tableModel.getValueAt(row, 1);

                JPopupMenu menu = new JPopupMenu();

                JMenuItem editItem = new JMenuItem("Edit");
                editItem.addActionListener(e -> showEditLessonDialog(parentDialog, lessonId, courseId));
                menu.add(editItem);

                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.setForeground(new Color(231, 76, 60));
                deleteItem.addActionListener(e -> deleteLesson(parentDialog, lessonId, courseId));
                menu.add(deleteItem);

                // Show popup relative to the table cell location
                Rectangle cellRect = table.getCellRect(row, table.getColumnCount() - 1, true);
                menu.show(table, cellRect.x, cellRect.y + cellRect.height);
            }
        }
    }
}
