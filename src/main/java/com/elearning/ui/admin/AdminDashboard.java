package com.elearning.ui.admin;

import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.service.AnalyticsService;
import com.elearning.service.CourseService;
import com.elearning.service.UserService;
import com.elearning.ui.components.ModernButton;
import com.elearning.ui.components.ModernPasswordField;
import com.elearning.ui.components.ModernTextField;
import com.elearning.ui.components.ScrollableWrapPanel;
import com.elearning.ui.components.StarRatingPanel;
import com.elearning.util.CourseCardImageUtil;
import com.elearning.util.ChartUtil;
import com.elearning.util.SessionManager;
import com.elearning.util.ValidationUtil;
import com.mysql.cj.log.Log;
import org.jfree.chart.ChartPanel;

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
    private final UserService userService;
    private final AnalyticsService analyticsService;

    private JPanel contentPanel;
    private JTable pendingCoursesTable;
    private DefaultTableModel pendingCoursesModel;
    private JPanel allCoursesGrid;
    private JTable usersTable;
    private DefaultTableModel usersModel;
    private JTextField userSearchField;
    private JComboBox<String> roleFilterComboBox;

    public AdminDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = new CourseService();
        this.userService = new UserService();
        this.analyticsService = new AnalyticsService();

        initComponents();
        loadPendingCourses();
        loadAllCourses();
        loadUsers();
    }

    private void initComponents() {
        setTitle("E-Learning Platform - Admin Dashboard");
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
        contentPanel.add(createPendingCoursesPanel(), "Pending Courses");
        contentPanel.add(createAllCoursesPanel(), "All Courses");
        contentPanel.add(createUserManagementPanel(), "User Management");
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

        JLabel titleLabel = new JLabel("  Admin Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(31, 41, 55));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(243, 244, 246));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // Add menu items
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createMenuItem("Pending Courses", "Pending Courses"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createMenuItem("All Courses", "All Courses"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createMenuItem("User Management", "User Management"));
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

    private JPanel createPendingCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with title and refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE); // Light background
        JLabel titleLabel = new JLabel("Pending Course Approvals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(31, 41, 55)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        pendingCoursesTable.setBackground(Color.WHITE);
        pendingCoursesTable.setForeground(new Color(31, 41, 55));
        pendingCoursesTable.setGridColor(new Color(230, 230, 230));
        pendingCoursesTable.setRowHeight(30);
        pendingCoursesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        pendingCoursesTable.getColumn("Actions").setCellEditor(new PendingCourseButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(pendingCoursesTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

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
        titleLabel.setForeground(new Color(31, 41, 55)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadAllCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        allCoursesGrid = new ScrollableWrapPanel(new com.elearning.ui.components.WrapLayout(FlowLayout.LEFT, 16, 16));
        allCoursesGrid.setBackground(Color.WHITE);
        allCoursesGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(allCoursesGrid);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with search and filters
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(31, 41, 55));

        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(new Color(31, 41, 55));
        searchPanel.add(searchLabel);

        userSearchField = new JTextField(20);
        userSearchField.setBackground(Color.WHITE);
        userSearchField.setForeground(new Color(31, 41, 55));
        userSearchField.setCaretColor(new Color(31, 41, 55));
        searchPanel.add(userSearchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(30, 64, 175)); // Navy blue
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchUsers());
        searchPanel.add(searchButton);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(new Color(31, 41, 55));
        searchPanel.add(roleLabel);

        String[] roles = {"All", "ADMIN", "INSTRUCTOR", "USER"};
        roleFilterComboBox = new JComboBox<>(roles);
        roleFilterComboBox.setBackground(Color.WHITE);
        roleFilterComboBox.setForeground(new Color(31, 41, 55));
        roleFilterComboBox.addActionListener(e -> filterUsersByRole());
        searchPanel.add(roleFilterComboBox);

        JButton createUserButton = new JButton("Create New User");
        createUserButton.setBackground(new Color(34, 197, 94));
        createUserButton.setForeground(Color.WHITE);
        createUserButton.setFocusPainted(false);
        createUserButton.setBorderPainted(false);
        createUserButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createUserButton.addActionListener(e -> showCreateUserDialog());
        searchPanel.add(createUserButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadUsers());
        searchPanel.add(refreshButton);

        JPanel topContainerPanel = new JPanel(new BorderLayout());
        topContainerPanel.setBackground(Color.WHITE);
        topContainerPanel.add(titleLabel, BorderLayout.NORTH);
        topContainerPanel.add(searchPanel, BorderLayout.CENTER);

        // Table
        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Role", "Status", "Created", "Actions"};
        usersModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only Actions column
            }
        };
        usersTable = new JTable(usersModel);
        usersTable.setBackground(Color.WHITE);
        usersTable.setForeground(new Color(31, 41, 55));
        usersTable.setGridColor(new Color(230, 230, 230));
        usersTable.setRowHeight(30);
        usersTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        usersTable.getColumn("Actions").setCellEditor(new UserButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topContainerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Platform Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(31, 41, 55));

        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.setBackground(new Color(30, 64, 175)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> refreshStatistics());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Main content with statistics and charts
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Statistics cards grid - responsive
        JPanel statsContent = new JPanel(new GridLayout(0, 2, 15, 15));
        statsContent.setBackground(Color.WHITE);
        statsContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        try {
            AnalyticsService.PlatformStatistics stats = analyticsService.getPlatformStatistics(currentUser.getRole());

            // User statistics
            statsContent.add(createStatCard("Total Users",
                    String.format("%d (%d active)", stats.totalUsers + stats.totalInstructors + stats.totalAdmins, stats.activeUsers),
                    new Color(47, 111, 235)));
            statsContent.add(createStatCard("Students", String.valueOf(stats.totalUsers), new Color(34, 197, 94)));
            statsContent.add(createStatCard("Instructors", String.valueOf(stats.totalInstructors), new Color(155, 89, 182)));
            statsContent.add(createStatCard("Admins", String.valueOf(stats.totalAdmins), new Color(225, 29, 72)));

            // Course statistics
            statsContent.add(createStatCard("Total Courses", String.valueOf(stats.totalCourses), new Color(47, 111, 235)));
            statsContent.add(createStatCard("Approved Courses", String.valueOf(stats.approvedCourses), new Color(34, 197, 94)));
            statsContent.add(createStatCard("Pending Approval", String.valueOf(stats.pendingCourses), new Color(241, 196, 15)));
            statsContent.add(createStatCard("Published Courses", String.valueOf(stats.publishedCourses), new Color(26, 188, 156)));

            // Enrollment statistics
            statsContent.add(createStatCard("Total Enrollments", String.valueOf(stats.totalEnrollments), new Color(47, 111, 235)));
            statsContent.add(createStatCard("Active Enrollments", String.valueOf(stats.activeEnrollments), new Color(34, 197, 94)));
            statsContent.add(createStatCard("Completed Enrollments", String.valueOf(stats.completedEnrollments), new Color(155, 89, 182)));
            statsContent.add(createStatCard("Average Progress", String.format("%.1f%%", stats.averageProgress), new Color(241, 196, 15)));

            // Additional statistics
            statsContent.add(createStatCard("Total Reviews", String.valueOf(stats.totalReviews), new Color(47, 111, 235)));
            statsContent.add(createStatCard("Avg Enrollments/Course", String.format("%.1f", stats.averageEnrollmentsPerCourse), new Color(26, 188, 156)));

            mainContent.add(statsContent);

            // Charts section
            JPanel chartsSection = new JPanel();
            chartsSection.setLayout(new BoxLayout(chartsSection, BoxLayout.Y_AXIS));
            chartsSection.setBackground(Color.WHITE);
            chartsSection.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

            // Charts title
            JLabel chartsTitle = new JLabel("Visual Analytics");
            chartsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            chartsTitle.setForeground(new Color(31, 41, 55));
            chartsTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            chartsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            chartsSection.add(chartsTitle);

            // Charts panel - 3 charts in a responsive grid
            JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            chartsPanel.setBackground(Color.WHITE);
            chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

            // User role distribution pie chart
            ChartPanel userRoleChart = ChartUtil.createUserRolePieChart(
                    stats.totalUsers,
                    stats.totalInstructors,
                    stats.totalAdmins
            );
            userRoleChart.setPreferredSize(new Dimension(280, 250));
            chartsPanel.add(userRoleChart);

            // Course status pie chart
            int rejectedCourses = stats.totalCourses - stats.approvedCourses - stats.pendingCourses;
            ChartPanel courseStatusChart = ChartUtil.createCourseStatusPieChart(
                    stats.approvedCourses,
                    stats.pendingCourses,
                    rejectedCourses
            );
            courseStatusChart.setPreferredSize(new Dimension(280, 250));
            chartsPanel.add(courseStatusChart);

            // Enrollment status pie chart
            ChartPanel enrollmentStatusChart = ChartUtil.createEnrollmentStatusPieChart(
                    stats.activeEnrollments,
                    stats.completedEnrollments
            );
            enrollmentStatusChart.setPreferredSize(new Dimension(280, 250));
            chartsPanel.add(enrollmentStatusChart);

            chartsSection.add(chartsPanel);
            mainContent.add(Box.createRigidArea(new Dimension(0, 10)));
            mainContent.add(chartsSection);

            // Top courses chart (if available)
            List<Course> topCourses = analyticsService.getTopCoursesByEnrollment(5);
            if (!topCourses.isEmpty()) {
                JPanel topCoursesSection = new JPanel();
                topCoursesSection.setLayout(new BoxLayout(topCoursesSection, BoxLayout.Y_AXIS));
                topCoursesSection.setBackground(Color.WHITE);
                topCoursesSection.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

                JLabel topCoursesTitle = new JLabel("Top 5 Courses by Enrollment");
                topCoursesTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                topCoursesTitle.setForeground(new Color(31, 41, 55));
                topCoursesTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                topCoursesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                topCoursesSection.add(topCoursesTitle);

                java.util.List<String> courseTitles = new java.util.ArrayList<>();
                java.util.List<Integer> enrollmentCounts = new java.util.ArrayList<>();
                for (Course course : topCourses) {
                    courseTitles.add(course.getTitle());
                    enrollmentCounts.add(course.getEnrollmentCount());
                }

                ChartPanel topCoursesChart = ChartUtil.createTopCoursesChart(courseTitles, enrollmentCounts, 5);
                topCoursesChart.setPreferredSize(new Dimension(850, 300));
                topCoursesChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
                topCoursesChart.setAlignmentX(Component.LEFT_ALIGNMENT);
                topCoursesSection.add(topCoursesChart);

                mainContent.add(Box.createRigidArea(new Dimension(0, 10)));
                mainContent.add(topCoursesSection);
            }

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading statistics: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            statsContent.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(200, 100));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void refreshStatistics() {
        // Re-create the statistics panel
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

            allCoursesGrid.removeAll();
            for (Course course : courses) {
                allCoursesGrid.add(createAdminCourseCard(course));
            }
            allCoursesGrid.revalidate();
            allCoursesGrid.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading courses: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createAdminCourseCard(Course course) {
        JPanel card = new com.elearning.ui.components.CardPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(280, 350));
        card.setMaximumSize(new Dimension(280, 350));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(243, 244, 246));
        imagePanel.setPreferredSize(new Dimension(280, 140));
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        ImageIcon icon = CourseCardImageUtil.loadCourseThumbnail(
                course.getThumbnailPath(),
                course.getTitle(),
                280,
                140
        );
        imageLabel.setIcon(icon);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel titleLabel = new JLabel(course.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String instructor = course.getInstructorName() != null ? course.getInstructorName() : "Instructor";
        JLabel instructorLabel = new JLabel(instructor);
        instructorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructorLabel.setForeground(new Color(107, 114, 128));
        instructorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StarRatingPanel ratingPanel = new StarRatingPanel(course.getAverageRating(), 18, 4);
        JLabel learnersLabel = new JLabel(course.getEnrollmentCount() + " learners");
        learnersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        learnersLabel.setForeground(new Color(107, 114, 128));
        learnersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statsRow = new JPanel();
        statsRow.setOpaque(false);
        statsRow.setLayout(new BoxLayout(statsRow, BoxLayout.X_AXIS));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(ratingPanel);
        statsRow.add(Box.createHorizontalStrut(8));
        statsRow.add(learnersLabel);
        statsRow.setMaximumSize(new Dimension(260, 22));

        String category = course.getCategory() != null ? course.getCategory() : "General";
        String status = course.getStatus() != null ? course.getStatus() : "UNKNOWN";
        String published = course.isPublished() ? "Published" : "Draft";
        JLabel metaLabel = new JLabel(category + " \u2022 " + status + " \u2022 " + published);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLabel.setForeground(new Color(75, 85, 99));
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(260, 80));

        if ("PENDING".equals(course.getStatus())) {
            JPanel pendingRow = new JPanel();
            pendingRow.setOpaque(false);
            pendingRow.setLayout(new BoxLayout(pendingRow, BoxLayout.X_AXIS));
            pendingRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton approveBtn = new JButton("Approve");
            approveBtn.setBackground(new Color(34, 197, 94));
            approveBtn.setForeground(Color.WHITE);
            approveBtn.setFocusPainted(false);
            approveBtn.setBorderPainted(false);
            approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            approveBtn.setPreferredSize(new Dimension(124, 34));
            approveBtn.setMaximumSize(new Dimension(124, 34));
            approveBtn.addActionListener(e -> approveCourse(course.getId()));

            JButton rejectBtn = new JButton("Reject");
            rejectBtn.setBackground(new Color(225, 29, 72));
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setFocusPainted(false);
            rejectBtn.setBorderPainted(false);
            rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rejectBtn.setPreferredSize(new Dimension(124, 34));
            rejectBtn.setMaximumSize(new Dimension(124, 34));
            rejectBtn.addActionListener(e -> rejectCourse(course.getId()));

            pendingRow.add(approveBtn);
            pendingRow.add(Box.createHorizontalStrut(8));
            pendingRow.add(rejectBtn);
            actions.add(pendingRow);
            actions.add(Box.createVerticalStrut(6));
        }

        JButton detailsButton = new JButton("View Details");
        detailsButton.setBackground(new Color(47, 111, 235));
        detailsButton.setForeground(Color.WHITE);
        detailsButton.setFocusPainted(false);
        detailsButton.setBorderPainted(false);
        detailsButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        detailsButton.setPreferredSize(new Dimension(256, 36));
        detailsButton.setMaximumSize(new Dimension(256, 36));
        detailsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsButton.addActionListener(e -> viewCourseDetails(course.getId()));
        actions.add(detailsButton);

        body.add(titleLabel);
        body.add(Box.createRigidArea(new Dimension(0, 4)));
        body.add(instructorLabel);
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(statsRow);
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(metaLabel);
        body.add(Box.createVerticalGlue());
        body.add(actions);

        card.add(imagePanel, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    // no-op

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
                textArea.setBackground(Color.WHITE);
                textArea.setForeground(new Color(31, 41, 55));
                textArea.setCaretColor(new Color(31, 41, 55));
                textArea.setEditable(false);
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

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers(currentUser.getRole());
            usersModel.setRowCount(0);

            for (User user : users) {
                Object[] row = {
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "N/A",
                        "Actions"
                };
                usersModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchUsers() {
        String keyword = userSearchField.getText().trim();
        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }

        try {
            List<User> users = userService.searchUsers(keyword, currentUser.getRole());
            usersModel.setRowCount(0);

            for (User user : users) {
                Object[] row = {
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "N/A",
                        "Actions"
                };
                usersModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error searching users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterUsersByRole() {
        String selectedRole = (String) roleFilterComboBox.getSelectedItem();
        if ("All".equals(selectedRole)) {
            loadUsers();
            return;
        }

        try {
            List<User> users = userService.getUsersByRole(selectedRole, currentUser.getRole());
            usersModel.setRowCount(0);

            for (User user : users) {
                Object[] row = {
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "N/A",
                        "Actions"
                };
                usersModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error filtering users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateUserDialog() {
        JDialog dialog = new JDialog(this, "Create New User", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        ModernTextField usernameField = new ModernTextField("Username");
        ModernTextField emailField = new ModernTextField("Email");
        ModernTextField fullNameField = new ModernTextField("Full Name");
        ModernTextField phoneField = new ModernTextField("Phone (optional)");
        ModernPasswordField passwordField = new ModernPasswordField("Password");

        String[] roles = {"USER", "INSTRUCTOR", "ADMIN"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setForeground(new Color(31, 41, 55));
        roleComboBox.setPreferredSize(new Dimension(300, 45));

        String[] statuses = {"ACTIVE", "PENDING", "SUSPENDED"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setBackground(Color.WHITE);
        statusComboBox.setForeground(new Color(31, 41, 55));
        statusComboBox.setPreferredSize(new Dimension(300, 45));

        ModernButton createButton = new ModernButton("Create User");
        createButton.setBackground(new Color(34, 197, 94));
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(148, 163, 184));

        panel.add(createLabel("Username:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Email:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Full Name:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(fullNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Phone:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(phoneField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Role:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(roleComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Status:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(statusComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        createButton.addActionListener(e -> {
            try {
                User newUser = new User();
                newUser.setUsername(usernameField.getText().trim());
                newUser.setEmail(emailField.getText().trim());
                newUser.setFullName(fullNameField.getText().trim());
                newUser.setPhone(phoneField.getText().trim());
                newUser.setRole((String) roleComboBox.getSelectedItem());
                newUser.setStatus((String) statusComboBox.getSelectedItem());

                String password = new String(passwordField.getPassword());

                boolean success = userService.createUser(newUser, password, currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "User created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to create user",
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

    private void showEditUserDialog(int userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        ModernTextField usernameField = new ModernTextField("Username");
        usernameField.setText(user.getUsername());
        ModernTextField emailField = new ModernTextField("Email");
        emailField.setText(user.getEmail());
        ModernTextField fullNameField = new ModernTextField("Full Name");
        fullNameField.setText(user.getFullName());
        ModernTextField phoneField = new ModernTextField("Phone (optional)");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");

        String[] roles = {"USER", "INSTRUCTOR", "ADMIN"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setForeground(new Color(31, 41, 55));
        roleComboBox.setSelectedItem(user.getRole());
        roleComboBox.setPreferredSize(new Dimension(300, 45));

        String[] statuses = {"ACTIVE", "PENDING", "SUSPENDED"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setBackground(Color.WHITE);
        statusComboBox.setForeground(new Color(31, 41, 55));
        statusComboBox.setSelectedItem(user.getStatus());
        statusComboBox.setPreferredSize(new Dimension(300, 45));

        ModernButton saveButton = new ModernButton("Save Changes");
        saveButton.setBackground(new Color(47, 111, 235));
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(148, 163, 184));

        panel.add(createLabel("Username:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Email:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Full Name:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(fullNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Phone:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(phoneField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Role:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(roleComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createLabel("Status:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(statusComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        saveButton.addActionListener(e -> {
            try {
                user.setUsername(usernameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setFullName(fullNameField.getText().trim());
                user.setPhone(phoneField.getText().trim());
                user.setRole((String) roleComboBox.getSelectedItem());
                user.setStatus((String) statusComboBox.getSelectedItem());

                boolean success = userService.updateUser(user, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "User updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to update user",
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

    private void showResetPasswordDialog(int userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Reset Password for " + user.getUsername(), true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        ModernPasswordField newPasswordField = new ModernPasswordField("New Password");
        ModernPasswordField confirmPasswordField = new ModernPasswordField("Confirm Password");

        ModernButton resetButton = new ModernButton("Reset Password");
        resetButton.setBackground(new Color(225, 29, 72));
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setBackground(new Color(148, 163, 184));

        panel.add(createLabel("New Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(newPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createLabel("Confirm Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(confirmPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        resetButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog,
                        "Passwords do not match",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean success = userService.updatePassword(userId, newPassword, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "Password reset successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to reset password",
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

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void changeUserStatus(int userId, String newStatus) {
        try {
            boolean success = userService.updateUserStatus(userId, newStatus, currentUser.getRole());
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "User status updated to " + newStatus,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update user status",
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

    private void deleteUser(int userId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this user?\nThis will suspend their account.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.deleteUser(userId, currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "User deleted successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete user",
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

    private void viewUserDetails(int userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            String details = String.format(
                    "User Details:\n\n" +
                            "ID: %d\n" +
                            "Username: %s\n" +
                            "Full Name: %s\n" +
                            "Email: %s\n" +
                            "Phone: %s\n" +
                            "Role: %s\n" +
                            "Status: %s\n" +
                            "Created: %s\n" +
                            "Updated: %s",
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone() != null ? user.getPhone() : "N/A",
                    user.getRole(),
                    user.getStatus(),
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A",
                    user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "N/A"
            );

            JTextArea textArea = new JTextArea(details);
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(new Color(31, 41, 55));
            textArea.setCaretColor(new Color(31, 41, 55));
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBackground(Color.WHITE);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "User Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(31, 41, 55));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
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
        private int currentRow;

        public PendingCourseButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                int row = currentRow;
                fireEditingStopped();
                showActionsMenu(row);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
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
            if (row >= 0) {
                int courseId = (Integer) pendingCoursesModel.getValueAt(row, 0);

                try {
                    com.elearning.ui.components.CourseDetailsDialog dialog =
                            new com.elearning.ui.components.CourseDetailsDialog(AdminDashboard.this, courseId);
                    dialog.setVisible(true);
                    loadPendingCourses();
                    loadAllCourses();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error opening course details: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Button editor for user management
    class UserButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private int currentRow;
        private JTable table;

        public UserButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                int row = currentRow;
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showUserActionsMenu(row));
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

        private void showUserActionsMenu(int row) {
            if (row >= 0 && table != null) {
                int userId = (Integer) usersModel.getValueAt(row, 0);
                String userStatus = (String) usersModel.getValueAt(row, 5);

                JPopupMenu menu = new JPopupMenu();

                JMenuItem viewItem = new JMenuItem("View Details");
                viewItem.addActionListener(e -> viewUserDetails(userId));
                menu.add(viewItem);

                JMenuItem editItem = new JMenuItem("Edit User");
                editItem.addActionListener(e -> showEditUserDialog(userId));
                menu.add(editItem);

                menu.addSeparator();

                JMenuItem resetPasswordItem = new JMenuItem("Reset Password");
                resetPasswordItem.addActionListener(e -> showResetPasswordDialog(userId));
                menu.add(resetPasswordItem);

                menu.addSeparator();

                if (!"ACTIVE".equals(userStatus)) {
                    JMenuItem activateItem = new JMenuItem("Activate");
                    activateItem.addActionListener(e -> changeUserStatus(userId, "ACTIVE"));
                    menu.add(activateItem);
                }

                if (!"SUSPENDED".equals(userStatus)) {
                    JMenuItem suspendItem = new JMenuItem("Suspend");
                    suspendItem.addActionListener(e -> changeUserStatus(userId, "SUSPENDED"));
                    menu.add(suspendItem);
                }

                menu.addSeparator();

                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.setForeground(new Color(225, 29, 72));
                deleteItem.addActionListener(e -> deleteUser(userId));
                menu.add(deleteItem);

                // Show popup relative to the table cell location
                Rectangle cellRect = table.getCellRect(row, table.getColumnCount() - 1, true);
                menu.show(table, cellRect.x, cellRect.y + cellRect.height);
            }
        }
    }
}
