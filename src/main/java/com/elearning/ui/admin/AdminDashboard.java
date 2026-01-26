package com.elearning.ui.admin;

import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin dashboard for course approval and user management
 */
public class AdminDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;

    private JTabbedPane tabbedPane;
    private JTable pendingCoursesTable;
    private DefaultTableModel pendingCoursesModel;
    private JTable allCoursesTable;
    private DefaultTableModel allCoursesModel;

    public AdminDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = new CourseService();

        initComponents();
        loadPendingCourses();
        loadAllCourses();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Admin Dashboard");
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
        tabbedPane.addTab("Pending Courses", createPendingCoursesPanel());
        tabbedPane.addTab("All Courses", createAllCoursesPanel());
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Statistics", createStatisticsPanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(79, 195, 247)); // Light blue
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("  Admin Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createPendingCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with title and refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE); // Light background
        JLabel titleLabel = new JLabel("Pending Course Approvals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPendingCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table
        String[] columnNames = {"ID", "Title", "Instructor", "Category", "Difficulty", "Hours", "Actions"};
        pendingCoursesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column
            }
        };
        pendingCoursesTable = new JTable(pendingCoursesModel);
        pendingCoursesTable.setRowHeight(30);
        pendingCoursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        pendingCoursesTable.getColumn("Actions").setCellEditor(new PendingCourseButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(pendingCoursesTable);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAllCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE); // Light background
        JLabel titleLabel = new JLabel("All Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAllCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table
        String[] columnNames = {"ID", "Title", "Instructor", "Category", "Status", "Published", "Enrollments", "Rating"};
        allCoursesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        allCoursesTable = new JTable(allCoursesModel);
        allCoursesTable.setRowHeight(25);

        // Add double-click to view details
        allCoursesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = allCoursesTable.getSelectedRow();
                    if (row >= 0) {
                        int courseId = (Integer) allCoursesModel.getValueAt(row, 0);
                        viewCourseDetails(courseId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(allCoursesTable);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE); // Light background
        JLabel label = new JLabel("User Management - To be implemented in Phase 4", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(33, 33, 33)); // Dark text
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE); // Light background
        JLabel label = new JLabel("Platform Statistics - To be implemented in Phase 7", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(33, 33, 33)); // Dark text
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void loadPendingCourses() {
        try {
            List<Course> courses = courseService.getPendingCourses(currentUser.getRole());

            pendingCoursesModel.setRowCount(0);

            for (Course course : courses) {
                Object[] row = {
                    course.getId(),
                    course.getTitle(),
                    course.getInstructorName(),
                    course.getCategory(),
                    course.getDifficultyLevel(),
                    course.getEstimatedHours(),
                    "Actions"
                };
                pendingCoursesModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading pending courses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllCourses() {
        try {
            List<Course> courses = courseService.getAllCourses(currentUser.getRole());

            allCoursesModel.setRowCount(0);

            for (Course course : courses) {
                Object[] row = {
                    course.getId(),
                    course.getTitle(),
                    course.getInstructorName(),
                    course.getCategory(),
                    course.getStatus(),
                    course.isPublished() ? "Yes" : "No",
                    course.getEnrollmentCount() != 0 ? course.getEnrollmentCount() : 0,
                    course.getAverageRating() != 0 ? String.format("%.1f", course.getAverageRating()) : "N/A"
                };
                allCoursesModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading courses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewCourseDetails(int courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course != null) {
                String details = String.format(
                    "Course Details:\n\n" +
                    "ID: %d\n" +
                    "Title: %s\n" +
                    "Instructor: %s\n" +
                    "Category: %s\n" +
                    "Difficulty: %s\n" +
                    "Estimated Hours: %d\n" +
                    "Status: %s\n" +
                    "Published: %s\n" +
                    "Created: %s\n" +
                    "Rejection Reason: %s\n\n" +
                    "Description:\n%s",
                    course.getId(),
                    course.getTitle(),
                    course.getInstructorName(),
                    course.getCategory(),
                    course.getDifficultyLevel(),
                    course.getEstimatedHours(),
                    course.getStatus(),
                    course.isPublished() ? "Yes" : "No",
                    course.getCreatedAt(),
                    course.getRejectionReason() != null ? course.getRejectionReason() : "N/A",
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

    private void approveCourse(int courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve this course?\nIt will become visible to students.",
            "Confirm Approval",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = courseService.approveCourse(courseId, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Course approved successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadPendingCourses();
                    loadAllCourses();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to approve course",
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

    private void rejectCourse(int courseId) {
        String reason = JOptionPane.showInputDialog(this,
            "Enter rejection reason:",
            "Reject Course",
            JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            try {
                boolean success = courseService.rejectCourse(courseId, reason, currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Course rejected successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadPendingCourses();
                    loadAllCourses();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to reject course",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else if (reason != null) {
            JOptionPane.showMessageDialog(this,
                "Rejection reason is required",
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

    // Button editor for pending courses
    class PendingCourseButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int currentRow;

        public PendingCourseButtonEditor(JCheckBox checkBox) {
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
                int courseId = (Integer) pendingCoursesModel.getValueAt(currentRow, 0);

                JPopupMenu menu = new JPopupMenu();

                JMenuItem viewItem = new JMenuItem("View Details");
                viewItem.addActionListener(e -> viewCourseDetails(courseId));
                menu.add(viewItem);

                menu.addSeparator();

                JMenuItem approveItem = new JMenuItem("Approve");
                approveItem.addActionListener(e -> approveCourse(courseId));
                menu.add(approveItem);

                JMenuItem rejectItem = new JMenuItem("Reject");
                rejectItem.addActionListener(e -> rejectCourse(courseId));
                menu.add(rejectItem);

                menu.show(button, 0, button.getHeight());
            }
        }
    }
}
