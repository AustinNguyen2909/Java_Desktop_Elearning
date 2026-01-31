package com.elearning.dao;

import com.elearning.model.TestQuestion;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for TestQuestion entity
 * Handles CRUD operations for test questions
 */
public class TestQuestionDAO {

    /**
     * Find all questions for a test (ordered by order_index)
     */
    public List<TestQuestion> findByTestId(int testId) {
        List<TestQuestion> questions = new ArrayList<>();
        String sql = "SELECT * FROM test_questions WHERE test_id = ? ORDER BY order_index ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(mapResultSetToTestQuestion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Find question by ID
     */
    public TestQuestion findById(int questionId) {
        String sql = "SELECT * FROM test_questions WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTestQuestion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new question
     */
    public TestQuestion create(TestQuestion question) {
        String sql = "INSERT INTO test_questions (test_id, question_text, order_index, points) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, question.getTestId());
            stmt.setString(2, question.getQuestionText());
            stmt.setInt(3, question.getOrderIndex());
            stmt.setDouble(4, question.getPoints());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    question.setId(rs.getInt(1));
                }
                return question;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an existing question
     */
    public boolean update(TestQuestion question) {
        String sql = "UPDATE test_questions SET question_text = ?, order_index = ?, points = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, question.getQuestionText());
            stmt.setInt(2, question.getOrderIndex());
            stmt.setDouble(3, question.getPoints());
            stmt.setInt(4, question.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a question and all its options (cascade)
     */
    public boolean delete(int questionId) {
        String sql = "DELETE FROM test_questions WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reorder questions (update order_index for multiple questions)
     */
    public boolean reorder(List<Integer> questionIds) {
        String sql = "UPDATE test_questions SET order_index = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < questionIds.size(); i++) {
                stmt.setInt(1, i);
                stmt.setInt(2, questionIds.get(i));
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the next order index for appending a new question
     */
    public int getNextOrderIndex(int testId) {
        String sql = "SELECT COALESCE(MAX(order_index), -1) + 1 FROM test_questions WHERE test_id = ?";

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
     * Count questions for a test
     */
    public int countQuestions(int testId) {
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
     * Map ResultSet to TestQuestion object
     */
    private TestQuestion mapResultSetToTestQuestion(ResultSet rs) throws SQLException {
        TestQuestion question = new TestQuestion();
        question.setId(rs.getInt("id"));
        question.setTestId(rs.getInt("test_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setOrderIndex(rs.getInt("order_index"));
        question.setPoints(rs.getDouble("points"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            question.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            question.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return question;
    }
}
