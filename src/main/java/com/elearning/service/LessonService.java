package com.elearning.service;

import com.elearning.dao.CourseDAO;
import com.elearning.dao.LessonDAO;
import com.elearning.model.Course;
import com.elearning.model.Lesson;

import java.util.List;

/**
 * Service for lesson management
 */
public class LessonService {
    private final LessonDAO lessonDAO;
    private final CourseDAO courseDAO;

    public LessonService() {
        this.lessonDAO = new LessonDAO();
        this.courseDAO = new CourseDAO();
    }

    /**
     * Get all lessons for a course (with access control)
     */
    public List<Lesson> getCourseLessons(int courseId, String userRole, Integer userId, boolean isEnrolled) {
        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        // Instructor can view their own course lessons regardless of enrollment
        boolean isInstructor = "INSTRUCTOR".equals(userRole) &&
                               userId != null &&
                               course.getInstructorId() == userId.intValue();

        // Admin can view all lessons
        boolean isAdmin = "ADMIN".equals(userRole);

        // Students can only view lessons if enrolled or if lessons are preview
        if (!isEnrolled && !isInstructor && !isAdmin) {
            return lessonDAO.findPreviewLessons(courseId);
        }

        return lessonDAO.findByCourseId(courseId);
    }

    /**
     * Get preview lessons for a course (public access)
     */
    public List<Lesson> getPreviewLessons(int courseId) {
        return lessonDAO.findPreviewLessons(courseId);
    }

    /**
     * Get lesson by ID (with access control)
     */
    public Lesson getLessonById(int lessonId, String userRole, Integer userId, boolean isEnrolled) {
        Lesson lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        Course course = courseDAO.findById(lesson.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        // Instructor can view their own course lessons
        boolean isInstructor = "INSTRUCTOR".equals(userRole) &&
                               userId != null &&
                               course.getInstructorId() == userId.intValue();

        // Admin can view all lessons
        boolean isAdmin = "ADMIN".equals(userRole);

        // Check access permission
        if (!isEnrolled && !isInstructor && !isAdmin && !lesson.isPreview()) {
            throw new SecurityException("You must be enrolled to access this lesson");
        }

        return lesson;
    }

    /**
     * Create new lesson (Instructor only, own courses)
     */
    public boolean createLesson(Lesson lesson, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can create lessons");
        }

        // Verify course ownership
        Course course = courseDAO.findById(lesson.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (course.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot create lessons for other instructor's courses");
        }

        // Validate lesson data
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson title is required");
        }

        // Set order index automatically if not provided
        if (lesson.getOrderIndex() == null) {
            lesson.setOrderIndex(lessonDAO.getNextOrderIndex(lesson.getCourseId()));
        }

        return lessonDAO.insert(lesson);
    }

    /**
     * Update lesson (Instructor only, own courses)
     */
    public boolean updateLesson(Lesson lesson, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can update lessons");
        }

        // Verify lesson exists
        Lesson existing = lessonDAO.findById(lesson.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        // Verify course ownership
        Course course = courseDAO.findById(existing.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (course.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot update lessons for other instructor's courses");
        }

        // Validate updated data
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson title is required");
        }

        // Preserve course_id (prevent changing lesson to different course)
        lesson.setCourseId(existing.getCourseId());

        return lessonDAO.update(lesson);
    }

    /**
     * Delete lesson (Instructor only, own courses, no progress)
     */
    public boolean deleteLesson(int lessonId, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can delete lessons");
        }

        // Verify lesson exists
        Lesson lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            return false;
        }

        // Verify course ownership
        Course course = courseDAO.findById(lesson.getCourseId());
        if (course == null) {
            return false;
        }

        if (course.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot delete lessons for other instructor's courses");
        }

        // Delete will fail if there are progress records (handled in DAO)
        return lessonDAO.delete(lessonId);
    }

    /**
     * Reorder lesson (Instructor only, own courses)
     */
    public boolean reorderLesson(int lessonId, int newOrderIndex, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can reorder lessons");
        }

        // Verify lesson exists
        Lesson lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        // Verify course ownership
        Course course = courseDAO.findById(lesson.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (course.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot reorder lessons for other instructor's courses");
        }

        if (newOrderIndex < 1) {
            throw new IllegalArgumentException("Order index must be positive");
        }

        return lessonDAO.updateOrderIndex(lessonId, newOrderIndex);
    }

    /**
     * Toggle lesson preview status (Instructor only, own courses)
     */
    public boolean togglePreview(int lessonId, boolean isPreview, int instructorId, String userRole) {
        if (!"INSTRUCTOR".equals(userRole)) {
            throw new SecurityException("Only instructors can change preview status");
        }

        // Verify lesson exists
        Lesson lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found");
        }

        // Verify course ownership
        Course course = courseDAO.findById(lesson.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        if (course.getInstructorId() != instructorId) {
            throw new SecurityException("Cannot modify lessons for other instructor's courses");
        }

        lesson.setPreview(isPreview);
        return lessonDAO.update(lesson);
    }

    /**
     * Get course statistics
     */
    public CourseStatistics getCourseStatistics(int courseId) {
        int lessonCount = lessonDAO.countLessons(courseId);
        int totalDuration = lessonDAO.getTotalDuration(courseId);
        return new CourseStatistics(lessonCount, totalDuration);
    }

    /**
     * Inner class for course statistics
     */
    public static class CourseStatistics {
        private final int lessonCount;
        private final int totalDurationMinutes;

        public CourseStatistics(int lessonCount, int totalDurationMinutes) {
            this.lessonCount = lessonCount;
            this.totalDurationMinutes = totalDurationMinutes;
        }

        public int getLessonCount() {
            return lessonCount;
        }

        public int getTotalDurationMinutes() {
            return totalDurationMinutes;
        }

        public double getTotalDurationHours() {
            return totalDurationMinutes / 60.0;
        }
    }
}
