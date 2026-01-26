package com.elearning.service;

import com.elearning.dao.CourseDAO;
import com.elearning.dao.EnrollmentDAO;
import com.elearning.dao.LessonDAO;
import com.elearning.dao.LessonProgressDAO;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.LessonProgress;

import java.util.List;

/**
 * Service for enrollment and progress tracking
 */
public class EnrollmentService {
    private final EnrollmentDAO enrollmentDAO;
    private final LessonProgressDAO lessonProgressDAO;
    private final CourseDAO courseDAO;
    private final LessonDAO lessonDAO;

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.lessonProgressDAO = new LessonProgressDAO();
        this.courseDAO = new CourseDAO();
        this.lessonDAO = new LessonDAO();
    }

    /**
     * Enroll user in a course
     */
    public boolean enrollInCourse(int userId, int courseId, String userRole) {
        if (!"USER".equals(userRole)) {
            throw new SecurityException("Only students can enroll in courses");
        }

        // Check if course exists and is available
        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (!"APPROVED".equals(course.getStatus()) || !course.isPublished()) {
            throw new IllegalStateException("Course is not available for enrollment");
        }

        // Check if already enrolled
        if (enrollmentDAO.isEnrolled(userId, courseId)) {
            throw new IllegalStateException("Already enrolled in this course");
        }

        return enrollmentDAO.enroll(userId, courseId);
    }

    /**
     * Unenroll from a course (only if progress < 10%)
     */
    public boolean unenrollFromCourse(int userId, int courseId, String userRole) {
        if (!"USER".equals(userRole)) {
            throw new SecurityException("Only students can unenroll from courses");
        }

        Enrollment enrollment = enrollmentDAO.findByUserAndCourse(userId, courseId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Not enrolled in this course");
        }

        if (enrollment.getProgressPercent() >= 10.0) {
            throw new IllegalStateException("Cannot unenroll after completing 10% of the course");
        }

        // Delete lesson progress
        lessonProgressDAO.deleteByUserAndCourse(userId, courseId);

        // Delete enrollment
        return enrollmentDAO.unenroll(userId, courseId);
    }

    /**
     * Get user's enrollments
     */
    public List<Enrollment> getUserEnrollments(int userId) {
        return enrollmentDAO.findByUserId(userId);
    }

    /**
     * Get in-progress courses
     */
    public List<Enrollment> getInProgressCourses(int userId) {
        return enrollmentDAO.findInProgressByUserId(userId);
    }

    /**
     * Get completed courses
     */
    public List<Enrollment> getCompletedCourses(int userId) {
        return enrollmentDAO.findCompletedByUserId(userId);
    }

    /**
     * Get course enrollments (Instructor/Admin view)
     */
    public List<Enrollment> getCourseEnrollments(int courseId, String userRole, Integer instructorId) {
        // Verify access rights
        if ("INSTRUCTOR".equals(userRole)) {
            Course course = courseDAO.findById(courseId);
            if (course == null) {
                throw new IllegalArgumentException("Course not found");
            }

            if (instructorId == null || course.getInstructorId() != instructorId.intValue()) {
                throw new SecurityException("Cannot view enrollments for other instructor's courses");
            }
        } else if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only instructors and admins can view course enrollments");
        }

        return enrollmentDAO.findByCourseId(courseId);
    }

    /**
     * Check if user is enrolled
     */
    public boolean isEnrolled(int userId, int courseId) {
        return enrollmentDAO.isEnrolled(userId, courseId);
    }

    /**
     * Get enrollment details
     */
    public Enrollment getEnrollment(int userId, int courseId) {
        return enrollmentDAO.findByUserAndCourse(userId, courseId);
    }

    /**
     * Mark lesson as opened and update enrollment
     */
    public boolean openLesson(int userId, int lessonId) {
        // Mark lesson as opened
        boolean success = lessonProgressDAO.markAsOpened(userId, lessonId);

        if (success) {
            // Get course ID from lesson
            var lesson = lessonDAO.findById(lessonId);
            if (lesson != null) {
                // Update last accessed time for enrollment
                enrollmentDAO.updateLastAccessed(userId, lesson.getCourseId());
            }
        }

        return success;
    }

    /**
     * Mark lesson as completed and update course progress
     */
    public boolean completeLesson(int userId, int lessonId) {
        // Get lesson to find course ID
        var lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        int courseId = lesson.getCourseId();

        // Check if enrolled
        if (!enrollmentDAO.isEnrolled(userId, courseId)) {
            throw new SecurityException("Must be enrolled to complete lessons");
        }

        // Mark lesson as completed
        boolean success = lessonProgressDAO.markAsCompleted(userId, lessonId);

        if (success) {
            // Recalculate course progress
            double progressPercent = lessonProgressDAO.calculateProgressPercent(userId, courseId);

            // Update enrollment progress
            enrollmentDAO.updateProgress(userId, courseId, progressPercent);
            enrollmentDAO.updateLastAccessed(userId, courseId);
        }

        return success;
    }

    /**
     * Mark lesson as incomplete
     */
    public boolean resetLessonProgress(int userId, int lessonId) {
        // Get lesson to find course ID
        var lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        int courseId = lesson.getCourseId();

        // Mark as incomplete
        boolean success = lessonProgressDAO.markAsIncomplete(userId, lessonId);

        if (success) {
            // Recalculate course progress
            double progressPercent = lessonProgressDAO.calculateProgressPercent(userId, courseId);

            // Update enrollment progress
            enrollmentDAO.updateProgress(userId, courseId, progressPercent);
        }

        return success;
    }

    /**
     * Get lesson progress for a course
     */
    public List<LessonProgress> getCourseProgress(int userId, int courseId) {
        // Check if enrolled
        if (!enrollmentDAO.isEnrolled(userId, courseId)) {
            throw new SecurityException("Must be enrolled to view progress");
        }

        return lessonProgressDAO.findByUserAndCourse(userId, courseId);
    }

    /**
     * Get next lesson to study
     */
    public Integer getNextLesson(int userId, int courseId) {
        // Check if enrolled
        if (!enrollmentDAO.isEnrolled(userId, courseId)) {
            throw new SecurityException("Must be enrolled to access lessons");
        }

        return lessonProgressDAO.getNextIncompleteLesson(userId, courseId);
    }

    /**
     * Get enrollment statistics for a course
     */
    public EnrollmentStats getCourseStats(int courseId, String userRole, Integer instructorId) {
        // Verify access rights
        if ("INSTRUCTOR".equals(userRole)) {
            Course course = courseDAO.findById(courseId);
            if (course == null) {
                throw new IllegalArgumentException("Course not found");
            }

            if (instructorId == null || course.getInstructorId() != instructorId.intValue()) {
                throw new SecurityException("Cannot view stats for other instructor's courses");
            }
        } else if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only instructors and admins can view course statistics");
        }

        int enrollmentCount = enrollmentDAO.getEnrollmentCount(courseId);
        int completionCount = enrollmentDAO.getCompletionCount(courseId);
        double averageProgress = enrollmentDAO.getAverageProgress(courseId);

        return new EnrollmentStats(enrollmentCount, completionCount, averageProgress);
    }

    /**
     * Inner class for enrollment statistics
     */
    public static class EnrollmentStats {
        private final int enrollmentCount;
        private final int completionCount;
        private final double averageProgress;

        public EnrollmentStats(int enrollmentCount, int completionCount, double averageProgress) {
            this.enrollmentCount = enrollmentCount;
            this.completionCount = completionCount;
            this.averageProgress = averageProgress;
        }

        public int getEnrollmentCount() {
            return enrollmentCount;
        }

        public int getCompletionCount() {
            return completionCount;
        }

        public double getAverageProgress() {
            return averageProgress;
        }

        public double getCompletionRate() {
            if (enrollmentCount == 0) {
                return 0.0;
            }
            return (completionCount * 100.0) / enrollmentCount;
        }
    }
}
