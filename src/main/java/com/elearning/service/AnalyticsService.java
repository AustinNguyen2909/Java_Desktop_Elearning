package com.elearning.service;

import com.elearning.dao.*;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analytics and statistics generation
 */
public class AnalyticsService {
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final LessonDAO lessonDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final ReviewDAO reviewDAO;

    public AnalyticsService() {
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.lessonDAO = new LessonDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.reviewDAO = new ReviewDAO();
    }

    /**
     * Get platform-wide statistics (Admin)
     */
    public PlatformStatistics getPlatformStatistics(String adminRole) {
        if (!"ADMIN".equals(adminRole)) {
            throw new SecurityException("Only admins can view platform statistics");
        }

        PlatformStatistics stats = new PlatformStatistics();

        // User counts
        stats.totalUsers = countUsersByRole("USER");
        stats.totalInstructors = countUsersByRole("INSTRUCTOR");
        stats.totalAdmins = countUsersByRole("ADMIN");
        stats.activeUsers = countActiveUsers();

        // Course counts
        stats.totalCourses = courseDAO.findAll().size();
        stats.approvedCourses = courseDAO.findApprovedCourses().size();
        stats.pendingCourses = courseDAO.findPendingCourses().size();
        stats.publishedCourses = courseDAO.findPublishedCourses().size();

        // Enrollment stats
        stats.totalEnrollments = enrollmentDAO.getTotalEnrollmentCount();
        stats.activeEnrollments = enrollmentDAO.getInProgressCount();
        stats.completedEnrollments = enrollmentDAO.getCompletedCount();

        // Review stats
        stats.totalReviews = countAllReviews();

        // Calculate averages
        if (stats.totalEnrollments > 0) {
            stats.averageProgress = enrollmentDAO.getGlobalAverageProgress();
        }

        if (stats.totalCourses > 0) {
            stats.averageEnrollmentsPerCourse = (double) stats.totalEnrollments / stats.totalCourses;
        }

        return stats;
    }

    /**
     * Get instructor-specific statistics
     */
    public InstructorStatistics getInstructorStatistics(int instructorId, String userRole) {
        // Only the instructor themselves or admin can view
        if (!"INSTRUCTOR".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new SecurityException("Unauthorized access to instructor statistics");
        }

        InstructorStatistics stats = new InstructorStatistics();

        // Get instructor's courses
        List<Course> courses = courseDAO.findByInstructorId(instructorId);
        stats.totalCourses = courses.size();

        int approvedCount = 0;
        int publishedCount = 0;
        int totalEnrollments = 0;
        int totalReviews = 0;
        double totalRating = 0.0;

        for (Course course : courses) {
            if ("APPROVED".equals(course.getStatus())) {
                approvedCount++;
            }
            if (course.isPublished()) {
                publishedCount++;
            }

            // Enrollment stats
            int enrollmentCount = enrollmentDAO.getEnrollmentCount(course.getId());
            totalEnrollments += enrollmentCount;

            // Review stats
            int reviewCount = reviewDAO.countByCourseId(course.getId());
            totalReviews += reviewCount;

            double avgRating = reviewDAO.getAverageRating(course.getId());
            if (avgRating > 0) {
                totalRating += avgRating;
            }
        }

        stats.approvedCourses = approvedCount;
        stats.publishedCourses = publishedCount;
        stats.totalStudents = totalEnrollments;
        stats.totalReviews = totalReviews;

        if (approvedCount > 0) {
            stats.averageRating = totalRating / approvedCount;
        }

        if (stats.totalCourses > 0) {
            stats.averageEnrollmentsPerCourse = (double) totalEnrollments / stats.totalCourses;
        }

        // Calculate completion rate
        if (totalEnrollments > 0) {
            int completedEnrollments = 0;
            for (Course course : courses) {
                completedEnrollments += enrollmentDAO.getCompletionCount(course.getId());
            }
            stats.completionRate = (double) completedEnrollments / totalEnrollments * 100;
        }

        return stats;
    }

