package com.elearning.service;

import com.elearning.dao.*;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analytics and statistics generation
 * Singleton pattern for single instance across application
 */
public class AnalyticsService {
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final LessonDAO lessonDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final ReviewDAO reviewDAO;

    // Private constructor to prevent direct instantiation
    private AnalyticsService() {
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.lessonDAO = new LessonDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.reviewDAO = new ReviewDAO();
    }

    // Static inner holder class - lazily loaded and thread-safe
    private static class SingletonHolder {
        private static final AnalyticsService INSTANCE = new AnalyticsService();
    }

    // Public accessor method
    public static AnalyticsService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Get platform-wide statistics (Admin)
     */
    public PlatformStatistics getPlatformStatistics(String adminRole) {
        if (!"ADMIN".equals(adminRole)) {
            throw new SecurityException("Only admins can view platform statistics");
        }

        // User counts
        int totalUsers = countUsersByRole("USER");
        int totalInstructors = countUsersByRole("INSTRUCTOR");
        int totalAdmins = countUsersByRole("ADMIN");
        int activeUsers = countActiveUsers();

        // Course counts
        int totalCourses = courseDAO.findAll().size();
        int approvedCourses = courseDAO.findApprovedCourses().size();
        int pendingCourses = courseDAO.findPendingCourses().size();
        int publishedCourses = courseDAO.findPublishedCourses().size();

        // Enrollment stats
        int totalEnrollments = enrollmentDAO.getTotalEnrollmentCount();
        int activeEnrollments = enrollmentDAO.getInProgressCount();
        int completedEnrollments = enrollmentDAO.getCompletedCount();

        // Review stats
        int totalReviews = countAllReviews();

        // Calculate averages
        double averageProgress = 0.0;
        if (totalEnrollments > 0) {
            averageProgress = enrollmentDAO.getGlobalAverageProgress();
        }

        double averageEnrollmentsPerCourse = 0.0;
        if (totalCourses > 0) {
            averageEnrollmentsPerCourse = (double) totalEnrollments / totalCourses;
        }

        return new PlatformStatistics(totalUsers, totalInstructors, totalAdmins, activeUsers,
                totalCourses, approvedCourses, pendingCourses, publishedCourses,
                totalEnrollments, activeEnrollments, completedEnrollments,
                totalReviews, averageProgress, averageEnrollmentsPerCourse);
    }

    /**
     * Get instructor-specific statistics
     */
    public InstructorStatistics getInstructorStatistics(int instructorId, String userRole) {
        // Only the instructor themselves or admin can view
        if (!"INSTRUCTOR".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new SecurityException("Unauthorized access to instructor statistics");
        }

        // Get instructor's courses
        List<Course> courses = courseDAO.findByInstructorId(instructorId);
        int totalCourses = courses.size();

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

        // Calculate averages
        double averageRating = 0.0;
        if (approvedCount > 0) {
            averageRating = totalRating / approvedCount;
        }

        double averageEnrollmentsPerCourse = 0.0;
        if (totalCourses > 0) {
            averageEnrollmentsPerCourse = (double) totalEnrollments / totalCourses;
        }

        // Calculate completion rate
        double completionRate = 0.0;
        if (totalEnrollments > 0) {
            int completedEnrollments = 0;
            for (Course course : courses) {
                completedEnrollments += enrollmentDAO.getCompletionCount(course.getId());
            }
            completionRate = (double) completedEnrollments / totalEnrollments * 100;
        }

        return new InstructorStatistics(totalCourses, approvedCount, publishedCount,
                totalEnrollments, totalReviews, averageRating,
                averageEnrollmentsPerCourse, completionRate);
    }

    /**
     * Get course-specific statistics
     */
    public CourseStatistics getCourseStatistics(int courseId) {
        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        String courseTitle = course.getTitle();

        // Enrollment stats
        int totalEnrollments = enrollmentDAO.getEnrollmentCount(courseId);
        int completedEnrollments = enrollmentDAO.getCompletionCount(courseId);
        int activeEnrollments = totalEnrollments - completedEnrollments;
        double averageProgress = enrollmentDAO.getAverageProgress(courseId);

        // Lesson stats
        int totalLessons = lessonDAO.countLessons(courseId);
        int totalDuration = lessonDAO.getTotalDuration(courseId);

        // Review stats
        int totalReviews = reviewDAO.countByCourseId(courseId);
        double averageRating = reviewDAO.getAverageRating(courseId);
        int[] ratingDistribution = reviewDAO.getRatingDistribution(courseId);

        return new CourseStatistics(courseId, courseTitle, totalEnrollments, activeEnrollments,
                completedEnrollments, averageProgress, totalLessons, totalDuration,
                totalReviews, averageRating, ratingDistribution);
    }

    /**
     * Get top courses by enrollment
     */
    public List<Course> getTopCoursesByEnrollment(int limit) {
        return courseDAO.findTopCoursesByEnrollment(limit);
    }

