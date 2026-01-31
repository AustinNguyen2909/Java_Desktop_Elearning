package com.elearning.dao;

import com.elearning.model.TestAttempt;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TestAttempt operations
 */
public class TestAttemptDAO {

    /**
     * Create a new test attempt
     */
    public TestAttempt create(TestAttempt attempt) {
        String sql = """
            INSERT INTO test_attempts (test_id, user_id, course_id, attempt_number, 
                                     total_questions, total_points, earned_points, 
                                     score_percentage, status, passed, started_at, 
                                     completed_at, time_spent_seconds) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, attempt.getTestId());
            stmt.setInt(2, attempt.getUserId());
            stmt.setInt(3, attempt.getCourseId());
            stmt.setInt(4, attempt.getAttemptNumber());
            stmt.setInt(5, attempt.getTotalQuestions());
            stmt.setDouble(6, attempt.getTotalPoints());
            stmt.setDouble(7, attempt.getEarnedPoints());
            stmt.setDouble(8, attempt.getScorePercentage());
            stmt.setString(9, attempt.getStatus());
            stmt.setBoolean(10, attempt.isPassed());
            stmt.setTimestamp(11, attempt.getStartedAt() != null ? 
                Timestamp.valueOf(attempt.getStartedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(12, attempt.getCompletedAt() != null ? 
                Timestamp.valueOf(attempt.getCompletedAt()) : null);
            stmt.setInt(13, attempt.getTimeSpentSeconds());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attempt.setId(generatedKeys.getInt(1));
                        return attempt;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an existing test attempt
     */
    public boolean update(TestAttempt attempt) {
        String sql = """
            UPDATE test_attempts 
            SET earned_points = ?, score_percentage = ?, status = ?, 
                passed = ?, completed_at = ?, time_spent_seconds = ?
            WHERE id = ?
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, attempt.getEarnedPoints());
            stmt.setDouble(2, attempt.getScorePercentage());
            stmt.setString(3, attempt.getStatus());
            stmt.setBoolean(4, attempt.isPassed());
            stmt.setTimestamp(5, attempt.getCompletedAt() != null ? 
                Timestamp.valueOf(attempt.getCompletedAt()) : null);
            stmt.setInt(6, attempt.getTimeSpentSeconds());
            stmt.setInt(7, attempt.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find test attempt by ID
     */
    public TestAttempt findById(int id) {
        String sql = "SELECT * FROM test_attempts WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestAttempt(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all attempts for a specific test
     */
    public List<TestAttempt> findByTestId(int testId) {
        String sql = """
            SELECT ta.*, u.full_name as user_name 
            FROM test_attempts ta 
            JOIN users u ON ta.user_id = u.id 
            WHERE ta.test_id = ? 
            ORDER BY ta.started_at DESC
        """;

        List<TestAttempt> attempts = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TestAttempt attempt = mapResultSetToTestAttempt(rs);
                    attempt.setUserName(rs.getString("user_name"));
                    attempts.add(attempt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attempts;
    }

    /**
     * Get all attempts for a specific user and test
     */
    public List<TestAttempt> findByUserAndTest(int userId, int testId) {
        String sql = "SELECT * FROM test_attempts WHERE user_id = ? AND test_id = ? ORDER BY started_at DESC";

        List<TestAttempt> attempts = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToTestAttempt(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attempts;
    }

    /**
     * Get the best attempt (highest score) for a user and test
     */
    public TestAttempt findBestAttempt(int userId, int testId) {
        String sql = """
            SELECT * FROM test_attempts 
            WHERE user_id = ? AND test_id = ? AND status = 'COMPLETED'
            ORDER BY score_percentage DESC, started_at DESC 
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestAttempt(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the next attempt number for a user and test
     */
    public int getNextAttemptNumber(int userId, int testId) {
        String sql = "SELECT COALESCE(MAX(attempt_number), 0) + 1 FROM test_attempts WHERE user_id = ? AND test_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default to first attempt
    }

    /**
     * Check if user can take another attempt
     */
    public boolean canTakeAnotherAttempt(int userId, int testId, Integer maxAttempts) {
        if (maxAttempts == null) {
            return true; // Unlimited attempts
        }

        String sql = "SELECT COUNT(*) FROM test_attempts WHERE user_id = ? AND test_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int currentAttempts = rs.getInt(1);
                    return currentAttempts < maxAttempts;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get test statistics for instructor
     */
    public TestStatistics getTestStatistics(int testId) {
        String sql = """
            SELECT 
                COUNT(DISTINCT user_id) as total_students,
                COUNT(*) as total_attempts,
                AVG(score_percentage) as average_score,
                SUM(CASE WHEN passed = TRUE THEN 1 ELSE 0 END) as passed_count,
                SUM(CASE WHEN status = 'COMPLETED' AND passed = FALSE THEN 1 ELSE 0 END) as failed_count,
                AVG(time_spent_seconds) as average_time_seconds
            FROM test_attempts 
            WHERE test_id = ? AND status = 'COMPLETED'
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TestStatistics stats = new TestStatistics();
                    stats.setTotalStudents(rs.getInt("total_students"));
                    stats.setTotalAttempts(rs.getInt("total_attempts"));
                    stats.setAverageScore(rs.getDouble("average_score"));
                    stats.setPassedCount(rs.getInt("passed_count"));
                    stats.setFailedCount(rs.getInt("failed_count"));
                    stats.setAverageTimeSeconds(rs.getDouble("average_time_seconds"));
                    return stats;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TestStatistics(); // Return empty stats on error
    }

    /**
     * Delete test attempt
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM test_attempts WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Map ResultSet to TestAttempt object
     */
    private TestAttempt mapResultSetToTestAttempt(ResultSet rs) throws SQLException {
        TestAttempt attempt = new TestAttempt();
        attempt.setId(rs.getInt("id"));
        attempt.setTestId(rs.getInt("test_id"));
        attempt.setUserId(rs.getInt("user_id"));
        attempt.setCourseId(rs.getInt("course_id"));
        attempt.setAttemptNumber(rs.getInt("attempt_number"));
        attempt.setTotalQuestions(rs.getInt("total_questions"));
        attempt.setTotalPoints(rs.getDouble("total_points"));
        attempt.setEarnedPoints(rs.getDouble("earned_points"));
        attempt.setScorePercentage(rs.getDouble("score_percentage"));
        attempt.setStatus(rs.getString("status"));
        attempt.setPassed(rs.getBoolean("passed"));
        
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) {
            attempt.setStartedAt(startedAt.toLocalDateTime());
        }
        
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            attempt.setCompletedAt(completedAt.toLocalDateTime());
        }
        
        attempt.setTimeSpentSeconds(rs.getInt("time_spent_seconds"));
        return attempt;
    }

    /**
     * Inner class for test statistics
     */
    public static class TestStatistics {
        private int totalStudents;
        private int totalAttempts;
        private double averageScore;
        private int passedCount;
        private int failedCount;
        private double averageTimeSeconds;

        // Getters and setters
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        
        public int getPassedCount() { return passedCount; }
        public void setPassedCount(int passedCount) { this.passedCount = passedCount; }
        
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        
        public double getAverageTimeSeconds() { return averageTimeSeconds; }
        public void setAverageTimeSeconds(double averageTimeSeconds) { this.averageTimeSeconds = averageTimeSeconds; }
        
        public double getPassRate() {
            int total = passedCount + failedCount;
            return total > 0 ? (passedCount * 100.0 / total) : 0.0;
        }
    }
}