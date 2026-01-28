package com.elearning.ui.components;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LessonService;
import com.elearning.util.FileUtil;
import com.elearning.util.SessionManager;
import com.elearning.util.VideoUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Reusable dialog for viewing and managing course details
 */
public class CourseDetailsDialog extends JDialog {
    private final int courseId;
    private final User currentUser;
    private Course course;

    private final CourseService courseService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;

    private JTabbedPane tabbedPane;
    private JPanel detailsPanel;
    private JPanel lessonsPanel;
    private JPanel studentsPanel;
    private JPanel reviewsPanel;

    // Details tab components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> difficultyCombo;
    private JSpinner hoursSpinner;
    private JLabel thumbnailInfoLabel;
    private String thumbnailPath;

    // Lessons tab components
    private JTable lessonsTable;
    private DefaultTableModel lessonsTableModel;

    // Students tab components
    private JTable studentsTable;
    private DefaultTableModel studentsTableModel;

    public CourseDetailsDialog(Frame owner, int courseId) {
        super(owner, "Course Details", true);
        this.courseId = courseId;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = new CourseService();
        this.lessonService = new LessonService();
        this.enrollmentService = new EnrollmentService();

        loadCourseData();
        initComponents();
        setSize(1000, 750);
        setLocationRelativeTo(owner);
    }