    /**
     * Get user registration trends for the last N days
     * Returns a map with date as key and UserRegistrationData as value
     */
    public Map<String, UserRegistrationData> getUserRegistrationTrends(int days) {
        Map<String, UserRegistrationData> trends = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> rawData = userDAO.getUserRegistrationsByDate(days);

        for (Map.Entry<String, Map<String, Integer>> entry : rawData.entrySet()) {
            String date = entry.getKey();
            Map<String, Integer> roleCounts = entry.getValue();

            int students = roleCounts.getOrDefault("USER", 0);
            int instructors = roleCounts.getOrDefault("INSTRUCTOR", 0);

            trends.put(date, new UserRegistrationData(date, students, instructors));
        }

        return trends;
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
        private final int totalUsers;
        private final int totalInstructors;
        private final int totalAdmins;
        private final int activeUsers;
        private final int totalCourses;
        private final int approvedCourses;
        private final int pendingCourses;
        private final int publishedCourses;
        private final int totalEnrollments;
        private final int activeEnrollments;
        private final int completedEnrollments;
        private final int totalReviews;
        private final double averageProgress;
        private final double averageEnrollmentsPerCourse;

        public PlatformStatistics(int totalUsers, int totalInstructors, int totalAdmins, int activeUsers,
                                  int totalCourses, int approvedCourses, int pendingCourses, int publishedCourses,
                                  int totalEnrollments, int activeEnrollments, int completedEnrollments,
                                  int totalReviews, double averageProgress, double averageEnrollmentsPerCourse) {
            this.totalUsers = totalUsers;
            this.totalInstructors = totalInstructors;
            this.totalAdmins = totalAdmins;
            this.activeUsers = activeUsers;
            this.totalCourses = totalCourses;
            this.approvedCourses = approvedCourses;
            this.pendingCourses = pendingCourses;
            this.publishedCourses = publishedCourses;
            this.totalEnrollments = totalEnrollments;
            this.activeEnrollments = activeEnrollments;
            this.completedEnrollments = completedEnrollments;
            this.totalReviews = totalReviews;
            this.averageProgress = averageProgress;
            this.averageEnrollmentsPerCourse = averageEnrollmentsPerCourse;
        }

        public int getTotalUsers() { return totalUsers; }
        public int getTotalInstructors() { return totalInstructors; }
        public int getTotalAdmins() { return totalAdmins; }
        public int getActiveUsers() { return activeUsers; }
        public int getTotalCourses() { return totalCourses; }
        public int getApprovedCourses() { return approvedCourses; }
        public int getPendingCourses() { return pendingCourses; }
        public int getPublishedCourses() { return publishedCourses; }
        public int getTotalEnrollments() { return totalEnrollments; }
        public int getActiveEnrollments() { return activeEnrollments; }
        public int getCompletedEnrollments() { return completedEnrollments; }
        public int getTotalReviews() { return totalReviews; }
        public double getAverageProgress() { return averageProgress; }
        public double getAverageEnrollmentsPerCourse() { return averageEnrollmentsPerCourse; }

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
        private final int totalCourses;
        private final int approvedCourses;
        private final int publishedCourses;
        private final int totalStudents;
        private final int totalReviews;
        private final double averageRating;
        private final double averageEnrollmentsPerCourse;
        private final double completionRate;

        public InstructorStatistics(int totalCourses, int approvedCourses, int publishedCourses,
                                    int totalStudents, int totalReviews, double averageRating,
                                    double averageEnrollmentsPerCourse, double completionRate) {
            this.totalCourses = totalCourses;
            this.approvedCourses = approvedCourses;
            this.publishedCourses = publishedCourses;
            this.totalStudents = totalStudents;
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.averageEnrollmentsPerCourse = averageEnrollmentsPerCourse;
            this.completionRate = completionRate;
        }

        public int getTotalCourses() { return totalCourses; }
        public int getApprovedCourses() { return approvedCourses; }
        public int getPublishedCourses() { return publishedCourses; }
        public int getTotalStudents() { return totalStudents; }
        public int getTotalReviews() { return totalReviews; }
        public double getAverageRating() { return averageRating; }
        public double getAverageEnrollmentsPerCourse() { return averageEnrollmentsPerCourse; }
        public double getCompletionRate() { return completionRate; }

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
        private final int courseId;
        private final String courseTitle;
        private final int totalEnrollments;
        private final int activeEnrollments;
        private final int completedEnrollments;
        private final double averageProgress;
        private final int totalLessons;
        private final int totalDuration;
        private final int totalReviews;
        private final double averageRating;
        private final int[] ratingDistribution;

        public CourseStatistics(int courseId, String courseTitle, int totalEnrollments,
                                int activeEnrollments, int completedEnrollments, double averageProgress,
                                int totalLessons, int totalDuration, int totalReviews,
                                double averageRating, int[] ratingDistribution) {
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.totalEnrollments = totalEnrollments;
            this.activeEnrollments = activeEnrollments;
            this.completedEnrollments = completedEnrollments;
            this.averageProgress = averageProgress;
            this.totalLessons = totalLessons;
            this.totalDuration = totalDuration;
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.ratingDistribution = ratingDistribution;
        }

        public int getCourseId() { return courseId; }
        public String getCourseTitle() { return courseTitle; }
        public int getTotalEnrollments() { return totalEnrollments; }
        public int getActiveEnrollments() { return activeEnrollments; }
        public int getCompletedEnrollments() { return completedEnrollments; }
        public double getAverageProgress() { return averageProgress; }
        public int getTotalLessons() { return totalLessons; }
        public int getTotalDuration() { return totalDuration; }
        public int getTotalReviews() { return totalReviews; }
        public double getAverageRating() { return averageRating; }
        public int[] getRatingDistribution() { return ratingDistribution; }

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

    /**
     * User registration data for a specific date
     */
    public static class UserRegistrationData {
        private final String date;
        private final int students;
        private final int instructors;

        public UserRegistrationData(String date, int students, int instructors) {
            this.date = date;
            this.students = students;
            this.instructors = instructors;
        }

        public String getDate() { return date; }
        public int getStudents() { return students; }
        public int getInstructors() { return instructors; }
    }
}
