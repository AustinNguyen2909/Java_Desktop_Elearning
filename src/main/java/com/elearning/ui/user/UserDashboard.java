package com.elearning.ui.user;

import com.elearning.model.Certificate;
import com.elearning.model.Course;
import com.elearning.model.CourseTest;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.AuthService;
import com.elearning.service.CertificateService;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LoginLogService;
import com.elearning.service.TestService;
import com.elearning.service.UserService;
import com.elearning.ui.components.LoginCalendarPanel;
import com.elearning.ui.components.StarRatingPanel;
import com.elearning.ui.components.TestTakingDialog;
import com.elearning.ui.components.UITheme;
import com.elearning.util.CourseCardImageUtil;
import com.elearning.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.List;

/**
 * Student dashboard for browsing and enrolling in courses
 */
public class UserDashboard extends JFrame {
    private final User currentUser;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final CertificateService certificateService;
    private final LoginLogService loginLogService;
    private final TestService testService;

    private JPanel contentPanel;
    private JPanel availableCoursesGrid;
    private JPanel myCoursesGrid;
    private JPanel profilePanel;
    private JTextField searchField;
    private YearMonth calendarMonth;
    private LoginCalendarPanel loginCalendarPanel;
    private JLabel calendarMonthLabel;
    private JLabel calendarSummaryLabel;

    public UserDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.courseService = CourseService.getInstance();
        this.enrollmentService = EnrollmentService.getInstance();
        this.userService = UserService.getInstance();
        this.certificateService = CertificateService.getInstance();
        this.loginLogService = LoginLogService.getInstance();
        this.calendarMonth = YearMonth.now();
        this.testService = TestService.getInstance();

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
        mainPanel.setBackground(UITheme.BACKGROUND);

        // Header
        JPanel headerPanel = createHeader();

