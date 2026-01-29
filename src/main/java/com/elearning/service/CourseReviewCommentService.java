package com.elearning.service;

import com.elearning.dao.CourseReviewCommentDAO;
import com.elearning.dao.ReviewDAO;
import com.elearning.model.CourseReviewComment;
import com.elearning.model.Review;

import java.util.List;

/**
 * Service for course review comments.
 */
public class CourseReviewCommentService {
    private final CourseReviewCommentDAO commentDAO;
    private final ReviewDAO reviewDAO;

    private CourseReviewCommentService() {
        this.commentDAO = new CourseReviewCommentDAO();
        this.reviewDAO = new ReviewDAO();
    }

    private static class SingletonHolder {
        private static final CourseReviewCommentService INSTANCE = new CourseReviewCommentService();
    }

    public static CourseReviewCommentService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public List<CourseReviewComment> getTopLevelComments(int reviewId) {
        return commentDAO.findTopLevelComments(reviewId);
    }

    public List<CourseReviewComment> getReplies(int parentId) {
        return commentDAO.findReplies(parentId);
    }

    public boolean postComment(CourseReviewComment comment, int userId) {
        Review review = reviewDAO.findById(comment.getReviewId());
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("Cannot post comment for another user");
        }
        if (comment.getParentId() != null) {
            CourseReviewComment parent = commentDAO.findById(comment.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("Parent comment not found");
            }
            if (!parent.getReviewId().equals(comment.getReviewId())) {
                throw new IllegalArgumentException("Parent comment belongs to a different review");
            }
        }
        return commentDAO.insert(comment);
    }
}
