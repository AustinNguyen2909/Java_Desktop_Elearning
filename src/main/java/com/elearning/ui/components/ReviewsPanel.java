package com.elearning.ui.components;

import com.elearning.model.CourseReviewComment;
import com.elearning.model.Review;
import com.elearning.model.User;
import com.elearning.service.CourseReviewCommentService;
import com.elearning.service.ReviewService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Reusable panel for displaying and posting course reviews
 */
public class ReviewsPanel extends JPanel {
    private final ReviewService reviewService;
    private final CourseReviewCommentService reviewCommentService;
    private final User currentUser;
    private final int courseId;
    private JPanel reviewsListPanel;
    private JLabel averageRatingLabel;
    private JLabel totalReviewsLabel;
    private StarRatingPanel averageStarsPanel;

    public ReviewsPanel(User currentUser, int courseId) {
        this.reviewService = ReviewService.getInstance();
        this.reviewCommentService = CourseReviewCommentService.getInstance();
        this.currentUser = currentUser;
        this.courseId = courseId;

        initComponents();
        loadReviews();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title and stats
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Course Reviews");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT);

        // Rating stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPanel.setBackground(Color.WHITE);

        averageStarsPanel = new StarRatingPanel(0.0, 18, 4);
        averageStarsPanel.setOpaque(false);

        averageRatingLabel = new JLabel("0.0");
        averageRatingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        averageRatingLabel.setForeground(new Color(55, 65, 81));

        totalReviewsLabel = new JLabel("(0 reviews)");
        totalReviewsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        totalReviewsLabel.setForeground(UITheme.MUTED_TEXT);

        statsPanel.add(averageStarsPanel);
        statsPanel.add(averageRatingLabel);
        statsPanel.add(totalReviewsLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        // Post review panel
        JPanel postPanel = new JPanel(new BorderLayout(10, 10));
        postPanel.setBackground(UITheme.BACKGROUND);
        postPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel postLabel = new JLabel("Write a Review");
        postLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Rating selector
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setBackground(UITheme.BACKGROUND);

        JLabel ratingLabel = new JLabel("Rating:");
        ratingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        Integer[] ratings = {1, 2, 3, 4, 5};
        JComboBox<Integer> ratingCombo = new JComboBox<>(ratings);
        ratingCombo.setSelectedItem(5);

        ratingPanel.add(ratingLabel);
        ratingPanel.add(ratingCombo);

        // Review text
        JTextArea reviewTextArea = new JTextArea(4, 40);
        reviewTextArea.setLineWrap(true);
        reviewTextArea.setWrapStyleWord(true);
        reviewTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane reviewScrollPane = new JScrollPane(reviewTextArea);

        JButton submitButton = new JButton("Submit Review");
        submitButton.setBackground(new Color(241, 196, 15));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitButton.addActionListener(e -> {
            int rating = (Integer) ratingCombo.getSelectedItem();
            String content = reviewTextArea.getText().trim();

            if (content.isEmpty()) {
                showStatusDialog("Error", "Please write a review", UITheme.DANGER);
                return;
            }

            try {
                Review review = new Review();
                review.setUserId(currentUser.getId());
                review.setCourseId(courseId);
                review.setRating(rating);
                review.setComment(content);

                boolean success = reviewService.postReview(review, currentUser.getId(), currentUser.getRole());
                if (success) {
                    reviewTextArea.setText("");
                    ratingCombo.setSelectedItem(5);
                    loadReviews(); // Refresh reviews
                    showStatusDialog("Success", "Review submitted successfully!", UITheme.ACCENT);
                } else {
                    showStatusDialog("Error", "Failed to submit review", UITheme.DANGER);
                }
            } catch (Exception ex) {
                showStatusDialog("Error", "Error: " + ex.getMessage(), UITheme.DANGER);
            }
        });

        JPanel postContentPanel = new JPanel(new BorderLayout(5, 5));
        postContentPanel.setBackground(UITheme.BACKGROUND);
        postContentPanel.add(postLabel, BorderLayout.NORTH);
        postContentPanel.add(ratingPanel, BorderLayout.CENTER);

        JPanel reviewInputPanel = new JPanel(new BorderLayout(5, 5));
        reviewInputPanel.setBackground(UITheme.BACKGROUND);
        reviewInputPanel.add(reviewScrollPane, BorderLayout.CENTER);
        reviewInputPanel.add(submitButton, BorderLayout.EAST);

        postPanel.add(postContentPanel, BorderLayout.NORTH);
        postPanel.add(reviewInputPanel, BorderLayout.CENTER);

        // Reviews list
        reviewsListPanel = new JPanel();
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        reviewsListPanel.setBackground(Color.WHITE);

        JScrollPane reviewsScrollPane = new JScrollPane(reviewsListPanel);
        reviewsScrollPane.setBorder(null);
        reviewsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Assemble
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(postPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(reviewsScrollPane, BorderLayout.CENTER);
    }

    private void loadReviews() {
        reviewsListPanel.removeAll();

        try {
            List<Review> reviews = reviewService.getCourseReviews(courseId);
            ReviewService.ReviewStatistics stats = reviewService.getReviewStatistics(courseId);

            // Update stats
            double avg = stats.getAverageRating();
            averageStarsPanel.setRating(avg);
            averageRatingLabel.setText(String.format("%.1f", avg));
            totalReviewsLabel.setText(String.format("(%d reviews)", stats.getTotalReviews()));

            if (reviews.isEmpty()) {
                JLabel noReviewsLabel = new JLabel("No reviews yet. Be the first to review this course!");
                noReviewsLabel.setForeground(new Color(154, 164, 178));
                noReviewsLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                reviewsListPanel.add(noReviewsLabel);
            } else {
                for (Review review : reviews) {
                    reviewsListPanel.add(createReviewCard(review));
                    reviewsListPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading reviews: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            reviewsListPanel.add(errorLabel);
        }

        reviewsListPanel.revalidate();
        reviewsListPanel.repaint();
    }

    private JPanel createReviewCard(Review review) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(UITheme.BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Header with user and rating
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BACKGROUND);

        JLabel userLabel = new JLabel(review.getUserName() != null ? review.getUserName() : "Anonymous");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(UITheme.PRIMARY);

        StarRatingPanel ratingPanel = new StarRatingPanel(review.getRating(), 16, 3);
        ratingPanel.setOpaque(false);

        headerPanel.add(userLabel, BorderLayout.WEST);
        headerPanel.add(ratingPanel, BorderLayout.EAST);

        // Review content
        JTextArea contentArea = new JTextArea(review.getComment());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(UITheme.BACKGROUND);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Date + reply action
        String timeText = review.getCreatedAt() != null ? review.getCreatedAt().toString() : "";
        JLabel timeLabel = new JLabel(timeText);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(154, 164, 178));

        JButton replyButton = new JButton("Reply");
        replyButton.setBackground(new Color(8, 145, 178));
        replyButton.setForeground(Color.WHITE);
        replyButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        replyButton.setFocusPainted(false);
        replyButton.setBorderPainted(false);
        replyButton.addActionListener(e -> showReviewReplyDialog(review, null));

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UITheme.BACKGROUND);
        footerPanel.add(timeLabel, BorderLayout.WEST);
        footerPanel.add(replyButton, BorderLayout.EAST);

        JPanel commentsPanel = createReviewCommentsPanel(review.getId());

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(UITheme.BACKGROUND);
        bodyPanel.add(contentArea);
        if (commentsPanel.getComponentCount() > 0) {
            bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            bodyPanel.add(commentsPanel);
        }
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(footerPanel);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(bodyPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createReviewCommentsPanel(int reviewId) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UITheme.BACKGROUND);
        container.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 0));

