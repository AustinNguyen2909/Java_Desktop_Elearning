package com.elearning.service;

import com.elearning.dao.CourseDAO;
import com.elearning.model.Course;

import java.util.List;

/**
 * Service for course management and approval workflow
 * Singleton pattern for single instance across application
 */
public class CourseService {
    private final CourseDAO courseDAO;

    // Private constructor to prevent direct instantiation
    private CourseService() {
        this.courseDAO = new CourseDAO();
    }

    // Static inner holder class - lazily loaded and thread-safe
    private static class SingletonHolder {
        private static final CourseService INSTANCE = new CourseService();
    }

    // Public accessor method
    public static CourseService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Get courses visible to students (approved and published)
     */
    public List<Course> getAvailableCourses() {
        return courseDAO.findApprovedCourses();
    }

    /**
     * Get all courses (Admin only)
     */
    public List<Course> getAllCourses(String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can view all courses");
        }
        return courseDAO.findAllCourses();
    }

    /**
     * Get courses by instructor
     */
    public List<Course> getInstructorCourses(int instructorId, String userRole, int requestingUserId) {
        // Allow instructors to view their own courses, admins to view any
        if ("INSTRUCTOR".equals(userRole) && instructorId != requestingUserId && !"ADMIN".equals(userRole)) {
            throw new SecurityException("Cannot view other instructor's courses");
        }
        return courseDAO.findByInstructorId(instructorId);
    }

    /**
     * Get pending courses for approval (Admin only)
     */
    public List<Course> getPendingCourses(String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can view pending courses");
        }
        return courseDAO.findPendingCourses();
    }

    /**
     * Get course by ID
     */
    public Course getCourseById(int courseId) {
        return courseDAO.findById(courseId);
    }

    /**
     * Create new course (Instructor only)
     */
    public boolean createCourse(Course course, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can create courses");
        }

        // Validate course data
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Course title is required");
        }

        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Course description is required");
        }

        if (course.getCategory() == null || course.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Course category is required");
        }

        if (course.getDifficultyLevel() == null) {
            throw new IllegalArgumentException("Difficulty level is required");
        }

        if (course.getEstimatedHours() == null || course.getEstimatedHours() <= 0) {
            throw new IllegalArgumentException("Estimated hours must be positive");
        }

        // Course starts as PENDING status
        course.setStatus("PENDING");
        course.setPublished(false);

        return courseDAO.insert(course);
    }

    /**
     * Update course (Instructor only, own courses)
     */
    public boolean updateCourse(Course course, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can update courses");
        }

        // Verify ownership
        Course existing = courseDAO.findById(course.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (existing.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot update other instructor's courses");
        }

        // Cannot edit if approved and published (to maintain integrity)
        // Instructor can only edit pending or rejected courses
        if ("APPROVED".equals(existing.getStatus()) && existing.isPublished()) {
            throw new IllegalStateException("Cannot edit published approved courses");
        }

        // Validate updated data
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Course title is required");
        }

        return courseDAO.update(course);
    }

    /**
     * Delete course (Instructor only, no enrollments)
     */
    public boolean deleteCourse(int courseId, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can delete courses");
        }

        // Verify ownership
        Course existing = courseDAO.findById(courseId);
        if (existing == null) {
            return false;
        }

        if (existing.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot delete other instructor's courses");
        }

        // Delete will fail if there are enrollments (handled in DAO)
        return courseDAO.delete(courseId, instructorId);
    }

    /**
     * Approve course (Admin only)
     */
    public boolean approveCourse(int courseId, int adminId, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can approve courses");
        }

        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (!"PENDING".equals(course.getStatus())) {
            throw new IllegalStateException("Can only approve pending courses");
        }

        return courseDAO.approveCourse(courseId, adminId);
    }

    /**
     * Reject course (Admin only)
     */
    public boolean rejectCourse(int courseId, String reason, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can reject courses");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (!"PENDING".equals(course.getStatus())) {
            throw new IllegalStateException("Can only reject pending courses");
        }

        return courseDAO.rejectCourse(courseId, reason);
    }

    /**
     * Publish/unpublish course (Instructor only, approved courses)
     */
    public boolean togglePublishCourse(int courseId, int instructorId, boolean publish, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can publish courses");
        }

        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (course.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot publish other instructor's courses");
        }

        if (!"APPROVED".equals(course.getStatus())) {
            throw new IllegalStateException("Can only publish approved courses");
        }

        return courseDAO.togglePublish(courseId, instructorId, publish);
    }

    /**
     * Search courses by title or category
     */
    public List<Course> searchCourses(String keyword) {
        // Get all approved courses and filter by keyword
        List<Course> allCourses = courseDAO.findApprovedCourses();
        if (keyword == null || keyword.trim().isEmpty()) {
            return allCourses;
        }

        String lowerKeyword = keyword.toLowerCase();
        return allCourses.stream()
                .filter(c -> c.getTitle().toLowerCase().contains(lowerKeyword) ||
                            (c.getCategory() != null && c.getCategory().toLowerCase().contains(lowerKeyword)) ||
                            (c.getDescription() != null && c.getDescription().toLowerCase().contains(lowerKeyword)))
                .toList();
    }
}