        // Sidebar + Content layout
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BACKGROUND);

        // Create sidebar
        JPanel sidebar = createSidebar();

        // Create content panel with CardLayout
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.add(createBrowseCoursesPanel(), "Browse Courses");
        contentPanel.add(createMyCoursesPanel(), "My Courses");
        profilePanel = createProfilePanel();
        contentPanel.add(profilePanel, "My Profile");
        contentPanel.add(createCalendarPanel(), "My Calendar");

        contentArea.add(sidebar, BorderLayout.WEST);
        contentArea.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.SURFACE);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.BORDER));

        JLabel titleLabel = new JLabel("  Student Dashboard - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.TEXT);

        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BACKGROUND);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        // Add menu items
        sidebar.add(createMenuItem("Browse Courses", "Browse Courses"));
        sidebar.add(createMenuItem("My Courses", "My Courses"));
        sidebar.add(createMenuItem("My Profile", "My Profile"));
        sidebar.add(createMenuItem("My Calendar", "My Calendar"));

        // Push logout button to bottom
        sidebar.add(Box.createVerticalGlue());

        // Logout button at bottom
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(UITheme.DANGER);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Fira Sans", Font.BOLD, 14));
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(220, 45));
        logoutButton.addActionListener(e -> logout());

        sidebar.add(logoutButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        return sidebar;
    }

    private JButton createMenuItem(String text, String panelName) {
        JButton menuItem = new JButton(text);
        menuItem.setBackground(UITheme.PRIMARY);
        menuItem.setForeground(Color.WHITE);
        menuItem.setFocusPainted(false);
        menuItem.setBorderPainted(false);
        menuItem.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        menuItem.setFont(new Font("Fira Sans", Font.BOLD, 14));
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
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with search
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(UITheme.BACKGROUND);

        JLabel titleLabel = new JLabel("Available Courses");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.TEXT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UITheme.BACKGROUND);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(UITheme.TEXT);
        searchPanel.add(searchLbl);
        searchField = new JTextField(20);
        searchField.setBackground(UITheme.SURFACE);
        searchField.setForeground(UITheme.TEXT);
        searchField.setCaretColor(UITheme.TEXT);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(UITheme.PRIMARY);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setFont(new Font("Fira Sans", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchCourses());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(UITheme.PRIMARY);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Fira Sans", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadAvailableCourses());
        searchPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        availableCoursesGrid = new com.elearning.ui.components.ScrollableWrapPanel(
                new com.elearning.ui.components.WrapLayout(FlowLayout.LEFT, 16, 16));
        availableCoursesGrid.setBackground(UITheme.BACKGROUND);
        availableCoursesGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(availableCoursesGrid);
        scrollPane.setBackground(UITheme.BACKGROUND);
        scrollPane.getViewport().setBackground(UITheme.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMyCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BACKGROUND);
        JLabel titleLabel = new JLabel("My Enrolled Courses");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.TEXT);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(UITheme.PRIMARY);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Fira Sans", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadMyCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        myCoursesGrid = new com.elearning.ui.components.ScrollableWrapPanel(
                new com.elearning.ui.components.WrapLayout(FlowLayout.LEFT, 16, 16));
        myCoursesGrid.setBackground(UITheme.BACKGROUND);
        myCoursesGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(myCoursesGrid);
        scrollPane.setBackground(UITheme.BACKGROUND);
        scrollPane.getViewport().setBackground(UITheme.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(new Color(236, 254, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 20));
        titleLabel.setForeground(new Color(22, 78, 99));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(UITheme.PRIMARY);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("Fira Sans", Font.BOLD, 12));
        refreshButton.setPreferredSize(new Dimension(110, 32));
        refreshButton.addActionListener(e -> {
            reloadCurrentUserData();
            refreshProfilePanel();
        });

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(titleLabel, BorderLayout.WEST);
        headerRow.add(refreshButton, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        // Left profile card
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setPreferredSize(new Dimension(260, 0));

        JPanel profileCard = new com.elearning.ui.components.CardPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel avatarLabel = new JLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setIcon(loadAvatarIcon(currentUser.getAvatarPath(), currentUser.getFullName(), 96));

        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(new Font("Fira Sans", Font.BOLD, 16));
        nameLabel.setForeground(new Color(22, 78, 99));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(displayRole(currentUser.getRole()));
        roleLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(8, 145, 178));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton changePhotoBtn = new JButton("Change Photo");
        changePhotoBtn.setBackground(new Color(8, 145, 178));
        changePhotoBtn.setForeground(Color.WHITE);
        changePhotoBtn.setFocusPainted(false);
        changePhotoBtn.setBorderPainted(false);
        changePhotoBtn.setFont(new Font("Fira Sans", Font.BOLD, 12));
        changePhotoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePhotoBtn.setMaximumSize(new Dimension(200, 36));
        changePhotoBtn.addActionListener(e -> handleAvatarChange(avatarLabel));

        JLabel statusChip = new JLabel(currentUser.getStatus());
        statusChip.setFont(new Font("Fira Sans", Font.BOLD, 11));
        statusChip.setForeground(new Color(5, 150, 105));
        statusChip.setOpaque(true);
        statusChip.setBackground(new Color(209, 250, 229));
        statusChip.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusChip.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileCard.add(avatarLabel);
        profileCard.add(Box.createRigidArea(new Dimension(0, 10)));
        profileCard.add(nameLabel);
        profileCard.add(Box.createRigidArea(new Dimension(0, 4)));
        profileCard.add(roleLabel);
        profileCard.add(Box.createRigidArea(new Dimension(0, 10)));
        profileCard.add(statusChip);
        profileCard.add(Box.createRigidArea(new Dimension(0, 12)));
        profileCard.add(changePhotoBtn);

        if (!"ADMIN".equals(currentUser.getRole())) {
            JButton editProfileBtn = new JButton("Edit Profile");
            editProfileBtn.setBackground(UITheme.ACCENT);
            editProfileBtn.setForeground(Color.WHITE);
            editProfileBtn.setFocusPainted(false);
            editProfileBtn.setBorderPainted(false);
            editProfileBtn.setFont(new Font("Fira Sans", Font.BOLD, 12));
            editProfileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            editProfileBtn.setMaximumSize(new Dimension(200, 36));
            editProfileBtn.addActionListener(e -> showEditProfileDialog());

            JButton changePasswordBtn = new JButton("Change Password");
            changePasswordBtn.setBackground(new Color(15, 118, 110));
            changePasswordBtn.setForeground(Color.WHITE);
            changePasswordBtn.setFocusPainted(false);
            changePasswordBtn.setBorderPainted(false);
            changePasswordBtn.setFont(new Font("Fira Sans", Font.BOLD, 12));
            changePasswordBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            changePasswordBtn.setMaximumSize(new Dimension(200, 36));
            changePasswordBtn.addActionListener(e -> showChangePasswordDialog());

            profileCard.add(Box.createRigidArea(new Dimension(0, 8)));
            profileCard.add(editProfileBtn);
            profileCard.add(Box.createRigidArea(new Dimension(0, 6)));
            profileCard.add(changePasswordBtn);
        }

        leftCol.add(profileCard);

        // Right content sections
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        JPanel personalCard = createProfileSection("Personal Details",
                createProfileRow("Username", currentUser.getUsername()),
                createProfileRow("Full name", currentUser.getFullName()),
                createProfileRow("Email", currentUser.getEmail()),
                createProfileRow("Phone", currentUser.getPhone() != null ? currentUser.getPhone() : "N/A"));

        String joinedAt = currentUser.getCreatedAt() != null
                ? currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                : "N/A";
        JPanel accountCard = createProfileSection("Account",
                createProfileRow("Role", displayRole(currentUser.getRole())),
                createProfileRow("Status", currentUser.getStatus()),
                createProfileRow("Joined", joinedAt));

        JPanel educationCard = createProfileSection("Education & Work",
                createProfileRow("Date of birth",
                        currentUser.getDateOfBirth() != null ? currentUser.getDateOfBirth().toString() : "N/A"),
                createProfileRow("School", valueOrNA(currentUser.getSchool())),
                createProfileRow("Job title", valueOrNA(currentUser.getJobTitle())),
                createProfileRow("Experience", valueOrNA(currentUser.getExperience())));

        rightCol.add(personalCard);
        rightCol.add(Box.createRigidArea(new Dimension(0, 12)));
        rightCol.add(accountCard);
        rightCol.add(Box.createRigidArea(new Dimension(0, 12)));
        rightCol.add(educationCard);

        if ("USER".equals(currentUser.getRole())) {
            rightCol.add(Box.createRigidArea(new Dimension(0, 12)));
            rightCol.add(createCertificatesSection());
        }

        content.add(leftCol, BorderLayout.WEST);
        content.add(rightCol, BorderLayout.CENTER);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("My Calendar");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.TEXT);

        calendarMonthLabel = new JLabel();
        calendarMonthLabel.setFont(new Font("Fira Sans", Font.BOLD, 16));
        calendarMonthLabel.setForeground(UITheme.TEXT);
        calendarMonthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navPanel.setOpaque(false);
        JButton prevBtn = createCalendarNavButton("<");
        JButton nextBtn = createCalendarNavButton(">");
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(UITheme.PRIMARY);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFont(new Font("Fira Sans", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> refreshCalendarData());

        prevBtn.addActionListener(e -> {
            calendarMonth = calendarMonth.minusMonths(1);
            refreshCalendarData();
        });
        nextBtn.addActionListener(e -> {
            calendarMonth = calendarMonth.plusMonths(1);
            refreshCalendarData();
        });

        navPanel.add(prevBtn);
        navPanel.add(nextBtn);
        navPanel.add(refreshBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(calendarMonthLabel, BorderLayout.CENTER);
        header.add(navPanel, BorderLayout.EAST);

        loginCalendarPanel = new LoginCalendarPanel();

        calendarSummaryLabel = new JLabel(" ");
        calendarSummaryLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        calendarSummaryLabel.setForeground(UITheme.MUTED_TEXT);

        panel.add(header, BorderLayout.NORTH);
        panel.add(loginCalendarPanel, BorderLayout.CENTER);
        panel.add(calendarSummaryLabel, BorderLayout.SOUTH);

        refreshCalendarData();

        return panel;
    }

    private JButton createCalendarNavButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(UITheme.SURFACE);
        button.setForeground(UITheme.TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        button.setFont(new Font("Fira Sans", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(36, 30));
        return button;
    }

    private void refreshCalendarData() {
        try {
            Set<LocalDate> loginDates = loginLogService.getLoginDatesForMonth(currentUser.getId(), calendarMonth);
            loginCalendarPanel.setMonth(calendarMonth);
            loginCalendarPanel.setLoginDates(loginDates);
            calendarMonthLabel.setText(calendarMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            if (loginDates.isEmpty()) {
                calendarSummaryLabel.setText("No logins this month yet.");
            } else {
                LocalDate lastLogin = loginDates.stream().max(LocalDate::compareTo).orElse(null);
                String lastLoginText = lastLogin != null
                        ? lastLogin.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        : "N/A";
                calendarSummaryLabel.setText("Logged in on " + loginDates.size() + " day(s). Last: " + lastLoginText);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load calendar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
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
                
                // Add test indicator if course has a test and student completed course
                if (enrollment.getProgressPercent() >= 100.0) {
                    CourseTest test = testService.getTestByCourseId(enrollment.getCourseId());
                    if (test != null && test.getIsPublished()) {
                        progressText += " • Test Available";
                    }
                }

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
        imagePanel.setBackground(UITheme.BACKGROUND);
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
        titleLabel.setForeground(UITheme.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle != null ? subtitle : "");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(UITheme.MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StarRatingPanel ratingPanel = new StarRatingPanel(rating, 18, 4);

        JLabel learnersLabel = new JLabel(learners + " learners");
        learnersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        learnersLabel.setForeground(UITheme.MUTED_TEXT);
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
        primaryBtn.setBackground(UITheme.PRIMARY);
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
            secondaryBtn.setBackground(UITheme.BACKGROUND);
            secondaryBtn.setForeground(UITheme.TEXT);
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

        // Check if student can take test
        Enrollment enrollment = enrollmentService.getEnrollment(currentUser.getId(), courseId);
        if (enrollment != null && enrollment.getProgressPercent() >= 100.0) {
            JMenuItem testItem = new JMenuItem("Take Test");
            testItem.addActionListener(e -> takeTest(courseId));
            menu.add(testItem);
        }

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

    private void takeTest(int courseId) {
        try {
            // Check if test exists for this course
            CourseTest test = testService.getTestByCourseId(courseId);
            if (test == null) {
                JOptionPane.showMessageDialog(this,
                    "No test is available for this course yet.",
                    "No Test Available",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Check if test is published
            if (!test.getIsPublished()) {
                JOptionPane.showMessageDialog(this,
                    "The test for this course is not yet available.",
                    "Test Not Available",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Verify course completion
            Enrollment enrollment = enrollmentService.getEnrollment(currentUser.getId(), courseId);
            if (enrollment == null || enrollment.getProgressPercent() < 100.0) {
                JOptionPane.showMessageDialog(this,
                    "You must complete 100% of the course lessons before taking the test.\n" +
                    "Current progress: " + String.format("%.1f%%", enrollment != null ? enrollment.getProgressPercent() : 0.0),
                    "Course Not Complete",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Show test information and confirm
            String message = String.format(
                "Test: %s\n\n" +
                "Questions: %d\n" +
                "Passing Score: %.0f%%\n" +
                "Time Limit: %s\n" +
                "Max Attempts: %s\n\n" +
                "Are you ready to take the test?",
                test.getTitle(),
                testService.getQuestions(test.getId()).size(),
                test.getPassingScore(),
                test.getTimeLimitMinutes() != null ? test.getTimeLimitMinutes() + " minutes" : "No limit",
                test.getMaxAttempts() != null ? String.valueOf(test.getMaxAttempts()) : "Unlimited"
            );

            int choice = JOptionPane.showConfirmDialog(this,
                message,
                "Take Test - " + test.getTitle(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                // Open test taking dialog
                TestTakingDialog testDialog = new TestTakingDialog(this, test);
                testDialog.setVisible(true);
                
                // Refresh courses after test completion (in case certificate was earned)
                loadMyCourses();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading test: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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
                label.setForeground(UITheme.TEXT);
            } else {
                label.setBackground(UITheme.PRIMARY);
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

    private JPanel createProfileSection(String title, JPanel... rows) {
        JPanel card = new com.elearning.ui.components.CardPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 14));
        titleLabel.setForeground(new Color(22, 78, 99));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        for (JPanel row : rows) {
            card.add(row);
            card.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        return card;
    }

    private JPanel createProfileRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel key = new JLabel(label);
        key.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        key.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Fira Sans", Font.BOLD, 12));
        val.setForeground(new Color(22, 78, 99));

        row.add(key, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel createCertificatesSection() {
        JPanel card = new com.elearning.ui.components.CardPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel("My Certificates");
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 14));
        titleLabel.setForeground(new Color(22, 78, 99));

        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        List<Certificate> certificates = certificateService.getCertificatesForUser(currentUser.getId());
        if (certificates.isEmpty()) {
            JLabel emptyLabel = new JLabel("Complete a course to earn your first certificate.");
            emptyLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
            emptyLabel.setForeground(UITheme.MUTED_TEXT);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(emptyLabel);
        } else {
            for (Certificate certificate : certificates) {
                listPanel.add(createCertificateItem(certificate));
                listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(0, 220));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.NORTH);
        body.add(scrollPane, BorderLayout.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCertificateItem(Certificate certificate) {
        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(new Color(241, 249, 252));
        item.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel courseLabel = new JLabel(certificate.getCourseTitle());
        courseLabel.setFont(new Font("Fira Sans", Font.BOLD, 13));
        courseLabel.setForeground(UITheme.TEXT);

        String issuedText = certificate.getIssuedAt() != null
                ? certificate.getIssuedAt().toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                : "N/A";
        JLabel metaLabel = new JLabel("Issued: " + issuedText);
        metaLabel.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        metaLabel.setForeground(UITheme.MUTED_TEXT);

        JLabel codeLabel = new JLabel(certificate.getCertificateCode());
        codeLabel.setFont(new Font("Fira Sans", Font.PLAIN, 11));
        codeLabel.setForeground(new Color(100, 116, 139));

        info.add(courseLabel);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(metaLabel);
        info.add(codeLabel);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        JButton viewButton = new JButton("View");
        viewButton.setBackground(UITheme.PRIMARY);
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        viewButton.setBorderPainted(false);
        viewButton.setFont(new Font("Fira Sans", Font.BOLD, 11));
        viewButton.setMaximumSize(new Dimension(110, 28));
        viewButton.addActionListener(e -> showCertificatePreview(certificate));

        JButton downloadButton = new JButton("Download");
        downloadButton.setBackground(UITheme.BACKGROUND);
        downloadButton.setForeground(UITheme.TEXT);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        downloadButton.setFont(new Font("Fira Sans", Font.BOLD, 11));
        downloadButton.setMaximumSize(new Dimension(110, 28));
        downloadButton.addActionListener(e -> downloadCertificate(certificate));

        actions.add(viewButton);
        actions.add(Box.createRigidArea(new Dimension(0, 6)));
        actions.add(downloadButton);

        item.add(info, BorderLayout.CENTER);
        item.add(actions, BorderLayout.EAST);

        return item;
    }

    private void showCertificatePreview(Certificate certificate) {
        Path imagePath = certificateService.ensureCertificateImage(certificate, currentUser.getFullName());
        if (imagePath == null || !Files.exists(imagePath)) {
            JOptionPane.showMessageDialog(this, "Certificate file not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ImageIcon icon = new ImageIcon(imagePath.toString());
        JLabel imageLabel = new JLabel(icon);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getVerticalScrollBar().setBlockIncrement(160);

        JFrame previewFrame = new JFrame("Certificate Preview");
        previewFrame.setSize(860, 620);
        previewFrame.setLocationRelativeTo(this);
        previewFrame.setLayout(new BorderLayout());
        previewFrame.setResizable(true);
        previewFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        toolBar.setBackground(UITheme.SURFACE);

        JButton zoomOutBtn = new JButton("-");
        zoomOutBtn.setBackground(UITheme.BACKGROUND);
        zoomOutBtn.setForeground(UITheme.TEXT);
        zoomOutBtn.setFocusPainted(false);
        zoomOutBtn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        zoomOutBtn.setPreferredSize(new Dimension(32, 28));

        JButton zoomInBtn = new JButton("+");
        zoomInBtn.setBackground(UITheme.PRIMARY);
        zoomInBtn.setForeground(Color.WHITE);
        zoomInBtn.setFocusPainted(false);
        zoomInBtn.setBorderPainted(false);
        zoomInBtn.setPreferredSize(new Dimension(32, 28));

        toolBar.add(zoomOutBtn);
        toolBar.add(zoomInBtn);

        final double[] zoom = {1.0};
        Runnable updateImage = () -> {
            int w = (int) Math.max(200, icon.getIconWidth() * zoom[0]);
            int h = (int) Math.max(200, icon.getIconHeight() * zoom[0]);
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
            imageLabel.revalidate();
        };

        zoomOutBtn.addActionListener(e -> {
            zoom[0] = Math.max(0.5, zoom[0] - 0.1);
            updateImage.run();
        });
        zoomInBtn.addActionListener(e -> {
            zoom[0] = Math.min(2.0, zoom[0] + 0.1);
            updateImage.run();
        });
        previewFrame.add(toolBar, BorderLayout.NORTH);
        previewFrame.add(scrollPane, BorderLayout.CENTER);
        previewFrame.setVisible(true);
    }

    private void downloadCertificate(Certificate certificate) {
        Path imagePath = certificateService.ensureCertificateImage(certificate, currentUser.getFullName());
        if (imagePath == null || !Files.exists(imagePath)) {
            JOptionPane.showMessageDialog(this, "Certificate file not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Certificate");
        chooser.setSelectedFile(new File(certificate.getCertificateCode() + ".png"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            String path = target.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png")) {
                target = new File(path + ".png");
            }
            try {
                Files.copy(imagePath, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Certificate saved successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save certificate: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String valueOrNA(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String displayRole(String role) {
        if (role == null) {
            return "Unknown";
        }
        return switch (role) {
            case "USER" -> "Student";
            case "INSTRUCTOR" -> "Instructor";
            case "ADMIN" -> "Admin";
            default -> role;
        };
    }

    private void reloadCurrentUserData() {
        try {
            User latest = userService.getUserById(currentUser.getId());
            if (latest == null) {
                return;
            }
            currentUser.setUsername(latest.getUsername());
            currentUser.setEmail(latest.getEmail());
            currentUser.setPhone(latest.getPhone());
            currentUser.setFullName(latest.getFullName());
            currentUser.setAvatarPath(latest.getAvatarPath());
            currentUser.setStatus(latest.getStatus());
            currentUser.setRole(latest.getRole());
            currentUser.setDateOfBirth(latest.getDateOfBirth());
            currentUser.setSchool(latest.getSchool());
            currentUser.setJobTitle(latest.getJobTitle());
            currentUser.setExperience(latest.getExperience());
            currentUser.setCreatedAt(latest.getCreatedAt());
            currentUser.setUpdatedAt(latest.getUpdatedAt());
            SessionManager.getInstance().login(currentUser);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to refresh profile: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshProfilePanel() {
        contentPanel.remove(profilePanel);
        profilePanel = createProfilePanel();
        contentPanel.add(profilePanel, "My Profile");
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "My Profile");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showEditProfileDialog() {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(520, 620);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(Color.WHITE);

        JTextField usernameField = new JTextField(currentUser.getUsername());
        usernameField.setEnabled(false);
        JTextField fullNameField = new JTextField(currentUser.getFullName());
        JTextField emailField = new JTextField(currentUser.getEmail());
        JTextField phoneField = new JTextField(valueOrNA(currentUser.getPhone()).equals("N/A") ? "" : currentUser.getPhone());
        JTextField dobField = new JTextField(currentUser.getDateOfBirth() != null
                ? currentUser.getDateOfBirth().toString()
                : "");
        JTextField schoolField = new JTextField(valueOrNA(currentUser.getSchool()).equals("N/A") ? "" : currentUser.getSchool());
        JTextField jobField = new JTextField(valueOrNA(currentUser.getJobTitle()).equals("N/A") ? "" : currentUser.getJobTitle());

        JTextArea experienceArea = new JTextArea(valueOrNA(currentUser.getExperience()).equals("N/A") ? "" : currentUser.getExperience(), 4, 20);
        experienceArea.setLineWrap(true);
        experienceArea.setWrapStyleWord(true);
        JScrollPane experienceScroll = new JScrollPane(experienceArea);

        panel.add(createFieldBlock("Username (read-only)", usernameField));
        panel.add(createFieldBlock("Full name", fullNameField));
        panel.add(createFieldBlock("Email", emailField));
        panel.add(createFieldBlock("Phone", phoneField));
        panel.add(createFieldBlock("Date of birth (YYYY-MM-DD)", dobField));
        panel.add(createFieldBlock("School", schoolField));
        panel.add(createFieldBlock("Job title", jobField));
        panel.add(createFieldBlock("Experience", experienceScroll));

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(UITheme.ACCENT);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setFont(new Font("Fira Sans", Font.BOLD, 12));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(148, 163, 184));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setFont(new Font("Fira Sans", Font.BOLD, 12));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.add(saveButton);
        buttonRow.add(cancelButton);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(buttonRow);

        saveButton.addActionListener(e -> {
            try {
                currentUser.setFullName(fullNameField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setPhone(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
                currentUser.setSchool(schoolField.getText().trim().isEmpty() ? null : schoolField.getText().trim());
                currentUser.setJobTitle(jobField.getText().trim().isEmpty() ? null : jobField.getText().trim());
                currentUser.setExperience(experienceArea.getText().trim().isEmpty() ? null : experienceArea.getText().trim());

                String dobText = dobField.getText().trim();
                if (!dobText.isEmpty()) {
                    currentUser.setDateOfBirth(LocalDate.parse(dobText));
                } else {
                    currentUser.setDateOfBirth(null);
                }

                boolean success = userService.updateUser(currentUser, currentUser.getId(), currentUser.getRole());
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Profile updated successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshProfilePanel();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update profile", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private JPanel createFieldBlock(String label, JComponent field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(Color.WHITE);
        block.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        lbl.setForeground(new Color(71, 85, 105));
        block.add(lbl);
        block.add(Box.createRigidArea(new Dimension(0, 4)));
        field.setFont(new Font("Fira Sans", Font.PLAIN, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        block.add(field);
        return block;
    }

    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "Change Password", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(Color.WHITE);

        JPasswordField oldPassword = new JPasswordField();
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirmPassword = new JPasswordField();

        panel.add(createFieldBlock("Current password", oldPassword));
        panel.add(createFieldBlock("New password", newPassword));
        panel.add(createFieldBlock("Confirm new password", confirmPassword));

        JButton updateBtn = new JButton("Update Password");
        updateBtn.setBackground(new Color(8, 145, 178));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        updateBtn.setFont(new Font("Fira Sans", Font.BOLD, 12));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(148, 163, 184));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFont(new Font("Fira Sans", Font.BOLD, 12));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.add(updateBtn);
        buttonRow.add(cancelBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(buttonRow);

        updateBtn.addActionListener(e -> {
            String oldPass = new String(oldPassword.getPassword());
            String newPass = new String(newPassword.getPassword());
            String confirm = new String(confirmPassword.getPassword());

            if (newPass.isEmpty() || !newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "New passwords do not match", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = AuthService.getInstance().changePassword(currentUser.getId(), oldPass, newPass);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Password updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update password", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleAvatarChange(JLabel avatarLabel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Profile Photo");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "png", "jpg", "jpeg"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        try {
            Path avatarsDir = Paths.get("avatars");
            if (!Files.exists(avatarsDir)) {
                Files.createDirectories(avatarsDir);
            }
            String extension = selected.getName().contains(".")
                    ? selected.getName().substring(selected.getName().lastIndexOf('.') + 1)
                    : "png";
            String fileName = "user_" + currentUser.getId() + "_" + System.currentTimeMillis() + "." + extension;
            Path dest = avatarsDir.resolve(fileName);
            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            currentUser.setAvatarPath(dest.toString());
            userService.updateUser(currentUser, currentUser.getId(), currentUser.getRole());
            avatarLabel.setIcon(loadAvatarIcon(currentUser.getAvatarPath(), currentUser.getFullName(), 96));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update avatar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon loadAvatarIcon(String path, String name, int size) {
        BufferedImage avatarImage;
        try {
            if (path != null && !path.isBlank() && new File(path).exists()) {
                avatarImage = javax.imageio.ImageIO.read(new File(path));
            } else {
                avatarImage = createInitialsAvatar(name, size);
            }
        } catch (Exception e) {
            avatarImage = createInitialsAvatar(name, size);
        }

        BufferedImage circle = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circle.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(0, 0, size, size));
        g2.drawImage(avatarImage, 0, 0, size, size, null);
        g2.dispose();
        return new ImageIcon(circle);
    }

    private BufferedImage createInitialsAvatar(String name, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(8, 145, 178));
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Fira Sans", Font.BOLD, size / 3));
        String initials = "U";
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else {
                initials = ("" + parts[0].charAt(0)).toUpperCase();
            }
        }
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getAscent();
        g2.drawString(initials, (size - textWidth) / 2, (size + textHeight / 2) / 2);
        g2.dispose();
        return image;
    }

    // no-op
}

