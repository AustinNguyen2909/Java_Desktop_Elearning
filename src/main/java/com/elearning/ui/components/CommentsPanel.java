package com.elearning.ui.components;

import com.elearning.model.Comment;
import com.elearning.model.User;
import com.elearning.service.CommentService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Reusable panel for displaying and posting comments on lessons
 */
public class CommentsPanel extends JPanel {
    private final CommentService commentService;
    private final User currentUser;
    private final int lessonId;
    private JPanel commentsListPanel;

    public CommentsPanel(User currentUser, int lessonId) {
        this.commentService = new CommentService();
        this.currentUser = currentUser;
        this.lessonId = lessonId;

        initComponents();
        loadComments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Comments & Discussion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(31, 41, 55));

        // Post comment panel
        JPanel postPanel = new JPanel(new BorderLayout(5, 5));
        postPanel.setBackground(Color.WHITE);

        JTextArea commentTextArea = new JTextArea(3, 40);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 222, 232), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane commentScrollPane = new JScrollPane(commentTextArea);

        JButton postButton = new JButton("Post Comment");
        postButton.setBackground(new Color(47, 111, 235));
        postButton.setForeground(Color.WHITE);
        postButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        postButton.addActionListener(e -> {
            String content = commentTextArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a comment",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Comment comment = new Comment();
                comment.setUserId(currentUser.getId());
                comment.setLessonId(lessonId);
                comment.setContent(content);

                boolean success = commentService.postComment(comment, currentUser.getId(), currentUser.getRole());
                if (success) {
                    commentTextArea.setText("");
                    loadComments(); // Refresh comments
                    JOptionPane.showMessageDialog(this,
                        "Comment posted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to post comment",
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

        postPanel.add(commentScrollPane, BorderLayout.CENTER);
        postPanel.add(postButton, BorderLayout.EAST);

        // Comments list
        commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(Color.WHITE);

        JScrollPane commentsScrollPane = new JScrollPane(commentsListPanel);
        commentsScrollPane.setBorder(null);
        commentsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Assemble
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(postPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(commentsScrollPane, BorderLayout.CENTER);
    }

    private void loadComments() {
        commentsListPanel.removeAll();

        try {
            List<Comment> comments = commentService.getTopLevelComments(lessonId);

            if (comments.isEmpty()) {
                JLabel noCommentsLabel = new JLabel("No comments yet. Be the first to comment!");
                noCommentsLabel.setForeground(new Color(154, 164, 178));
                noCommentsLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                commentsListPanel.add(noCommentsLabel);
            } else {
                for (Comment comment : comments) {
                    commentsListPanel.add(createCommentCard(comment));
                    commentsListPanel.add(Box.createVerticalStrut(10));

                    // Load replies
                    List<Comment> replies = commentService.getReplies(comment.getId());
                    for (Comment reply : replies) {
                        JPanel replyCard = createCommentCard(reply);
                        replyCard.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(0, 30, 0, 0),
                            replyCard.getBorder()
                        ));
                        commentsListPanel.add(replyCard);
                        commentsListPanel.add(Box.createVerticalStrut(5));
                    }
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading comments: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            commentsListPanel.add(errorLabel);
        }

        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }

    private JPanel createCommentCard(Comment comment) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(243, 244, 246));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // User info
        JLabel userLabel = new JLabel(comment.getUserName() != null ? comment.getUserName() : "Anonymous");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(47, 111, 235));

        String timeText = comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : "";
        JLabel timeLabel = new JLabel(timeText);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(154, 164, 178));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(243, 244, 246));
        headerPanel.add(userLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);

        // Comment content
        JTextArea contentArea = new JTextArea(comment.getContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(new Color(243, 244, 246));
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentArea, BorderLayout.CENTER);

        return card;
    }

    public void refresh() {
        loadComments();
    }
}