    /**
     * Get course-specific statistics
     */
    public CourseStatistics getCourseStatistics(int courseId) {
        CourseStatistics stats = new CourseStatistics();

        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        stats.courseId = courseId;
        stats.courseTitle = course.getTitle();

        // Enrollment stats
        stats.totalEnrollments = enrollmentDAO.getEnrollmentCount(courseId);
        stats.activeEnrollments = enrollmentDAO.getEnrollmentCount(courseId) -
                                  enrollmentDAO.getCompletionCount(courseId);
        stats.completedEnrollments = enrollmentDAO.getCompletionCount(courseId);
        stats.averageProgress = enrollmentDAO.getAverageProgress(courseId);

        // Lesson stats
        stats.totalLessons = lessonDAO.countLessons(courseId);
        stats.totalDuration = lessonDAO.getTotalDuration(courseId);

        // Review stats
        stats.totalReviews = reviewDAO.countByCourseId(courseId);
        stats.averageRating = reviewDAO.getAverageRating(courseId);
        stats.ratingDistribution = reviewDAO.getRatingDistribution(courseId);

        return stats;
    }

    /**
     * Get top courses by enrollment
     */
    public List<Course> getTopCoursesByEnrollment(int limit) {
        return courseDAO.findTopCoursesByEnrollment(limit);
    }

    /**
     * Helper method to count users by role
     */
    private int countUsersByRole(String role) {
        return userDAO.findByRole(role).size();
    }

    /**
     * Helper method to count active users
     */
    private int countActiveUsers() {
        List<com.elearning.model.User> allUsers = userDAO.findAll();
        return (int) allUsers.stream()
            .filter(u -> "ACTIVE".equals(u.getStatus()))
            .count();
    }

    /**
     * Helper method to count all reviews
     */
    private int countAllReviews() {
        List<Course> allCourses = courseDAO.findAll();
        int total = 0;
        for (Course course : allCourses) {
            total += reviewDAO.countByCourseId(course.getId());
        }
        return total;
    }

    /**
     * Platform-wide statistics
     */
    public static class PlatformStatistics {
        public int totalUsers;
        public int totalInstructors;
        public int totalAdmins;
        public int activeUsers;
        public int totalCourses;
        public int approvedCourses;
        public int pendingCourses;
        public int publishedCourses;
        public int totalEnrollments;
        public int activeEnrollments;
        public int completedEnrollments;
        public int totalReviews;
        public double averageProgress;
        public double averageEnrollmentsPerCourse;

        @Override
        public String toString() {
            return String.format(
                "Platform Statistics:\n" +
                "Users: %d total (%d students, %d instructors, %d admins, %d active)\n" +
                "Courses: %d total (%d approved, %d pending, %d published)\n" +
                "Enrollments: %d total (%d active, %d completed)\n" +
                "Reviews: %d total\n" +
                "Average Progress: %.1f%%\n" +
                "Average Enrollments per Course: %.1f",
                totalUsers + totalInstructors + totalAdmins, totalUsers, totalInstructors, totalAdmins, activeUsers,
                totalCourses, approvedCourses, pendingCourses, publishedCourses,
                totalEnrollments, activeEnrollments, completedEnrollments,
                totalReviews, averageProgress, averageEnrollmentsPerCourse
            );
        }
    }

    /**
     * Instructor-specific statistics
     */
    public static class InstructorStatistics {
        public int totalCourses;
        public int approvedCourses;
        public int publishedCourses;
        public int totalStudents;
        public int totalReviews;
        public double averageRating;
        public double averageEnrollmentsPerCourse;
        public double completionRate;

        @Override
        public String toString() {
            return String.format(
                "Instructor Statistics:\n" +
                "Courses: %d total (%d approved, %d published)\n" +
                "Students: %d total\n" +
                "Reviews: %d (Average Rating: %.1f/5.0)\n" +
                "Average Enrollments per Course: %.1f\n" +
                "Completion Rate: %.1f%%",
                totalCourses, approvedCourses, publishedCourses,
                totalStudents, totalReviews, averageRating,
                averageEnrollmentsPerCourse, completionRate
            );
        }
    }

    /**
     * Course-specific statistics
     */
    public static class CourseStatistics {
        public int courseId;
        public String courseTitle;
        public int totalEnrollments;
        public int activeEnrollments;
        public int completedEnrollments;
        public double averageProgress;
        public int totalLessons;
        public int totalDuration;
        public int totalReviews;
        public double averageRating;
        public int[] ratingDistribution;

        @Override
        public String toString() {
            return String.format(
                "Course Statistics: %s\n" +
                "Enrollments: %d total (%d active, %d completed)\n" +
                "Average Progress: %.1f%%\n" +
                "Lessons: %d (%d minutes total)\n" +
                "Reviews: %d (Average Rating: %.1f/5.0)",
                courseTitle, totalEnrollments, activeEnrollments, completedEnrollments,
                averageProgress, totalLessons, totalDuration,
                totalReviews, averageRating
            );
        }
    }
}
