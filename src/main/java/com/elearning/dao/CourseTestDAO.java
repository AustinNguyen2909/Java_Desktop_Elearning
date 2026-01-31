package com.elearning.dao;

import com.elearning.model.CourseTest;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for CourseTest entity
 * Handles CRUD operations for course tests
 */
public class CourseTestDAO {

    /**
     * Find test by course ID
     */
    public CourseTest findByCourseId(int courseId) {
        String sql = "SELECT * FROM course_tests WHERE course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourseTest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find test by ID
     */
    public CourseTest findById(int testId) {
        String sql = "SELECT * FROM course_tests WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourseTest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new test
     */
    public CourseTest create(CourseTest test) {
        String sql = "INSERT INTO course_tests (course_id, title, description, passing_score, " +
                     "time_limit_minutes, shuffle_questions, shuffle_options, max_attempts, is_published) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, test.getCourseId());
            stmt.setString(2, test.getTitle());
            stmt.setString(3, test.getDescription());
            stmt.setDouble(4, test.getPassingScore());

            if (test.getTimeLimitMinutes() != null) {
                stmt.setInt(5, test.getTimeLimitMinutes());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setBoolean(6, test.getShuffleQuestions());
            stmt.setBoolean(7, test.getShuffleOptions());

            if (test.getMaxAttempts() != null) {
                stmt.setInt(8, test.getMaxAttempts());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setBoolean(9, test.getIsPublished());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    test.setId(rs.getInt(1));
                }
                return test;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an existing test
     */
    public boolean update(CourseTest test) {
        String sql = "UPDATE course_tests SET title = ?, description = ?, passing_score = ?, " +
                     "time_limit_minutes = ?, shuffle_questions = ?, shuffle_options = ?, " +
                     "max_attempts = ?, is_published = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, test.getTitle());
            stmt.setString(2, test.getDescription());
            stmt.setDouble(3, test.getPassingScore());

            if (test.getTimeLimitMinutes() != null) {
                stmt.setInt(4, test.getTimeLimitMinutes());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setBoolean(5, test.getShuffleQuestions());
            stmt.setBoolean(6, test.getShuffleOptions());

            if (test.getMaxAttempts() != null) {
                stmt.setInt(7, test.getMaxAttempts());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setBoolean(8, test.getIsPublished());
            stmt.setInt(9, test.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a test and all associated data (cascade)
     */
    public boolean delete(int testId) {
        String sql = "DELETE FROM course_tests WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Publish a test (make it visible to students)
     */
    public boolean publish(int testId) {
        String sql = "UPDATE course_tests SET is_published = TRUE WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Unpublish a test (hide from students)
     */
    public boolean unpublish(int testId) {
        String sql = "UPDATE course_tests SET is_published = FALSE WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if test exists for a course
     */
    public boolean existsByCourseId(int courseId) {
        String sql = "SELECT COUNT(*) FROM course_tests WHERE course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
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
     * Get question count for a test
     */
    public int getQuestionCount(int testId) {
        String sql = "SELECT COUNT(*) FROM test_questions WHERE test_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
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
     * Get total points for a test
     */
    public double getTotalPoints(int testId) {
        String sql = "SELECT SUM(points) FROM test_questions WHERE test_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
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
     * Map ResultSet to CourseTest object
     */
    private CourseTest mapResultSetToCourseTest(ResultSet rs) throws SQLException {
        CourseTest test = new CourseTest();
        test.setId(rs.getInt("id"));
        test.setCourseId(rs.getInt("course_id"));
        test.setTitle(rs.getString("title"));
        test.setDescription(rs.getString("description"));
        test.setPassingScore(rs.getDouble("passing_score"));

        int timeLimitMinutes = rs.getInt("time_limit_minutes");
        if (!rs.wasNull()) {
            test.setTimeLimitMinutes(timeLimitMinutes);
        }

        test.setShuffleQuestions(rs.getBoolean("shuffle_questions"));
        test.setShuffleOptions(rs.getBoolean("shuffle_options"));

        int maxAttempts = rs.getInt("max_attempts");
        if (!rs.wasNull()) {
            test.setMaxAttempts(maxAttempts);
        }

        test.setIsPublished(rs.getBoolean("is_published"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            test.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            test.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return test;
    }
}
