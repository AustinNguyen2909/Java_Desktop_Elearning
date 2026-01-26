package com.elearning.dao;

import com.elearning.model.LessonProgress;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for LessonProgress entity
 */
public class LessonProgressDAO {

    /**
     * Find all lesson progress for a user in a specific course
     */
    public List<LessonProgress> findByUserAndCourse(int userId, int courseId) {
        List<LessonProgress> progressList = new ArrayList<>();
        String sql = "SELECT lp.* FROM lesson_progress lp " +
                     "JOIN lessons l ON lp.lesson_id = l.id " +
                     "WHERE lp.user_id = ? AND l.course_id = ? " +
                     "ORDER BY l.order_index ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                progressList.add(mapResultSetToLessonProgress(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return progressList;
    }

    /**
     * Find progress for a specific lesson and user
     */
    public LessonProgress findByUserAndLesson(int userId, int lessonId) {
        String sql = "SELECT * FROM lesson_progress WHERE user_id = ? AND lesson_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToLessonProgress(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mark lesson as opened (creates progress record if doesn't exist)
     */
    public boolean markAsOpened(int userId, int lessonId) {
        // Check if progress already exists
        LessonProgress existing = findByUserAndLesson(userId, lessonId);

        if (existing != null) {
            // Update last opened time
            String sql = "UPDATE lesson_progress SET last_opened_at = NOW() WHERE user_id = ? AND lesson_id = ?";

            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setInt(2, lessonId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Create new progress record
            String sql = "INSERT INTO lesson_progress (user_id, lesson_id, is_completed, last_opened_at) " +
                         "VALUES (?, ?, FALSE, NOW())";

            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setInt(2, lessonId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Mark lesson as completed
     */
    public boolean markAsCompleted(int userId, int lessonId) {
        // Check if progress exists
        LessonProgress existing = findByUserAndLesson(userId, lessonId);

        if (existing != null) {
            // Update to completed
            String sql = "UPDATE lesson_progress SET is_completed = TRUE, completed_at = NOW(), " +
                         "last_opened_at = NOW() WHERE user_id = ? AND lesson_id = ?";

            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setInt(2, lessonId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Create as completed
            String sql = "INSERT INTO lesson_progress (user_id, lesson_id, is_completed, completed_at, last_opened_at) " +
                         "VALUES (?, ?, TRUE, NOW(), NOW())";

            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setInt(2, lessonId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Mark lesson as incomplete (reset completion)
     */
    public boolean markAsIncomplete(int userId, int lessonId) {
        String sql = "UPDATE lesson_progress SET is_completed = FALSE, completed_at = NULL " +
                     "WHERE user_id = ? AND lesson_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get completion count for a course
     */
    public int getCompletedLessonCount(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM lesson_progress lp " +
                     "JOIN lessons l ON lp.lesson_id = l.id " +
                     "WHERE lp.user_id = ? AND l.course_id = ? AND lp.is_completed = TRUE";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
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
     * Check if lesson is completed
     */
    public boolean isCompleted(int userId, int lessonId) {
        String sql = "SELECT is_completed FROM lesson_progress WHERE user_id = ? AND lesson_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_completed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get next incomplete lesson for a course
     */
    public Integer getNextIncompleteLesson(int userId, int courseId) {
        String sql = "SELECT l.id FROM lessons l " +
                     "LEFT JOIN lesson_progress lp ON l.id = lp.lesson_id AND lp.user_id = ? " +
                     "WHERE l.course_id = ? AND (lp.is_completed IS NULL OR lp.is_completed = FALSE) " +
                     "ORDER BY l.order_index ASC LIMIT 1";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calculate progress percentage for a course
     */
    public double calculateProgressPercent(int userId, int courseId) {
        String sql = "SELECT " +
                     "COUNT(l.id) as total_lessons, " +
                     "COUNT(CASE WHEN lp.is_completed = TRUE THEN 1 END) as completed_lessons " +
                     "FROM lessons l " +
                     "LEFT JOIN lesson_progress lp ON l.id = lp.lesson_id AND lp.user_id = ? " +
                     "WHERE l.course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int totalLessons = rs.getInt("total_lessons");
                int completedLessons = rs.getInt("completed_lessons");

                if (totalLessons == 0) {
                    return 0.0;
                }

                return (completedLessons * 100.0) / totalLessons;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Delete all progress for a user in a course (when unenrolling)
     */
    public boolean deleteByUserAndCourse(int userId, int courseId) {
        String sql = "DELETE lp FROM lesson_progress lp " +
                     "JOIN lessons l ON lp.lesson_id = l.id " +
                     "WHERE lp.user_id = ? AND l.course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Map ResultSet to LessonProgress object
     */
    private LessonProgress mapResultSetToLessonProgress(ResultSet rs) throws SQLException {
        LessonProgress progress = new LessonProgress();
        progress.setId(rs.getInt("id"));
        progress.setUserId(rs.getInt("user_id"));
        progress.setLessonId(rs.getInt("lesson_id"));
        progress.setCompleted(rs.getBoolean("is_completed"));

        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            progress.setCompletedAt(completedAt.toLocalDateTime());
        }

        Timestamp lastOpenedAt = rs.getTimestamp("last_opened_at");
        if (lastOpenedAt != null) {
            progress.setLastOpenedAt(lastOpenedAt.toLocalDateTime());
        }

        return progress;
    }
}
