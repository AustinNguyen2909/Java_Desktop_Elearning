package com.elearning.service;

import com.elearning.dao.CourseDAO;
import com.elearning.dao.EnrollmentDAO;
import com.elearning.dao.ReviewDAO;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.Review;

import java.util.List;

/**
 * Service for review management
 */
public class ReviewService {
    private final ReviewDAO reviewDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
    }

    /**
     * Get all reviews for a course
     */
    public List<Review> getCourseReviews(int courseId) {
        return reviewDAO.findByCourseId(courseId);
    }

    /**
     * Get user's review for a course
     */
    public Review getUserReview(int userId, int courseId) {
        return reviewDAO.findByUserAndCourse(userId, courseId);
    }

    /**
     * Post a review for a course (requires enrollment)
     */
    public boolean postReview(Review review, int userId, String userRole) {
        // Get course details
        Course course = courseDAO.findById(review.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        // Validate rating
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Validate comment
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty");
        }

        // Check if user is enrolled in the course
        Enrollment enrollment = enrollmentDAO.findByUserAndCourse(userId, review.getCourseId());
        if (enrollment == null) {
            throw new SecurityException("You must be enrolled in the course to review it");
        }

        // Verify user cannot review their own course (if they're the instructor)
        if (course.getInstructorId() == userId) {
            throw new SecurityException("You cannot review your own course");
        }

        // Verify ownership
        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("Cannot post review for another user");
        }

        // Check if user has already reviewed this course
        Review existingReview = reviewDAO.findByUserAndCourse(userId, review.getCourseId());
        if (existingReview != null) {
            throw new IllegalArgumentException("You have already reviewed this course. Use update instead.");
        }

        return reviewDAO.insert(review);
    }

    /**
     * Update a review (only by owner)
     */
    public boolean updateReview(int reviewId, int newRating, String newComment, int userId, String userRole) {
        Review review = reviewDAO.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }

        // Only owner or admin can update
        boolean isOwner = review.getUserId().equals(userId);
        boolean isAdmin = "ADMIN".equals(userRole);

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You can only update your own reviews");
        }

        // Validate rating
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Validate comment
        if (newComment == null || newComment.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty");
        }

        review.setRating(newRating);
        review.setComment(newComment);
        return reviewDAO.update(review);
    }

    /**
     * Delete a review (only by owner or admin)
     */
    public boolean deleteReview(int reviewId, int userId, String userRole) {
        Review review = reviewDAO.findById(reviewId);
        if (review == null) {
            return false;
        }

        // Only owner or admin can delete
        boolean isOwner = review.getUserId().equals(userId);
        boolean isAdmin = "ADMIN".equals(userRole);

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You can only delete your own reviews");
        }

        return reviewDAO.delete(reviewId);
    }

    /**
     * Get average rating for a course
     */
    public double getAverageRating(int courseId) {
        return reviewDAO.getAverageRating(courseId);
    }

    /**
     * Get review count for a course
     */
    public int getReviewCount(int courseId) {
        return reviewDAO.countByCourseId(courseId);
    }

    /**
     * Get rating distribution for a course
     */
    public int[] getRatingDistribution(int courseId) {
        return reviewDAO.getRatingDistribution(courseId);
    }

    /**
     * Get review statistics for a course
     */
    public ReviewStatistics getReviewStatistics(int courseId) {
        double avgRating = reviewDAO.getAverageRating(courseId);
        int reviewCount = reviewDAO.countByCourseId(courseId);
        int[] distribution = reviewDAO.getRatingDistribution(courseId);

        return new ReviewStatistics(avgRating, reviewCount, distribution);
    }

    /**
     * Inner class for review statistics
     */
    public static class ReviewStatistics {
        private final double averageRating;
        private final int totalReviews;
        private final int[] ratingDistribution;

        public ReviewStatistics(double averageRating, int totalReviews, int[] ratingDistribution) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.ratingDistribution = ratingDistribution;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public int[] getRatingDistribution() {
            return ratingDistribution;
        }

        public int getFiveStars() {
            return ratingDistribution[4];
        }

        public int getFourStars() {
            return ratingDistribution[3];
        }

        public int getThreeStars() {
            return ratingDistribution[2];
        }

        public int getTwoStars() {
            return ratingDistribution[1];
        }

        public int getOneStar() {
            return ratingDistribution[0];
        }
    }
}
