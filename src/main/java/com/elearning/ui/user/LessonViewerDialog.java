package com.elearning.ui.user;

import com.elearning.dao.LessonProgressDAO;
import com.elearning.model.Comment;
import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.model.LessonProgress;
import com.elearning.service.CommentService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LessonService;
import com.elearning.ui.components.VideoPlayerPanel;
import com.elearning.ui.components.UITheme;
import com.elearning.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog for viewing course lessons and tracking progress
 * FEATURE 2 & 3: Video Viewing and Progress Tracking
 */
public class LessonViewerDialog extends JDialog {
    private final Course course;
    private final int userId;
    private List<Lesson> lessons;
    private Lesson currentLesson;
    private Map<Integer, LessonProgress> progressMap;

    private JList<Lesson> lessonList;
    private DefaultListModel<Lesson> lessonListModel;
    private JLabel lessonTitleLabel;
    private JTextArea lessonDescriptionArea;
    private JPanel commentsListPanel;
    private JTextArea commentInputArea;
    private JButton postCommentButton;
    private JScrollPane commentsScrollPane;
    private JLabel commentsTitleLabel;
    private VideoPlayerPanel videoPlayer;
    private JButton markCompleteBtn;
    private JLabel progressLabel;
    private JProgressBar courseProgressBar;

    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final LessonProgressDAO progressDAO;
    private final CommentService commentService;
    private final String currentUserRole;
    private final boolean allowLessonComments;

