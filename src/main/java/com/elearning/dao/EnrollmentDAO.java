package com.elearning.dao;

import com.elearning.model.Enrollment;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Enrollment entity
 */
public class EnrollmentDAO {

    /**
     * Find all enrollments for a user
     */
    public List<Enrollment> findByUserId(int userId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.title as course_title, c.thumbnail_path as course_thumbnail " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "WHERE e.user_id = ? " +
                     "ORDER BY e.last_accessed_at DESC NULLS LAST, e.enrolled_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Enrollment enrollment = mapResultSetToEnrollment(rs);
                enrollment.setCourseTitle(rs.getString("course_title"));
                enrollment.setCourseThumbnail(rs.getString("course_thumbnail"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    /**
     * Find all enrollments for a course (Instructor/Admin view)
     */
    public List<Enrollment> findByCourseId(int courseId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name as user_name, u.email " +
                     "FROM enrollments e " +
                     "JOIN users u ON e.user_id = u.id " +
                     "WHERE e.course_id = ? " +
                     "ORDER BY e.enrolled_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Enrollment enrollment = mapResultSetToEnrollment(rs);
                enrollment.setUserName(rs.getString("user_name"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    /**
     * Find specific enrollment
     */
    public Enrollment findByUserAndCourse(int userId, int courseId) {
        String sql = "SELECT e.*, c.title as course_title, c.thumbnail_path as course_thumbnail " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "WHERE e.user_id = ? AND e.course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Enrollment enrollment = mapResultSetToEnrollment(rs);
                enrollment.setCourseTitle(rs.getString("course_title"));
                enrollment.setCourseThumbnail(rs.getString("course_thumbnail"));
                return enrollment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check if user is enrolled in a course
     */
    public boolean isEnrolled(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Enroll user in a course
     */
    public boolean enroll(int userId, int courseId) {
        // Check if already enrolled
        if (isEnrolled(userId, courseId)) {
            return false;
        }

        String sql = "INSERT INTO enrollments (user_id, course_id, progress_percent) VALUES (?, ?, 0.0)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Unenroll user from a course (only if progress < 10%)
     */
    public boolean unenroll(int userId, int courseId) {
        // Check current progress
        Enrollment enrollment = findByUserAndCourse(userId, courseId);
        if (enrollment == null) {
            return false;
        }

        // Only allow unenrollment if progress is less than 10%
        if (enrollment.getProgressPercent() >= 10.0) {
            return false;
        }

        String sql = "DELETE FROM enrollments WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update progress percentage
     */
    public boolean updateProgress(int userId, int courseId, double progressPercent) {
        String sql = "UPDATE enrollments SET progress_percent = ?, " +
                     "completed_at = CASE WHEN ? >= 100.0 AND completed_at IS NULL THEN NOW() ELSE completed_at END " +
                     "WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, progressPercent);
            stmt.setDouble(2, progressPercent);
            stmt.setInt(3, userId);
            stmt.setInt(4, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update last accessed timestamp
     */
    public boolean updateLastAccessed(int userId, int courseId) {
        String sql = "UPDATE enrollments SET last_accessed_at = NOW() WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get enrollment count for a course
     */
    public int getEnrollmentCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get completion count for a course
     */
    public int getCompletionCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND completed_at IS NOT NULL";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get average progress for a course
     */
    public double getAverageProgress(int courseId) {
        String sql = "SELECT AVG(progress_percent) FROM enrollments WHERE course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Get in-progress courses for a user (progress > 0 and < 100)
     */
    public List<Enrollment> findInProgressByUserId(int userId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.title as course_title, c.thumbnail_path as course_thumbnail " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "WHERE e.user_id = ? AND e.progress_percent > 0 AND e.progress_percent < 100 " +
                     "ORDER BY e.last_accessed_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Enrollment enrollment = mapResultSetToEnrollment(rs);
                enrollment.setCourseTitle(rs.getString("course_title"));
                enrollment.setCourseThumbnail(rs.getString("course_thumbnail"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    /**
     * Get completed courses for a user
     */
    public List<Enrollment> findCompletedByUserId(int userId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.title as course_title, c.thumbnail_path as course_thumbnail " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "WHERE e.user_id = ? AND e.completed_at IS NOT NULL " +
                     "ORDER BY e.completed_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Enrollment enrollment = mapResultSetToEnrollment(rs);
                enrollment.setCourseTitle(rs.getString("course_title"));
                enrollment.setCourseThumbnail(rs.getString("course_thumbnail"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    /**
     * Map ResultSet to Enrollment object
     */
    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getInt("id"));
        enrollment.setUserId(rs.getInt("user_id"));
        enrollment.setCourseId(rs.getInt("course_id"));
        enrollment.setProgressPercent(rs.getDouble("progress_percent"));

        Timestamp enrolledAt = rs.getTimestamp("enrolled_at");
        if (enrolledAt != null) {
            enrollment.setEnrolledAt(enrolledAt.toLocalDateTime());
        }

        Timestamp lastAccessedAt = rs.getTimestamp("last_accessed_at");
        if (lastAccessedAt != null) {
            enrollment.setLastAccessedAt(lastAccessedAt.toLocalDateTime());
        }

        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            enrollment.setCompletedAt(completedAt.toLocalDateTime());
        }

        return enrollment;
    }
}
