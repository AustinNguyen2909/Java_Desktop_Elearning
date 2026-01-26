package com.elearning.ui.instructor;

import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Instructor dashboard for course management
 */
public class InstructorDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    private JTabbedPane tabbedPane;
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;

    public InstructorDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();

        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Instructor Dashboard");
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
        tabbedPane.addTab("My Courses", createCoursesPanel());
        tabbedPane.addTab("Create Course", createNewCoursePanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(129, 212, 250)); // Light cyan blue
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("  Instructor Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        return headerPanel;
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
        coursesTable.setRowHeight(30);
        coursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        coursesTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(coursesTable);

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
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
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

        // Thumbnail Path (optional)
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel thumbLbl = new JLabel("Thumbnail Path:");
        thumbLbl.setForeground(new Color(33, 33, 33)); // Dark text
        formPanel.add(thumbLbl, gbc);

        gbc.gridx = 1;
        JTextField thumbnailField = new JTextField(30);
        formPanel.add(thumbnailField, gbc);

        // Submit button
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        JButton createButton = new JButton("Create Course");
        createButton.setPreferredSize(new Dimension(150, 35));
        createButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String difficulty = (String) difficultyCombo.getSelectedItem();
            int hours = (Integer) hoursSpinner.getValue();
            String thumbnail = thumbnailField.getText().trim();

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
                course.setThumbnailPath(thumbnail.isEmpty() ? null : thumbnail);

                boolean success = courseService.createCourse(course, currentUser.getRole());

                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Course created successfully! It will be PENDING until admin approval.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Clear form
                    titleField.setText("");
                    descriptionArea.setText("");
                    thumbnailField.setText("");
                    hoursSpinner.setValue(10);

                    // Refresh courses list
                    loadCourses();

                    // Switch to courses tab
                    tabbedPane.setSelectedIndex(0);
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
        private boolean clicked;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
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
                int courseId = (Integer) coursesTableModel.getValueAt(currentRow, 0);
                String status = (String) coursesTableModel.getValueAt(currentRow, 3);
                boolean published = coursesTableModel.getValueAt(currentRow, 4).equals("Yes");

                JPopupMenu menu = new JPopupMenu();

                JMenuItem viewItem = new JMenuItem("View Details");
                viewItem.addActionListener(e -> viewCourse(courseId));
                menu.add(viewItem);

                JMenuItem lessonsItem = new JMenuItem("Manage Lessons");
                lessonsItem.addActionListener(e -> manageLessons(courseId));
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

                menu.show(button, 0, button.getHeight());
            }
        }
    }

    private void viewCourse(int courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course != null) {
                String details = String.format(
                    "Course Details:\n\n" +
                    "ID: %d\n" +
                    "Title: %s\n" +
                    "Category: %s\n" +
                    "Difficulty: %s\n" +
                    "Estimated Hours: %d\n" +
                    "Status: %s\n" +
                    "Published: %s\n" +
                    "Enrollments: %d\n" +
                    "Average Rating: %.1f\n\n" +
                    "Description:\n%s",
                    course.getId(),
                    course.getTitle(),
                    course.getCategory(),
                    course.getDifficultyLevel(),
                    course.getEstimatedHours(),
                    course.getStatus(),
                    course.isPublished() ? "Yes" : "No",
                    course.getEnrollmentCount(),
                    course.getAverageRating(),
                    course.getDescription()
                );

                JTextArea textArea = new JTextArea(details);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));

                JOptionPane.showMessageDialog(this, scrollPane, "Course Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCourse(int courseId) {
        JOptionPane.showMessageDialog(this, "Edit course feature will be implemented in Phase 4", "Info", JOptionPane.INFORMATION_MESSAGE);
        // TODO: Implement edit dialog
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
        JOptionPane.showMessageDialog(this, "Lesson management will be implemented in Phase 4", "Info", JOptionPane.INFORMATION_MESSAGE);
        // TODO: Open lesson management dialog
    }
}
