package com.elearning.dao;

import com.elearning.model.Course;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Course entity
 */
public class CourseDAO {

    /**
     * Find all approved and published courses
     */
    public List<Course> findApprovedCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as instructor_name, " +
                     "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id) as enrollment_count, " +
                     "(SELECT AVG(rating) FROM reviews r WHERE r.course_id = c.id) as avg_rating " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "WHERE c.status = 'APPROVED' AND c.is_published = TRUE " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                course.setInstructorName(rs.getString("instructor_name"));
                course.setEnrollmentCount(rs.getInt("enrollment_count"));
                course.setAverageRating(rs.getDouble("avg_rating"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Find all courses (Admin only)
     */
    public List<Course> findAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                course.setInstructorName(rs.getString("instructor_name"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Find courses by instructor
     */
    public List<Course> findByInstructorId(int instructorId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id) as enrollment_count, " +
                     "(SELECT AVG(rating) FROM reviews r WHERE r.course_id = c.id) as avg_rating " +
                     "FROM courses c " +
                     "WHERE c.instructor_id = ? " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                course.setEnrollmentCount(rs.getInt("enrollment_count"));
                course.setAverageRating(rs.getDouble("avg_rating"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Find pending courses (Admin only)
     */
    public List<Course> findPendingCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "WHERE c.status = 'PENDING' " +
                     "ORDER BY c.created_at ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                course.setInstructorName(rs.getString("instructor_name"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Find course by ID
     */
    public Course findById(int id) {
        String sql = "SELECT c.*, u.full_name as instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "WHERE c.id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                course.setInstructorName(rs.getString("instructor_name"));
                return course;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert new course
     */
    public boolean insert(Course course) {
        String sql = "INSERT INTO courses (instructor_id, title, description, thumbnail_path, " +
                     "category, difficulty_level, estimated_hours, status, is_published) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', FALSE)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, course.getInstructorId());
            stmt.setString(2, course.getTitle());
            stmt.setString(3, course.getDescription());
            stmt.setString(4, course.getThumbnailPath());
            stmt.setString(5, course.getCategory());
            stmt.setString(6, course.getDifficultyLevel());
            stmt.setInt(7, course.getEstimatedHours());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    course.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update course
     */
    public boolean update(Course course) {
        String sql = "UPDATE courses SET title = ?, description = ?, thumbnail_path = ?, " +
                     "category = ?, difficulty_level = ?, estimated_hours = ? " +
                     "WHERE id = ? AND instructor_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getThumbnailPath());
            stmt.setString(4, course.getCategory());
            stmt.setString(5, course.getDifficultyLevel());
            stmt.setInt(6, course.getEstimatedHours());
            stmt.setInt(7, course.getId());
            stmt.setInt(8, course.getInstructorId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete course (only if no enrollments)
     */
    public boolean delete(int courseId, int instructorId) {
        // First check if there are any enrollments
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";
        String deleteSql = "DELETE FROM courses WHERE id = ? AND instructor_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, courseId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Has enrollments, cannot delete
                return false;
            }

            // No enrollments, safe to delete
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, courseId);
                deleteStmt.setInt(2, instructorId);
                return deleteStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Approve course (Admin only)
     */
    public boolean approveCourse(int courseId, int adminId) {
        String sql = "UPDATE courses SET status = 'APPROVED', is_published = TRUE, " +
                     "approved_by = ?, approved_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);
            stmt.setInt(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reject course (Admin only)
     */
    public boolean rejectCourse(int courseId, String reason) {
        String sql = "UPDATE courses SET status = 'REJECTED', rejection_reason = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reason);
            stmt.setInt(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Publish/unpublish course (Instructor only for approved courses)
     */
    public boolean togglePublish(int courseId, int instructorId, boolean publish) {
        String sql = "UPDATE courses SET is_published = ? " +
                     "WHERE id = ? AND instructor_id = ? AND status = 'APPROVED'";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, publish);
            stmt.setInt(2, courseId);
            stmt.setInt(3, instructorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Map ResultSet to Course object
     */
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setInstructorId(rs.getInt("instructor_id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setThumbnailPath(rs.getString("thumbnail_path"));
        course.setStatus(rs.getString("status"));
        course.setRejectionReason(rs.getString("rejection_reason"));
        course.setCategory(rs.getString("category"));
        course.setDifficultyLevel(rs.getString("difficulty_level"));
        course.setEstimatedHours(rs.getInt("estimated_hours"));
        course.setPublished(rs.getBoolean("is_published"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            course.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            course.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp approvedAt = rs.getTimestamp("approved_at");
        if (approvedAt != null) {
            course.setApprovedAt(approvedAt.toLocalDateTime());
        }

        Integer approvedBy = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            course.setApprovedBy(approvedBy);
        }

        return course;
    }
}