    private void loadCourseData() {
        this.course = courseService.getCourseById(courseId);
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Course not found", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        createDetailsTab();
        createLessonsTab();
        if (!currentUser.getRole().equals("USER")) {
            createStudentsTab();
        }
        createReviewsTab();

        tabbedPane.addTab("Details", detailsPanel);
        tabbedPane.addTab("Lessons", lessonsPanel);
        if (!currentUser.getRole().equals("USER")) {
            tabbedPane.addTab("Enrolled Students", studentsPanel);
        }
        tabbedPane.addTab("Reviews", reviewsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Add Approval Panel for Admin if course is PENDING
        if (currentUser.getRole().equals("ADMIN") && "PENDING".equals(course.getStatus())) {
            add(createApprovalPanel(), BorderLayout.NORTH);
        }

        // Bottom panel with Close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(new Color(243, 244, 246));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void createDetailsTab() {
        detailsPanel = new JPanel(new BorderLayout(15, 15));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        boolean canEdit = currentUser.getRole().equals("ADMIN") || 
                         (currentUser.getRole().equals("INSTRUCTOR") && course.getInstructorId().equals(currentUser.getId()));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        titleField = new JTextField(course.getTitle());
        titleField.setEditable(canEdit);
        formPanel.add(titleField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        String[] categories = {"Programming", "Web Development", "Database", "Mobile Development", "Data Science", "DevOps", "Security", "Other"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(course.getCategory());
        categoryCombo.setEnabled(canEdit);
        formPanel.add(categoryCombo, gbc);

        // Difficulty
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        String[] difficulties = {"BEGINNER", "INTERMEDIATE", "ADVANCED"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setSelectedItem(course.getDifficultyLevel());
        difficultyCombo.setEnabled(canEdit);
        formPanel.add(difficultyCombo, gbc);

        // Estimated Hours
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Estimated Hours:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(
                course.getEstimatedHours() != null ? course.getEstimatedHours() : 10, 1, 1000, 1));
        hoursSpinner.setEnabled(canEdit);
        formPanel.add(hoursSpinner, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(course.getDescription(), 6, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(canEdit);
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        // Thumbnail
        gbc.gridy = 5; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Thumbnail:"), gbc);
        gbc.gridx = 1;
        JPanel thumbPanel = new JPanel(new BorderLayout(10, 0));
        thumbPanel.setBackground(Color.WHITE);
        thumbnailInfoLabel = new JLabel(course.getThumbnailPath() != null ? new File(course.getThumbnailPath()).getName() : "No image");
        thumbPanel.add(thumbnailInfoLabel, BorderLayout.CENTER);
        if (canEdit) {
            JButton uploadBtn = new JButton("Change Image");
            uploadBtn.addActionListener(e -> {
                String path = FileUtil.uploadThumbnailTemp(this);
                if (path != null) {
                    thumbnailPath = path;
                    thumbnailInfoLabel.setText(FileUtil.getImageInfo(path));
                }
            });
            thumbPanel.add(uploadBtn, BorderLayout.EAST);
        }
        formPanel.add(thumbPanel, gbc);

        detailsPanel.add(formPanel, BorderLayout.CENTER);

        if (canEdit) {
            JButton saveButton = new JButton("Update Course");
            saveButton.setBackground(new Color(47, 111, 235));
            saveButton.setForeground(Color.WHITE);
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            saveButton.setPreferredSize(new Dimension(0, 45));
            saveButton.addActionListener(e -> updateCourse());
            detailsPanel.add(saveButton, BorderLayout.SOUTH);
        }
    }

    private void updateCourse() {
        course.setTitle(titleField.getText().trim());
        course.setDescription(descriptionArea.getText().trim());
        course.setCategory((String) categoryCombo.getSelectedItem());
        course.setDifficultyLevel((String) difficultyCombo.getSelectedItem());
        course.setEstimatedHours((Integer) hoursSpinner.getValue());

        try {
            if (thumbnailPath != null) {
                String finalPath = FileUtil.moveThumbnailFromTemp(thumbnailPath, course.getId());
                if (finalPath != null) {
                    course.setThumbnailPath(finalPath);
                }
            }
            boolean success = courseService.updateCourse(course, currentUser.getId(), currentUser.getRole());
            if (success) {
                JOptionPane.showMessageDialog(this, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourseData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update course.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createLessonsTab() {
        lessonsPanel = new JPanel(new BorderLayout(10, 10));
        lessonsPanel.setBackground(Color.WHITE);
        lessonsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        boolean canEdit = currentUser.getRole().equals("ADMIN") || 
                         (currentUser.getRole().equals("INSTRUCTOR") && course.getInstructorId().equals(currentUser.getId()));

        if (canEdit) {
            JButton addLessonBtn = new JButton("Add New Lesson");
            addLessonBtn.setBackground(new Color(34, 197, 94));
            addLessonBtn.setForeground(Color.WHITE);
            addLessonBtn.addActionListener(e -> showCreateLessonDialog());
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            topPanel.setBackground(Color.WHITE);
            topPanel.add(addLessonBtn);
            lessonsPanel.add(topPanel, BorderLayout.NORTH);
        }

        String[] columns = {"Order", "ID", "Title", "Duration", "Preview", "Video", "Actions"};
        lessonsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        lessonsTable = new JTable(lessonsTableModel);
        lessonsTable.setBackground(Color.WHITE);
        lessonsTable.setRowHeight(35);

        // Actions column renderer and editor
        lessonsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        lessonsTable.getColumn("Actions").setCellEditor(new LessonActionEditor(new JCheckBox(), lessonsTable, canEdit));

        lessonsPanel.add(new JScrollPane(lessonsTable), BorderLayout.CENTER);
        loadLessons();
    }

    private void loadLessons() {
        lessonsTableModel.setRowCount(0);
        try {
            List<Lesson> lessons = lessonService.getCourseLessons(courseId, currentUser.getRole(), currentUser.getId(), false);
            for (Lesson lesson : lessons) {
                lessonsTableModel.addRow(new Object[]{
                    lesson.getOrderIndex(),
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getDurationMinutes() + "m",
                    lesson.isPreview() ? "Yes" : "No",
                    lesson.getVideoPath() != null ? "\u2713" : "\u2717",
                    "Actions"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createStudentsTab() {
        studentsPanel = new JPanel(new BorderLayout(10, 10));
        studentsPanel.setBackground(Color.WHITE);
        studentsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"User ID", "Name", "Enrollment Date", "Progress", "Last Accessed"};
        studentsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setBackground(Color.WHITE);
        studentsTable.setRowHeight(30);
        studentsPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        loadStudents();
    }

    private void loadStudents() {
        studentsTableModel.setRowCount(0);
        try {
            Integer instructorId = currentUser.getRole().equals("INSTRUCTOR") ? currentUser.getId() : null;
            List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courseId, currentUser.getRole(), instructorId);
            for (Enrollment e : enrollments) {
                studentsTableModel.addRow(new Object[]{
                    e.getUserId(),
                    e.getUserName(),
                    e.getEnrolledAt(),
                    String.format("%.1f%%", e.getProgressPercent()),
                    e.getLastAccessedAt()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createReviewsTab() {
        reviewsPanel = new JPanel(new BorderLayout());
        reviewsPanel.add(new ReviewsPanel(currentUser, courseId), BorderLayout.CENTER);
    }

    private JPanel createApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(255, 243, 205)); // Light warning yellow
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 238, 186)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel label = new JLabel("This course is currently PENDING approval.");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(133, 100, 4));
        panel.add(label, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton approveBtn = new JButton("Approve Course");
        approveBtn.setBackground(new Color(40, 167, 69));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFocusPainted(false);
        approveBtn.addActionListener(e -> approveCourseAction());

        JButton rejectBtn = new JButton("Reject Course");
        rejectBtn.setBackground(new Color(220, 53, 69));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFocusPainted(false);
        rejectBtn.addActionListener(e -> rejectCourseAction());

        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void approveCourseAction() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to APPROVE this course?\nIt will become visible to students once published.",
            "Confirm Approval", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = courseService.approveCourse(courseId, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Course approved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCourseData();
                    // Refresh UI
                    getContentPane().removeAll();
                    initComponents();
                    revalidate();
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to approve course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rejectCourseAction() {
        String reason = JOptionPane.showInputDialog(this, "Enter rejection reason:", "Reject Course", JOptionPane.QUESTION_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            try {
                boolean success = courseService.rejectCourse(courseId, reason, currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Course rejected successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCourseData();
                    // Refresh UI
                    getContentPane().removeAll();
                    initComponents();
                    revalidate();
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reject course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Helper methods and classes for Actions
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Actions");
            return this;
        }
    }

    class LessonActionEditor extends DefaultCellEditor {
        private JButton button;
        private JTable table;
        private int currentRow;
        private boolean canEdit;

        public LessonActionEditor(JCheckBox checkBox, JTable table, boolean canEdit) {
            super(checkBox);
            this.table = table;
            this.canEdit = canEdit;
            button = new JButton("Actions");
            button.addActionListener(e -> {
                // Store button position before it's removed
                Point buttonLocation = button.getLocationOnScreen();
                int buttonHeight = button.getHeight();

                fireEditingStopped();

                // Show menu using invokeLater to ensure UI is updated
                SwingUtilities.invokeLater(() -> showActionsMenu(buttonLocation, buttonHeight));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        private void showActionsMenu(Point buttonLocation, int buttonHeight) {
            int lessonId = (Integer) lessonsTableModel.getValueAt(currentRow, 1);
            JPopupMenu menu = new JPopupMenu();

            JMenuItem viewItem = new JMenuItem("View Details");
            viewItem.addActionListener(e -> showLessonDetails(lessonId));
            menu.add(viewItem);

            if (canEdit) {
                JMenuItem editItem = new JMenuItem("Edit");
                editItem.addActionListener(e -> showEditLessonDialog(lessonId));
                menu.add(editItem);

                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.setForeground(Color.RED);
                deleteItem.addActionListener(e -> deleteLesson(lessonId));
                menu.add(deleteItem);
            }

            // Show menu relative to the table at the button's previous location
            try {
                Point tableLocation = table.getLocationOnScreen();
                int x = buttonLocation.x - tableLocation.x;
                int y = buttonLocation.y - tableLocation.y + buttonHeight;
                menu.show(table, x, y);
            } catch (IllegalComponentStateException ex) {
                // Fallback: show at the row location if button location can't be determined
                Rectangle cellRect = table.getCellRect(currentRow, table.getColumnCount() - 1, true);
                menu.show(table, cellRect.x, cellRect.y + cellRect.height);
            }
        }
    }

    private void showLessonDetails(int lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId, currentUser.getRole(), currentUser.getId(), false);
        if (lesson != null) {
            String msg = String.format("Title: %s\nDuration: %d min\nPreview: %b\n\nDescription:\n%s",
                    lesson.getTitle(), lesson.getDurationMinutes(), lesson.isPreview(), lesson.getDescription());
            JOptionPane.showMessageDialog(this, msg, "Lesson Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCreateLessonDialog() {
        LessonDialog dialog = new LessonDialog(this, courseId, null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadLessons();
        }
    }

    private void showEditLessonDialog(int lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId, currentUser.getRole(), currentUser.getId(), false);
        if (lesson != null) {
            LessonDialog dialog = new LessonDialog(this, courseId, lesson);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadLessons();
            }
        }
    }

    private void deleteLesson(int lessonId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this lesson?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (lessonService.deleteLesson(lessonId, currentUser.getId(), currentUser.getRole())) {
                loadLessons();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete lesson.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
