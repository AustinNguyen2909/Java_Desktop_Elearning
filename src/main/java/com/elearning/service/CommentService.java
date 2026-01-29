package com.elearning.service;

import com.elearning.dao.CommentDAO;
import com.elearning.dao.EnrollmentDAO;
import com.elearning.dao.LessonDAO;
import com.elearning.model.Comment;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;

import java.util.List;

/**
 * Service for comment management
 * Singleton pattern for single instance across application
 */
public class CommentService {
    private final CommentDAO commentDAO;
    private final LessonDAO lessonDAO;
    private final EnrollmentDAO enrollmentDAO;

    // Private constructor to prevent direct instantiation
    private CommentService() {
        this.commentDAO = new CommentDAO();
        this.lessonDAO = new LessonDAO();
        this.enrollmentDAO = new EnrollmentDAO();
    }

    // Static inner holder class - lazily loaded and thread-safe
    private static class SingletonHolder {
        private static final CommentService INSTANCE = new CommentService();
    }

    // Public accessor method
    public static CommentService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Get all comments for a lesson
     */
    public List<Comment> getLessonComments(int lessonId) {
        return commentDAO.findByLessonId(lessonId);
    }

    /**
     * Get top-level comments for a lesson
     */
    public List<Comment> getTopLevelComments(int lessonId) {
        return commentDAO.findTopLevelComments(lessonId);
    }

    /**
     * Get replies to a comment
     */
    public List<Comment> getReplies(int commentId) {
        return commentDAO.findReplies(commentId);
    }

    /**
     * Post a comment on a lesson (requires enrollment or preview access)
     */
    public boolean postComment(Comment comment, int userId, String userRole) {
        // Get lesson details
        Lesson lesson = lessonDAO.findById(comment.getLessonId());
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        // Validate content
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        // Check access permissions
        // Users can comment if:
        // 1. Lesson is preview
        // 2. User is enrolled in the course
        // 3. User is admin
        boolean isAdmin = "ADMIN".equals(userRole);
        boolean isPreview = lesson.isPreview();

        if (!isAdmin && !isPreview) {
            // Check enrollment
            Enrollment enrollment = enrollmentDAO.findByUserAndCourse(userId, lesson.getCourseId());
            if (enrollment == null) {
                throw new SecurityException("You must be enrolled to comment on this lesson");
            }
        }

        // Verify ownership
        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("Cannot post comment for another user");
        }

        // If this is a reply, verify parent comment exists
        if (comment.getParentId() != null) {
            Comment parentComment = commentDAO.findById(comment.getParentId());
            if (parentComment == null) {
                throw new IllegalArgumentException("Parent comment not found");
            }
            // Verify parent comment is on the same lesson
            if (!parentComment.getLessonId().equals(comment.getLessonId())) {
                throw new IllegalArgumentException("Parent comment is on a different lesson");
            }
        }

        return commentDAO.insert(comment);
    }

    /**
     * Update a comment (only by owner)
     */
    public boolean updateComment(int commentId, String newContent, int userId, String userRole) {
        Comment comment = commentDAO.findById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found");
        }

        // Only owner or admin can update
        boolean isOwner = comment.getUserId().equals(userId);
        boolean isAdmin = "ADMIN".equals(userRole);

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You can only update your own comments");
        }

        // Validate new content
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        comment.setContent(newContent);
        return commentDAO.update(comment);
    }

    /**
     * Delete a comment (only by owner or admin)
     */
    public boolean deleteComment(int commentId, int userId, String userRole) {
        Comment comment = commentDAO.findById(commentId);
        if (comment == null) {
            return false;
        }

        // Only owner or admin can delete
        boolean isOwner = comment.getUserId().equals(userId);
        boolean isAdmin = "ADMIN".equals(userRole);

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You can only delete your own comments");
        }

        return commentDAO.delete(commentId);
    }

    /**
     * Get comment count for a lesson
     */
    public int getCommentCount(int lessonId) {
        return commentDAO.countByLessonId(lessonId);
    }

    /**
     * Get reply count for a comment
     */
    public int getReplyCount(int commentId) {
        return commentDAO.countReplies(commentId);
    }
}