    public LessonViewerDialog(JFrame parent, Course course) {
        super(parent, course.getTitle() + " - Lessons", true);
        this.course = course;
        this.userId = SessionManager.getInstance().getCurrentUser().getId();
        this.lessonService = LessonService.getInstance();
        this.enrollmentService = EnrollmentService.getInstance();
        this.progressDAO = new LessonProgressDAO();
        this.commentService = CommentService.getInstance();
        this.progressMap = new HashMap<>();
        this.currentUserRole = SessionManager.getInstance().getCurrentUser().getRole();
        this.allowLessonComments = "USER".equals(currentUserRole) || "INSTRUCTOR".equals(currentUserRole);

        initComponents();
        loadLessons();
        loadProgress();

        // Add window closing listener for cleanup
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                disposeResources();
            }
        });
    }

    private void initComponents() {
        setSize(1200, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top panel with progress
        JPanel topPanel = createProgressPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center: Split pane with lesson list (left) and content (right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createLessonListSidebar());
        splitPane.setRightComponent(createContentPanel());
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.2);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Course title
        JLabel titleLabel = new JLabel(course.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.TEXT);

        // Progress info
        JPanel progressInfoPanel = new JPanel(new BorderLayout(5, 5));
        progressInfoPanel.setBackground(Color.WHITE);

        progressLabel = new JLabel("Progress: 0% (0/0 lessons completed)");
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressLabel.setForeground(UITheme.MUTED_TEXT);

        courseProgressBar = new JProgressBar(0, 100);
        courseProgressBar.setPreferredSize(new Dimension(400, 25));
        courseProgressBar.setStringPainted(true);
        courseProgressBar.setForeground(UITheme.ACCENT); // Green
        courseProgressBar.setValue(0);

        progressInfoPanel.add(progressLabel, BorderLayout.NORTH);
        progressInfoPanel.add(courseProgressBar, BorderLayout.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(progressInfoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLessonListSidebar() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, 600));

        // Title
        JLabel titleLabel = new JLabel("Lessons");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Lesson list
        lessonListModel = new DefaultListModel<>();
        lessonList = new JList<>(lessonListModel);
        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonList.setCellRenderer(new LessonCellRenderer());
        lessonList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lessonList.setBackground(Color.WHITE);
        lessonList.setFixedCellHeight(50);

        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lessonList.getSelectedValue() != null) {
                loadSelectedLesson(lessonList.getSelectedValue());
            }
        });

        JScrollPane scrollPane = new JScrollPane(lessonList);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top: Lesson title
        lessonTitleLabel = new JLabel("Select a lesson to begin");
        lessonTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lessonTitleLabel.setForeground(UITheme.TEXT);
        lessonTitleLabel.setBorder(new EmptyBorder(5, 5, 10, 5));
        panel.add(lessonTitleLabel, BorderLayout.NORTH);

        // Center: Video player placeholder
        JPanel videoPanel = new JPanel(new BorderLayout());
        videoPanel.setPreferredSize(new Dimension(980, 580));
        videoPanel.setBackground(Color.BLACK);
        JLabel placeholderLabel = new JLabel("Select a lesson to watch", SwingConstants.CENTER);
        placeholderLabel.setForeground(Color.WHITE);
        placeholderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        videoPanel.add(placeholderLabel, BorderLayout.CENTER);
        panel.add(videoPanel, BorderLayout.CENTER);

        // Bottom: Description, comments, and mark complete button
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 8));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setForeground(UITheme.TEXT);

        lessonDescriptionArea = new JTextArea(3, 40);
        lessonDescriptionArea.setEditable(false);
        lessonDescriptionArea.setLineWrap(true);
        lessonDescriptionArea.setWrapStyleWord(true);
        lessonDescriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lessonDescriptionArea.setBackground(new Color(245, 245, 245));
        lessonDescriptionArea.setForeground(UITheme.MUTED_TEXT);
        lessonDescriptionArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        lessonDescriptionArea.setText("Select a lesson to view description");

        JScrollPane descScrollPane = new JScrollPane(lessonDescriptionArea);
        descScrollPane.setPreferredSize(new Dimension(800, 80));

        // Mark complete button
        markCompleteBtn = new JButton("Complete");
        markCompleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        markCompleteBtn.setBackground(UITheme.ACCENT); // Green
        markCompleteBtn.setForeground(Color.WHITE);
        markCompleteBtn.setFocusPainted(false);
        markCompleteBtn.setBorderPainted(false);
        markCompleteBtn.setPreferredSize(new Dimension(120, 34));
        markCompleteBtn.setMaximumSize(new Dimension(120, 34));
        markCompleteBtn.setToolTipText("Mark Complete");
        markCompleteBtn.setEnabled(false);
        markCompleteBtn.addActionListener(e -> markLessonComplete());

        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBackground(Color.WHITE);
        descPanel.add(descLabel, BorderLayout.NORTH);
        descPanel.add(descScrollPane, BorderLayout.CENTER);

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setBackground(Color.WHITE);
        centerStack.add(descPanel);
        centerStack.add(Box.createRigidArea(new Dimension(0, 10)));
        centerStack.add(createCommentsSection());

        bottomPanel.add(centerStack, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCommentsSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        commentsTitleLabel = new JLabel("Lesson Comments");
        commentsTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        commentsTitleLabel.setForeground(UITheme.TEXT);

        commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(Color.WHITE);
        commentsListPanel.add(new JLabel("Select a lesson to view comments."));

        commentsScrollPane = new JScrollPane(commentsListPanel);
        commentsScrollPane.setBorder(null);
        commentsScrollPane.setPreferredSize(new Dimension(800, 110));
        commentsScrollPane.getVerticalScrollBar().setUnitIncrement(14);

        JPanel inputPanel = new JPanel(new BorderLayout(6, 6));
        inputPanel.setBackground(Color.WHITE);

        commentInputArea = new JTextArea(2, 40);
        commentInputArea.setLineWrap(true);
        commentInputArea.setWrapStyleWord(true);
        commentInputArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        commentInputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JScrollPane inputScroll = new JScrollPane(commentInputArea);
        inputScroll.setBorder(null);

        postCommentButton = new JButton("Post Comment");
        postCommentButton.setBackground(UITheme.PRIMARY);
        postCommentButton.setForeground(Color.WHITE);
        postCommentButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        postCommentButton.setFocusPainted(false);
        postCommentButton.setBorderPainted(false);
        postCommentButton.setPreferredSize(new Dimension(120, 34));
        postCommentButton.addActionListener(e -> postLessonComment(null));

        if (!allowLessonComments) {
            JLabel info = new JLabel("Comments are available for students and instructors only.");
            info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            info.setForeground(UITheme.MUTED_TEXT);
            inputPanel.add(info, BorderLayout.CENTER);
        } else {
            commentInputArea.setEnabled(false);
            postCommentButton.setEnabled(false);
            inputPanel.add(inputScroll, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setBackground(Color.WHITE);
            actions.add(postCommentButton);
            actions.add(markCompleteBtn);

            inputPanel.add(actions, BorderLayout.EAST);
        }

        panel.add(commentsTitleLabel, BorderLayout.NORTH);
        panel.add(commentsScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadLessons() {
        // Get user role from session
        String userRole = SessionManager.getInstance().getCurrentUser().getRole();

        // Load lessons - student is enrolled (this dialog only opens for enrolled courses)
        lessons = lessonService.getCourseLessons(course.getId(), userRole, userId, true);

        lessonListModel.clear();
        for (Lesson lesson : lessons) {
            lessonListModel.addElement(lesson);
        }

        // Select first lesson if available
        if (!lessons.isEmpty()) {
            lessonList.setSelectedIndex(0);
        }
    }

    private void loadProgress() {
        try {
            // Load progress for all lessons
            for (Lesson lesson : lessons) {
                LessonProgress progress = progressDAO.findByUserAndLesson(userId, lesson.getId());
                if (progress != null) {
                    progressMap.put(lesson.getId(), progress);
                } else {
                    // Create default progress entry
                    LessonProgress defaultProgress = new LessonProgress();
                    defaultProgress.setUserId(userId);
                    defaultProgress.setLessonId(lesson.getId());
                    defaultProgress.setCompleted(false);
                    progressMap.put(lesson.getId(), defaultProgress);
                }
            }

            // Update course progress display
            updateCourseProgress();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading progress: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedLesson(Lesson lesson) {
        this.currentLesson = lesson;

        System.out.println("Loading lesson: " + lesson.getTitle() + " (ID: " + lesson.getId() + ")");
        // Update lesson title
        lessonTitleLabel.setText(String.format("Lesson %d: %s",
                lessons.indexOf(lesson) + 1, lesson.getTitle()));

        // Update description
        String description = lesson.getDescription();
        if (description == null || description.trim().isEmpty()) {
            description = "No description available for this lesson.";
        }
        lessonDescriptionArea.setText(description);

        // Get panels first
        JPanel contentPanel = (JPanel) ((JSplitPane) getContentPane().getComponent(1)).getRightComponent();
        JPanel centerPanel = (JPanel) contentPanel.getComponent(1);

        // Handle video
        if (lesson.getVideoPath() != null && !lesson.getVideoPath().isEmpty()) {
            // Validate video file exists first
            java.io.File videoFile = new java.io.File(lesson.getVideoPath());
            if (!videoFile.exists()) {
                System.err.println("Video file not found: " + lesson.getVideoPath());
                // Dispose broken video player if it exists
                if (videoPlayer != null) {
                    System.out.println("Disposing broken video player due to missing file");
                    videoPlayer.dispose();
                    videoPlayer = null;
                }
                // Show error message
                centerPanel.removeAll();
                JLabel errorLabel = new JLabel("Video file not found: " + videoFile.getName(), SwingConstants.CENTER);
                errorLabel.setForeground(Color.RED);
                centerPanel.add(errorLabel, BorderLayout.CENTER);
                centerPanel.revalidate();
                centerPanel.repaint();
            } else if (videoPlayer == null) {
                // Create video player for the first lesson with valid video
                centerPanel.removeAll();
                try {
                    System.out.println("Creating video player for first lesson: " + lesson.getVideoPath());
                    videoPlayer = new VideoPlayerPanel(lesson.getVideoPath());
                    centerPanel.add(videoPlayer, BorderLayout.CENTER);
                    centerPanel.revalidate();
                    centerPanel.repaint();
                    System.out.println("Video player created and added to panel");
                } catch (Exception ex) {
                    System.err.println("Error creating video player: " + ex.getMessage());
                    ex.printStackTrace();
                    videoPlayer = null; // Clear the reference on error
                    JLabel errorLabel = new JLabel("Error loading video: " + ex.getMessage(), SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    centerPanel.add(errorLabel, BorderLayout.CENTER);
                    centerPanel.revalidate();
                    centerPanel.repaint();
                }
            } else if (!videoPlayer.isInitialized()) {
                // Video player exists but failed to initialize - recreate it
                System.out.println("Video player not initialized, disposing and recreating");
                videoPlayer.dispose();
                videoPlayer = null;
                centerPanel.removeAll();
                try {
                    System.out.println("Recreating video player: " + lesson.getVideoPath());
                    videoPlayer = new VideoPlayerPanel(lesson.getVideoPath());
                    centerPanel.add(videoPlayer, BorderLayout.CENTER);
                    centerPanel.revalidate();
                    centerPanel.repaint();
                    System.out.println("Video player recreated successfully");
                } catch (Exception ex) {
                    System.err.println("Error recreating video player: " + ex.getMessage());
                    ex.printStackTrace();
                    videoPlayer = null;
                    JLabel errorLabel = new JLabel("Error loading video: " + ex.getMessage(), SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    centerPanel.add(errorLabel, BorderLayout.CENTER);
                    centerPanel.revalidate();
                    centerPanel.repaint();
                }
            } else {
                // Reuse existing player and just change the video source
                System.out.println("Disposing resources for video: " + videoPlayer.getName());
                System.out.println("Reusing video player, loading new video: " + lesson.getVideoPath());
                boolean loadSuccess = videoPlayer.loadVideo(lesson.getVideoPath());
                if (!loadSuccess) {
                    System.err.println("Failed to load video, disposing and recreating player");
                    videoPlayer.dispose();
                    videoPlayer = null;
                    centerPanel.removeAll();
                    try {
                        System.out.println("Recreating video player after load failure: " + lesson.getVideoPath());
                        videoPlayer = new VideoPlayerPanel(lesson.getVideoPath());
                        centerPanel.add(videoPlayer, BorderLayout.CENTER);
                        centerPanel.revalidate();
                        centerPanel.repaint();
                    } catch (Exception ex) {
                        System.err.println("Error recreating video player: " + ex.getMessage());
                        ex.printStackTrace();
                        videoPlayer = null;
                        JLabel errorLabel = new JLabel("Error loading video: " + ex.getMessage(), SwingConstants.CENTER);
                        errorLabel.setForeground(Color.RED);
                        centerPanel.add(errorLabel, BorderLayout.CENTER);
                        centerPanel.revalidate();
                        centerPanel.repaint();
                    }
                }
            }
        } else {
            // No video available for this lesson
            System.out.println("No video path available for lesson: " + lesson.getTitle());

            // Dispose video player if it exists since we don't need it
            if (videoPlayer != null) {
                System.out.println("Disposing video player - no video for this lesson");
                videoPlayer.dispose();
                videoPlayer = null;
            }

            centerPanel.removeAll();
            JLabel noVideoLabel = new JLabel("No video available for this lesson", SwingConstants.CENTER);
            noVideoLabel.setForeground(new Color(154, 164, 178));
            centerPanel.add(noVideoLabel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
        }

        // Update mark complete button
        LessonProgress progress = progressMap.get(lesson.getId());
        if (progress != null && progress.isCompleted()) {
            markCompleteBtn.setText("Completed");
            markCompleteBtn.setEnabled(false);
            markCompleteBtn.setBackground(new Color(148, 163, 184)); // Gray
            markCompleteBtn.setToolTipText("Completed");
        } else {
            markCompleteBtn.setText("Complete");
            markCompleteBtn.setEnabled(true);
            markCompleteBtn.setBackground(UITheme.ACCENT); // Green
            markCompleteBtn.setToolTipText("Mark Complete");
        }

        // Mark lesson as opened
        try {
            enrollmentService.openLesson(userId, lesson.getId());
        } catch (Exception e) {
            // Ignore errors for opening lesson
        }

        if (allowLessonComments) {
            commentInputArea.setEnabled(true);
            postCommentButton.setEnabled(true);
        }

        loadLessonComments();
    }

    private void loadLessonComments() {
        commentsListPanel.removeAll();
        if (currentLesson == null) {
            commentsListPanel.add(new JLabel("Select a lesson to view comments."));
            commentsListPanel.revalidate();
            commentsListPanel.repaint();
            return;
        }

        try {
            List<Comment> comments = commentService.getTopLevelComments(currentLesson.getId());
            int totalCount = commentService.getCommentCount(currentLesson.getId());
            commentsTitleLabel.setText("Lesson Comments (" + totalCount + ")");

            if (comments.isEmpty()) {
                JLabel emptyLabel = new JLabel("No comments yet. Start the discussion.");
                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                emptyLabel.setForeground(UITheme.MUTED_TEXT);
                commentsListPanel.add(emptyLabel);
            } else {
                for (Comment comment : comments) {
                    commentsListPanel.add(createCommentCard(comment, 0));
                    List<Comment> replies = commentService.getReplies(comment.getId());
                    for (Comment reply : replies) {
                        commentsListPanel.add(createCommentCard(reply, 18));
                    }
                    commentsListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading comments: " + e.getMessage());
            errorLabel.setForeground(UITheme.DANGER);
            commentsListPanel.add(errorLabel);
        }

        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }

    private JPanel createCommentCard(Comment comment, int indent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(new Color(236, 254, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, indent, 6, 0),
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1)
        ));

        String userName = comment.getUserName() != null ? comment.getUserName() : "Anonymous";
        JLabel userLabel = new JLabel(userName);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userLabel.setForeground(new Color(22, 78, 99));

        String timeText = comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : "";
        JLabel timeLabel = new JLabel(timeText);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(154, 164, 178));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(236, 254, 255));
        header.add(userLabel, BorderLayout.WEST);
        header.add(timeLabel, BorderLayout.EAST);

        JTextArea content = new JTextArea(comment.getContent());
        content.setEditable(false);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setBackground(new Color(236, 254, 255));
        content.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionRow.setBackground(new Color(236, 254, 255));
        if (allowLessonComments) {
            JButton replyBtn = new JButton("Reply");
            replyBtn.setBackground(new Color(8, 145, 178));
            replyBtn.setForeground(Color.WHITE);
            replyBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            replyBtn.setFocusPainted(false);
            replyBtn.setBorderPainted(false);
            replyBtn.addActionListener(e -> showLessonReplyDialog(comment));
            actionRow.add(replyBtn);
        }

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.add(actionRow, BorderLayout.SOUTH);

        return card;
    }

    private void postLessonComment(Comment parent) {
        if (!allowLessonComments || currentLesson == null) {
            return;
        }

        String content = commentInputArea.getText().trim();
        if (content.isEmpty()) {
            showStatusDialog("Error", "Please enter a comment.", UITheme.DANGER, UITheme.TEXT);
            return;
        }

        try {
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setLessonId(currentLesson.getId());
            comment.setContent(content);
            if (parent != null) {
                comment.setParentId(parent.getId());
            }

            boolean success = commentService.postComment(comment, userId, currentUserRole);
            if (success) {
                commentInputArea.setText("");
                loadLessonComments();
            } else {
                showStatusDialog("Error", "Failed to post comment.", UITheme.DANGER, UITheme.TEXT);
            }
        } catch (Exception ex) {
            showStatusDialog("Error", "Error: " + ex.getMessage(), UITheme.DANGER, UITheme.TEXT);
        }
    }

    private void showLessonReplyDialog(Comment parentComment) {
        if (parentComment == null) {
            return;
        }

        JTextArea replyArea = new JTextArea(4, 30);
        replyArea.setLineWrap(true);
        replyArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(replyArea);

        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Reply",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String content = replyArea.getText().trim();
            if (content.isEmpty()) {
                showStatusDialog("Error", "Please enter a reply.", UITheme.DANGER, UITheme.TEXT);
                return;
            }
            try {
                Comment comment = new Comment();
                comment.setUserId(userId);
                comment.setLessonId(currentLesson.getId());
                comment.setParentId(parentComment.getId());
                comment.setContent(content);
                boolean success = commentService.postComment(comment, userId, currentUserRole);
                if (success) {
                    loadLessonComments();
                } else {
                    showStatusDialog("Error", "Failed to post reply.", UITheme.DANGER, UITheme.TEXT);
                }
            } catch (Exception ex) {
                showStatusDialog("Error", "Error: " + ex.getMessage(), UITheme.DANGER, UITheme.TEXT);
            }
        }
    }

    private void markLessonComplete() {
        if (currentLesson == null) {
            return;
        }

        try {
            boolean success = enrollmentService.completeLesson(userId, currentLesson.getId());

            if (success) {
                // Update local progress map
                LessonProgress progress = progressMap.get(currentLesson.getId());
                if (progress == null) {
                    progress = new LessonProgress();
                    progress.setUserId(userId);
                    progress.setLessonId(currentLesson.getId());
                    progressMap.put(currentLesson.getId(), progress);
                }
                progress.setCompleted(true);

                // Update UI
                markCompleteBtn.setText("Completed");
                markCompleteBtn.setEnabled(false);
                markCompleteBtn.setBackground(new Color(148, 163, 184)); // Gray
                markCompleteBtn.setToolTipText("Completed");

                // Repaint lesson list to show checkmark
                lessonList.repaint();

                // Update course progress
                updateCourseProgress();

                // Check if course is 100% complete
                int completedCount = (int) progressMap.values().stream()
                        .filter(LessonProgress::isCompleted)
                        .count();

                if (completedCount == lessons.size()) {
                    showStatusDialog(
                            "Course Completed",
                            "Congratulations! You have completed all lessons in this course.",
                            UITheme.ACCENT,
                            UITheme.TEXT
                    );
                } else {
                    showStatusDialog(
                            "Success",
                            "Lesson marked as complete!",
                            UITheme.PRIMARY,
                            UITheme.TEXT
                    );
                }
            } else {
                showStatusDialog(
                        "Error",
                        "Failed to mark lesson as complete. Please try again.",
                        UITheme.DANGER,
                        UITheme.TEXT
                );
            }
        } catch (Exception e) {
            showStatusDialog(
                    "Error",
                    "Error: " + e.getMessage(),
                    UITheme.DANGER,
                    UITheme.TEXT
            );
        }
    }

    private void updateCourseProgress() {
        if (lessons == null || lessons.isEmpty()) {
            return;
        }

        int completedCount = (int) progressMap.values().stream()
                .filter(LessonProgress::isCompleted)
                .count();

        int totalLessons = lessons.size();
        double progressPercent = (double) completedCount / totalLessons * 100.0;
        int progressInt = (int) Math.round(progressPercent);

        // Update progress label
        progressLabel.setText(String.format("Progress: %d%% (%d/%d lessons completed)",
                progressInt, completedCount, totalLessons));

        // Update progress bar
        courseProgressBar.setValue(progressInt);
        courseProgressBar.setString(progressInt + "%");
    }

    private void disposeResources() {
        if (videoPlayer != null) {
            try {
                System.out.println("Disposing resources for video: " + videoPlayer.getName());
                System.out.println("Disposing video player for course: " + course.getTitle());
                videoPlayer.dispose();
                videoPlayer = null;
                System.out.println("Video player disposed successfully");
            } catch (Exception e) {
                System.err.println("Error disposing video player: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        System.out.println("LessonViewerDialog dispose called for course: " + course.getTitle());
        disposeResources();
        super.dispose();
        System.out.println("LessonViewerDialog disposed completely");
    }

    private void showStatusDialog(String title, String message, Color accent, Color textColor) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setBackground(accent);
        dot.setMaximumSize(new Dimension(14, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 16));
        titleLabel.setForeground(textColor);

        header.add(dot, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);

        JLabel body = new JLabel("<html><div style='width:280px;'>" + message + "</div></html>");
        body.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        body.setForeground(UITheme.MUTED_TEXT);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Custom cell renderer for lesson list
     */
    private class LessonCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Lesson) {
                Lesson lesson = (Lesson) value;
                LessonProgress progress = progressMap.get(lesson.getId());

                // Determine icon
                String icon;
                Color textColor;
                if (progress != null && progress.isCompleted()) {
                    icon = "\u2713";
                    textColor = UITheme.ACCENT; // Green
                } else if (lesson.isPreview()) {
                    icon = "\u26D4";
                    textColor = UITheme.TEXT;
                } else {
                    icon = "\u25B6";
                    textColor = UITheme.TEXT;
                }

                // Format text
                String text = String.format("<html><div style='padding:5px;'>" +
                                "<span style='font-size:14px;'>%s</span> " +
                                "<b>%d. %s</b><br>" +
                                "<span style='font-size:11px; color:#666;'>%d minutes</span>" +
                                "</div></html>",
                        icon, index + 1, lesson.getTitle(),
                        lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0);

                label.setText(text);
                label.setForeground(textColor);
            }

            return label;
        }
    }
}

