package com.elearning.dao;

import com.elearning.model.AnswerOption;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for AnswerOption entity
 * Handles CRUD operations for answer options
 */
public class AnswerOptionDAO {

    /**
     * Find all options for a question (ordered by option_letter A, B, C, D)
     */
    public List<AnswerOption> findByQuestionId(int questionId) {
        List<AnswerOption> options = new ArrayList<>();
        String sql = "SELECT * FROM answer_options WHERE question_id = ? ORDER BY option_letter ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                options.add(mapResultSetToAnswerOption(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }

    /**
     * Find option by ID
     */
    public AnswerOption findById(int optionId) {
        String sql = "SELECT * FROM answer_options WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, optionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAnswerOption(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new answer option
     */
    public AnswerOption create(AnswerOption option) {
        String sql = "INSERT INTO answer_options (question_id, option_text, is_correct, option_letter) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, option.getQuestionId());
            stmt.setString(2, option.getOptionText());
            stmt.setBoolean(3, option.getIsCorrect());
            stmt.setString(4, option.getOptionLetter());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    option.setId(rs.getInt(1));
                }
                return option;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an existing option
     */
    public boolean update(AnswerOption option) {
        String sql = "UPDATE answer_options SET option_text = ?, is_correct = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, option.getOptionText());
            stmt.setBoolean(2, option.getIsCorrect());
            stmt.setInt(3, option.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete an option
     */
    public boolean delete(int optionId) {
        String sql = "DELETE FROM answer_options WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, optionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete all options for a question
     */
    public boolean deleteByQuestionId(int questionId) {
        String sql = "DELETE FROM answer_options WHERE question_id = ?";

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
     * Find the correct option for a question
     */
    public AnswerOption findCorrectOption(int questionId) {
        String sql = "SELECT * FROM answer_options WHERE question_id = ? AND is_correct = TRUE";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAnswerOption(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Validate that a question has exactly 4 options and exactly 1 correct option
     */
    public boolean validateQuestion(int questionId) {
        // Check option count
        String countSql = "SELECT COUNT(*) FROM answer_options WHERE question_id = ?";
        String correctSql = "SELECT COUNT(*) FROM answer_options WHERE question_id = ? AND is_correct = TRUE";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql);
             PreparedStatement correctStmt = conn.prepareStatement(correctSql)) {

            // Check total count = 4
            countStmt.setInt(1, questionId);
            ResultSet countRs = countStmt.executeQuery();
            if (!countRs.next() || countRs.getInt(1) != 4) {
                return false;
            }

            // Check correct count = 1
            correctStmt.setInt(1, questionId);
            ResultSet correctRs = correctStmt.executeQuery();
            if (!correctRs.next() || correctRs.getInt(1) != 1) {
                return false;
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Map ResultSet to AnswerOption object
     */
    private AnswerOption mapResultSetToAnswerOption(ResultSet rs) throws SQLException {
        AnswerOption option = new AnswerOption();
        option.setId(rs.getInt("id"));
        option.setQuestionId(rs.getInt("question_id"));
        option.setOptionText(rs.getString("option_text"));
        option.setIsCorrect(rs.getBoolean("is_correct"));
        option.setOptionLetter(rs.getString("option_letter"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            option.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            option.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return option;
    }
}
