package com.elearning.ui.components;

import com.elearning.model.Review;
import com.elearning.model.User;
import com.elearning.service.ReviewService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Reusable panel for displaying and posting course reviews
 */
public class ReviewsPanel extends JPanel {
    private final ReviewService reviewService;
    private final User currentUser;
    private final int courseId;
    private JPanel reviewsListPanel;
    private JLabel averageRatingLabel;
    private JLabel totalReviewsLabel;
    private StarRatingPanel averageStarsPanel;

    public ReviewsPanel(User currentUser, int courseId) {
        this.reviewService = ReviewService.getInstance();
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
        titleLabel.setForeground(new Color(31, 41, 55));

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
        totalReviewsLabel.setForeground(new Color(107, 114, 128));

        statsPanel.add(averageStarsPanel);
        statsPanel.add(averageRatingLabel);
        statsPanel.add(totalReviewsLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        // Post review panel
        JPanel postPanel = new JPanel(new BorderLayout(10, 10));
        postPanel.setBackground(new Color(243, 244, 246));
        postPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel postLabel = new JLabel("Write a Review");
        postLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Rating selector
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setBackground(new Color(243, 244, 246));

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
            BorderFactory.createLineBorder(new Color(215, 222, 232), 1),
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
                JOptionPane.showMessageDialog(this,
                    "Please write a review",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(this,
                        "Review submitted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to submit review",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel postContentPanel = new JPanel(new BorderLayout(5, 5));
        postContentPanel.setBackground(new Color(243, 244, 246));
        postContentPanel.add(postLabel, BorderLayout.NORTH);
        postContentPanel.add(ratingPanel, BorderLayout.CENTER);

        JPanel reviewInputPanel = new JPanel(new BorderLayout(5, 5));
        reviewInputPanel.setBackground(new Color(243, 244, 246));
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
        card.setBackground(new Color(243, 244, 246));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Header with user and rating
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(243, 244, 246));

        JLabel userLabel = new JLabel(review.getUserName() != null ? review.getUserName() : "Anonymous");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(47, 111, 235));

        StarRatingPanel ratingPanel = new StarRatingPanel(review.getRating(), 16, 3);
        ratingPanel.setOpaque(false);

        headerPanel.add(userLabel, BorderLayout.WEST);
        headerPanel.add(ratingPanel, BorderLayout.EAST);

        // Review content
        JTextArea contentArea = new JTextArea(review.getComment());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(new Color(243, 244, 246));
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Date
        String timeText = review.getCreatedAt() != null ? review.getCreatedAt().toString() : "";
        JLabel timeLabel = new JLabel(timeText);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(154, 164, 178));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentArea, BorderLayout.CENTER);
        card.add(timeLabel, BorderLayout.SOUTH);

        return card;
    }

    public void refresh() {
        loadReviews();
    }
}
