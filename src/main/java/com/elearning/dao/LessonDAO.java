package com.elearning.dao;

import com.elearning.model.Lesson;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Lesson entity
 */
public class LessonDAO {

    /**
     * Find all lessons for a course (ordered by order_index)
     */
    public List<Lesson> findByCourseId(int courseId) {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT * FROM lessons WHERE course_id = ? ORDER BY order_index ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lessons.add(mapResultSetToLesson(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lessons;
    }

    /**
     * Find preview lessons for a course (for non-enrolled users)
     */
    public List<Lesson> findPreviewLessons(int courseId) {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT * FROM lessons WHERE course_id = ? AND is_preview = TRUE ORDER BY order_index ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lessons.add(mapResultSetToLesson(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lessons;
    }

    /**
     * Find lesson by ID
     */
    public Lesson findById(int id) {
        String sql = "SELECT * FROM lessons WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToLesson(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert new lesson
     */
    public boolean insert(Lesson lesson) {
        String sql = "INSERT INTO lessons (course_id, title, video_path, content_text, " +
                     "duration_minutes, order_index, is_preview) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, lesson.getCourseId());
            stmt.setString(2, lesson.getTitle());
            stmt.setString(3, lesson.getVideoPath());
            // Map description to content_text for database storage
            String content = lesson.getDescription() != null ? lesson.getDescription() : lesson.getContentText();
            stmt.setString(4, content);

            if (lesson.getDurationMinutes() != null) {
                stmt.setInt(5, lesson.getDurationMinutes());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setInt(6, lesson.getOrderIndex());
            stmt.setBoolean(7, lesson.isPreview());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    lesson.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update lesson
     */
    public boolean update(Lesson lesson) {
        String sql = "UPDATE lessons SET title = ?, video_path = ?, content_text = ?, " +
                     "duration_minutes = ?, order_index = ?, is_preview = ? " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lesson.getTitle());
            stmt.setString(2, lesson.getVideoPath());
            // Map description to content_text for database storage
            String content = lesson.getDescription() != null ? lesson.getDescription() : lesson.getContentText();
            stmt.setString(3, content);

            if (lesson.getDurationMinutes() != null) {
                stmt.setInt(4, lesson.getDurationMinutes());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, lesson.getOrderIndex());
            stmt.setBoolean(6, lesson.isPreview());
            stmt.setInt(7, lesson.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete lesson (only if no lesson progress records exist)
     */
    public boolean delete(int lessonId) {
        // First check if there are any progress records
        String checkSql = "SELECT COUNT(*) FROM lesson_progress WHERE lesson_id = ?";
        String deleteSql = "DELETE FROM lessons WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, lessonId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Has progress records, cannot delete
                return false;
            }

            // No progress records, safe to delete
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, lessonId);
                return deleteStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update order index for a lesson
     */
    public boolean updateOrderIndex(int lessonId, int newOrderIndex) {
        String sql = "UPDATE lessons SET order_index = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newOrderIndex);
            stmt.setInt(2, lessonId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the next order index for a course (for adding new lessons)
     */
    public int getNextOrderIndex(int courseId) {
        String sql = "SELECT COALESCE(MAX(order_index), 0) + 1 FROM lessons WHERE course_id = ?";

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
        return 1;
    }

    /**
     * Count total lessons in a course
     */
    public int countLessons(int courseId) {
        String sql = "SELECT COUNT(*) FROM lessons WHERE course_id = ?";

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
     * Get total duration of all lessons in a course
     */
    public int getTotalDuration(int courseId) {
        String sql = "SELECT COALESCE(SUM(duration_minutes), 0) FROM lessons WHERE course_id = ?";

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
     * Map ResultSet to Lesson object
     */
    private Lesson mapResultSetToLesson(ResultSet rs) throws SQLException {
        Lesson lesson = new Lesson();
        lesson.setId(rs.getInt("id"));
        lesson.setCourseId(rs.getInt("course_id"));
        lesson.setTitle(rs.getString("title"));
        lesson.setVideoPath(rs.getString("video_path"));
        // Map content_text from database to both contentText and description fields
        String content = rs.getString("content_text");
        lesson.setContentText(content);
        lesson.setDescription(content);

        Integer durationMinutes = rs.getInt("duration_minutes");
        if (!rs.wasNull()) {
            lesson.setDurationMinutes(durationMinutes);
        }

        lesson.setOrderIndex(rs.getInt("order_index"));
        lesson.setPreview(rs.getBoolean("is_preview"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            lesson.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            lesson.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return lesson;
    }
}