        List<CourseReviewComment> comments = reviewCommentService.getTopLevelComments(reviewId);
        for (CourseReviewComment comment : comments) {
            container.add(createReviewCommentCard(comment, reviewId, 0));
            List<CourseReviewComment> replies = reviewCommentService.getReplies(comment.getId());
            for (CourseReviewComment reply : replies) {
                container.add(createReviewCommentCard(reply, reviewId, 18));
            }
        }
        return container;
    }

    private JPanel createReviewCommentCard(CourseReviewComment comment, int reviewId, int indent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(new Color(236, 254, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, indent, 6, 0),
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1)
        ));

        JLabel userLabel = new JLabel(comment.getUserName() != null ? comment.getUserName() : "Anonymous");
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

        JButton replyBtn = new JButton("Reply");
        replyBtn.setBackground(new Color(8, 145, 178));
        replyBtn.setForeground(Color.WHITE);
        replyBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        replyBtn.setFocusPainted(false);
        replyBtn.setBorderPainted(false);
        replyBtn.addActionListener(e -> showReviewReplyDialog(null, comment));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionRow.setBackground(new Color(236, 254, 255));
        actionRow.add(replyBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.add(actionRow, BorderLayout.SOUTH);

        return card;
    }

    private void showReviewReplyDialog(Review review, CourseReviewComment parentComment) {
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
                showStatusDialog("Error", "Please enter a reply", UITheme.DANGER);
                return;
            }
            try {
                CourseReviewComment comment = new CourseReviewComment();
                comment.setUserId(currentUser.getId());
                if (parentComment != null) {
                    comment.setReviewId(parentComment.getReviewId());
                    comment.setParentId(parentComment.getId());
                } else if (review != null) {
                    comment.setReviewId(review.getId());
                } else {
                    return;
                }
                comment.setContent(content);
                boolean success = reviewCommentService.postComment(comment, currentUser.getId());
                if (success) {
                    loadReviews();
                } else {
                    showStatusDialog("Error", "Failed to post reply", UITheme.DANGER);
                }
            } catch (Exception ex) {
                showStatusDialog("Error", "Error: " + ex.getMessage(), UITheme.DANGER);
            }
        }
    }

    private void showStatusDialog(String title, String message, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setBackground(accent);
        dot.setMaximumSize(new Dimension(14, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Fira Sans", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT);

        header.add(dot, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);

        JLabel body = new JLabel("<html><div style='width:280px;'>" + message + "</div></html>");
        body.setFont(new Font("Fira Sans", Font.PLAIN, 13));
        body.setForeground(UITheme.MUTED_TEXT);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    public void refresh() {
        loadReviews();
    }
}

