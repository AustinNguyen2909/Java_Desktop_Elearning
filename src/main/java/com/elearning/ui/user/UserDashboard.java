package com.elearning.ui.user;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.ui.components.StarRatingPanel;
import com.elearning.util.CourseCardImageUtil;
import com.elearning.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Student dashboard for browsing and enrolling in courses
 */
public class UserDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    private JPanel contentPanel;
    private JPanel availableCoursesGrid;
    private JPanel myCoursesGrid;
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
        contentPanel.add(createBrowseCoursesPanel(), "Browse Courses");
        contentPanel.add(createMyCoursesPanel(), "My Courses");
        contentPanel.add(createProfilePanel(), "My Profile");

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

        JLabel titleLabel = new JLabel("  Student Dashboard - " + currentUser.getFullName());
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
        sidebar.add(createMenuItem("Browse Courses", "Browse Courses"));
        sidebar.add(createMenuItem("My Courses", "My Courses"));
        sidebar.add(createMenuItem("My Profile", "My Profile"));

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
        menuItem.setBackground(new Color(47, 111, 235));
        menuItem.setForeground(Color.WHITE);
        menuItem.setFocusPainted(false);
        menuItem.setBorderPainted(false);
        menuItem.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        menuItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuItem.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuItem.setMaximumSize(new Dimension(250, 80));
        menuItem.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, panelName);
        });
        return menuItem;
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
        titleLabel.setForeground(new Color(31, 41, 55)); // Dark text

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE); // Light background
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(new Color(31, 41, 55)); // Dark text
        searchPanel.add(searchLbl);
        searchField = new JTextField(20);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(new Color(31, 41, 55));
        searchField.setCaretColor(new Color(31, 41, 55));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(47, 111, 235)); // Navy blue
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchCourses());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(47, 111, 235)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadAvailableCourses());
        searchPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        availableCoursesGrid = new com.elearning.ui.components.ScrollableWrapPanel(
                new com.elearning.ui.components.WrapLayout(FlowLayout.LEFT, 16, 16));
        availableCoursesGrid.setBackground(Color.WHITE);
        availableCoursesGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(availableCoursesGrid);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
        titleLabel.setForeground(new Color(31, 41, 55)); // Dark text

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(47, 111, 235)); // Navy blue
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadMyCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        myCoursesGrid = new com.elearning.ui.components.ScrollableWrapPanel(
                new com.elearning.ui.components.WrapLayout(FlowLayout.LEFT, 16, 16));
        myCoursesGrid.setBackground(Color.WHITE);
        myCoursesGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(myCoursesGrid);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
        titleLabel.setForeground(new Color(31, 41, 55)); // Dark text

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
        usernameLbl.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(usernameLbl, gbc);
        gbc.gridx = 1;
        JLabel usernameVal = new JLabel(currentUser.getUsername());
        usernameVal.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(usernameVal, gbc);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(nameLbl, gbc);
        gbc.gridx = 1;
        JLabel nameVal = new JLabel(currentUser.getFullName());
        nameVal.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(nameVal, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(emailLbl, gbc);
        gbc.gridx = 1;
        JLabel emailVal = new JLabel(currentUser.getEmail());
        emailVal.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(emailVal, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel phoneLbl = new JLabel("Phone:");
        phoneLbl.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(phoneLbl, gbc);
        gbc.gridx = 1;
        JLabel phoneVal = new JLabel(currentUser.getPhone() != null ? currentUser.getPhone() : "N/A");
        phoneVal.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(phoneVal, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel roleLbl = new JLabel("Role:");
        roleLbl.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(roleLbl, gbc);
        gbc.gridx = 1;
        JLabel roleVal = new JLabel(currentUser.getRole());
        roleVal.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(roleVal, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setForeground(new Color(31, 41, 55)); // Dark text
        profilePanel.add(statusLbl, gbc);
        gbc.gridx = 1;
        JLabel statusVal = new JLabel(currentUser.getStatus());
        statusVal.setForeground(new Color(31, 41, 55)); // Dark text
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
        availableCoursesGrid.removeAll();
        for (Course course : courses) {
            // Check if already enrolled
            boolean enrolled = enrollmentService.isEnrolled(currentUser.getId(), course.getId());
            String primaryText = enrolled ? "View Details" : "Enroll";
            Runnable primaryAction = enrolled
                    ? () -> viewCourseDetails(course.getId())
                    : () -> enrollInCourse(course.getId());

            String instructorName = course.getInstructorName() != null ? course.getInstructorName() : "Instructor";
            JPanel card = createCourseCard(
                    course.getTitle(),
                    instructorName,
                    course.getThumbnailPath(),
                    course.getAverageRating(),
                    course.getEnrollmentCount(),
                    course.getCategory(),
                    primaryText,
                    primaryAction,
                    null,
                    null
            );
            availableCoursesGrid.add(card);
        }
        availableCoursesGrid.revalidate();
        availableCoursesGrid.repaint();
    }

    private void loadMyCourses() {
        try {
            myCoursesGrid.removeAll(); // Clear existing cards to prevent duplication
            List<Enrollment> enrollments = enrollmentService.getUserEnrollments(currentUser.getId());

            for (Enrollment enrollment : enrollments) {
                Course course = courseService.getCourseById(enrollment.getCourseId());
                String title = enrollment.getCourseTitle();
                String subtitle = enrollment.getCourseCategory() != null ? enrollment.getCourseCategory() : "N/A";
                String thumbnail = enrollment.getCourseThumbnail();
                double rating = 0.0;
                int learners = 0;
                if (course != null) {
                    title = course.getTitle();
                    subtitle = course.getCategory() != null ? course.getCategory() : subtitle;
                    thumbnail = course.getThumbnailPath();
                    rating = course.getAverageRating();
                    learners = course.getEnrollmentCount();
                }

                String progressText = String.format("Progress: %.1f%%", enrollment.getProgressPercent());

                JPanel card = createCourseCard(
                        title,
                        subtitle,
                        thumbnail,
                        rating,
                        learners,
                        progressText,
                        "Continue",
                        () -> continueCourse(enrollment.getCourseId()),
                        "More",
                        () -> showMyCourseMenu(enrollment.getCourseId())
                );
                myCoursesGrid.add(card);
            }
            myCoursesGrid.revalidate();
            myCoursesGrid.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading enrollments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createCourseCard(String title,
                                    String subtitle,
                                    String thumbnailPath,
                                    double rating,
                                    int learners,
                                    String metaLine,
                                    String primaryText,
                                    Runnable primaryAction,
                                    String secondaryText,
                                    Runnable secondaryAction) {
        JPanel card = new com.elearning.ui.components.CardPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(280, 380));
        card.setMaximumSize(new Dimension(280, 380));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(243, 244, 246));
        imagePanel.setPreferredSize(new Dimension(280, 140));
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        ImageIcon icon = CourseCardImageUtil.loadCourseThumbnail(thumbnailPath, title, 280, 140);
        imageLabel.setIcon(icon);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle != null ? subtitle : "");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StarRatingPanel ratingPanel = new StarRatingPanel(rating, 18, 4);

        JLabel learnersLabel = new JLabel(learners + " learners");
        learnersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        learnersLabel.setForeground(new Color(107, 114, 128));
        learnersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaLabel = new JLabel(metaLine != null ? metaLine : "");
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLabel.setForeground(new Color(75, 85, 99));
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statsRow = new JPanel();
        statsRow.setOpaque(false);
        statsRow.setLayout(new BoxLayout(statsRow, BoxLayout.X_AXIS));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(ratingPanel);
        statsRow.add(Box.createHorizontalStrut(8));
        statsRow.add(learnersLabel);
        statsRow.setMaximumSize(new Dimension(260, 22));

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(260, 44));

        JButton primaryBtn = new JButton(primaryText);
        primaryBtn.setBackground(new Color(47, 111, 235));
        primaryBtn.setForeground(Color.WHITE);
        primaryBtn.setFocusPainted(false);
        primaryBtn.setBorderPainted(false);
        primaryBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        primaryBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        primaryBtn.setPreferredSize(new Dimension(256, 36));
        primaryBtn.setMaximumSize(new Dimension(256, 36));
        primaryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        primaryBtn.setHorizontalAlignment(SwingConstants.CENTER);
        primaryBtn.addActionListener(e -> primaryAction.run());
        actions.add(Box.createVerticalStrut(6));
        actions.add(primaryBtn);

        if (secondaryText != null && secondaryAction != null) {
            JButton secondaryBtn = new JButton(secondaryText);
            secondaryBtn.setBackground(new Color(243, 244, 246));
            secondaryBtn.setForeground(new Color(31, 41, 55));
            secondaryBtn.setFocusPainted(false);
            secondaryBtn.setBorderPainted(true);
            secondaryBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            secondaryBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            secondaryBtn.setPreferredSize(new Dimension(256, 36));
            secondaryBtn.setMaximumSize(new Dimension(256, 36));
            secondaryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            secondaryBtn.setHorizontalAlignment(SwingConstants.CENTER);
            secondaryBtn.addActionListener(e -> secondaryAction.run());
            actions.add(Box.createVerticalStrut(6));
            actions.add(secondaryBtn);
        }

        body.add(titleLabel);
        body.add(Box.createRigidArea(new Dimension(0, 4)));
        body.add(subtitleLabel);
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

    

    private void showMyCourseMenu(int courseId) {
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

        Point location = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(location, this);
        menu.show(this, location.x, location.y);
    }

    private void viewCourseDetails(int courseId) {
        com.elearning.ui.components.CourseDetailsDialog dialog =
                new com.elearning.ui.components.CourseDetailsDialog(this, courseId);
        dialog.setVisible(true);
        loadAvailableCourses();
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

        // Open LessonViewerDialog with video player and progress tracking
        // FEATURE 2 & 3: Video Viewing and Progress Tracking
        LessonViewerDialog dialog = new LessonViewerDialog(this, course);
        dialog.setVisible(true);

        // Refresh course list when dialog closes to show updated progress
        loadMyCourses();
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
                        lesson.isPreview() ? " \u2022 Preview" : ""));
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            }

            if (!isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(31, 41, 55));
            } else {
                label.setBackground(new Color(47, 111, 235));
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

    // no-op
}
