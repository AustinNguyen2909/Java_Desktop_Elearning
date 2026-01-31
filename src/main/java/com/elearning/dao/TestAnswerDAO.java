package com.elearning.dao;

import com.elearning.model.TestAnswer;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TestAnswer operations
 */
public class TestAnswerDAO {

    /**
     * Create a new test answer
     */
    public TestAnswer create(TestAnswer answer) {
        String sql = """
            INSERT INTO test_answers (attempt_id, question_id, selected_option_id, 
                                    is_correct, points_earned, answered_at) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, answer.getAttemptId());
            stmt.setInt(2, answer.getQuestionId());
            stmt.setInt(3, answer.getSelectedOptionId());
            stmt.setBoolean(4, answer.isCorrect());
            stmt.setDouble(5, answer.getPointsEarned());
            stmt.setTimestamp(6, answer.getAnsweredAt() != null ? 
                Timestamp.valueOf(answer.getAnsweredAt()) : Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        answer.setId(generatedKeys.getInt(1));
                        return answer;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an existing test answer
     */
    public boolean update(TestAnswer answer) {
        String sql = """
            UPDATE test_answers 
            SET selected_option_id = ?, is_correct = ?, points_earned = ?, answered_at = ?
            WHERE id = ?
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, answer.getSelectedOptionId());
            stmt.setBoolean(2, answer.isCorrect());
            stmt.setDouble(3, answer.getPointsEarned());
            stmt.setTimestamp(4, answer.getAnsweredAt() != null ? 
                Timestamp.valueOf(answer.getAnsweredAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, answer.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find test answer by ID
     */
    public TestAnswer findById(int id) {
        String sql = "SELECT * FROM test_answers WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestAnswer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all answers for a specific attempt
     */
    public List<TestAnswer> findByAttemptId(int attemptId) {
        String sql = """
            SELECT ta.*, tq.question_text, tq.points as question_points,
                   ao.option_text, ao.option_letter, ao.is_correct as option_is_correct
            FROM test_answers ta
            JOIN test_questions tq ON ta.question_id = tq.id
            JOIN answer_options ao ON ta.selected_option_id = ao.id
            WHERE ta.attempt_id = ?
            ORDER BY tq.order_index
        """;

        List<TestAnswer> answers = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TestAnswer answer = mapResultSetToTestAnswer(rs);
                    
                    // Set additional fields from joins
                    answer.setQuestionText(rs.getString("question_text"));
                    answer.setQuestionPoints(rs.getDouble("question_points"));
                    answer.setSelectedOptionText(rs.getString("option_text"));
                    answer.setSelectedOptionLetter(rs.getString("option_letter"));
                    
                    answers.add(answer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    /**
     * Get answer for a specific question in an attempt
     */
    public TestAnswer findByAttemptAndQuestion(int attemptId, int questionId) {
        String sql = "SELECT * FROM test_answers WHERE attempt_id = ? AND question_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestAnswer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save or update an answer (upsert operation)
     */
    public TestAnswer saveAnswer(TestAnswer answer) {
        // Check if answer already exists
        TestAnswer existing = findByAttemptAndQuestion(answer.getAttemptId(), answer.getQuestionId());
        
        if (existing != null) {
            // Update existing answer
            answer.setId(existing.getId());
            if (update(answer)) {
                return answer;
            }
        } else {
            // Create new answer
            return create(answer);
        }
        return null;
    }

    /**
     * Get question statistics for instructor analysis
     */
    public List<QuestionStatistics> getQuestionStatistics(int testId) {
        String sql = """
            SELECT 
                tq.id as question_id,
                tq.question_text,
                tq.order_index,
                tq.points,
                COUNT(ta.id) as total_answers,
                SUM(CASE WHEN ta.is_correct = TRUE THEN 1 ELSE 0 END) as correct_answers,
                AVG(CASE WHEN ta.is_correct = TRUE THEN 1.0 ELSE 0.0 END) * 100 as correct_percentage
            FROM test_questions tq
            LEFT JOIN test_answers ta ON tq.id = ta.question_id
            LEFT JOIN test_attempts att ON ta.attempt_id = att.id AND att.status = 'COMPLETED'
            WHERE tq.test_id = ?
            GROUP BY tq.id, tq.question_text, tq.order_index, tq.points
            ORDER BY tq.order_index
        """;

        List<QuestionStatistics> stats = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionStatistics stat = new QuestionStatistics();
                    stat.setQuestionId(rs.getInt("question_id"));
                    stat.setQuestionText(rs.getString("question_text"));
                    stat.setOrderIndex(rs.getInt("order_index"));
                    stat.setPoints(rs.getDouble("points"));
                    stat.setTotalAnswers(rs.getInt("total_answers"));
                    stat.setCorrectAnswers(rs.getInt("correct_answers"));
                    stat.setCorrectPercentage(rs.getDouble("correct_percentage"));
                    stats.add(stat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Get option selection statistics for a question
     */
    public List<OptionStatistics> getOptionStatistics(int questionId) {
        String sql = """
            SELECT 
                ao.id as option_id,
                ao.option_text,
                ao.option_letter,
                ao.is_correct,
                COUNT(ta.id) as selection_count,
                COUNT(ta.id) * 100.0 / (
                    SELECT COUNT(*) 
                    FROM test_answers ta2 
                    WHERE ta2.question_id = ?
                ) as selection_percentage
            FROM answer_options ao
            LEFT JOIN test_answers ta ON ao.id = ta.selected_option_id
            WHERE ao.question_id = ?
            GROUP BY ao.id, ao.option_text, ao.option_letter, ao.is_correct
            ORDER BY ao.option_letter
        """;

        List<OptionStatistics> stats = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            stmt.setInt(2, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OptionStatistics stat = new OptionStatistics();
                    stat.setOptionId(rs.getInt("option_id"));
                    stat.setOptionText(rs.getString("option_text"));
                    stat.setOptionLetter(rs.getString("option_letter"));
                    stat.setCorrect(rs.getBoolean("is_correct"));
                    stat.setSelectionCount(rs.getInt("selection_count"));
                    stat.setSelectionPercentage(rs.getDouble("selection_percentage"));
                    stats.add(stat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Delete test answer
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM test_answers WHERE id = ?";

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
     * Delete all answers for an attempt
     */
    public boolean deleteByAttemptId(int attemptId) {
        String sql = "DELETE FROM test_answers WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            return stmt.executeUpdate() >= 0; // Allow 0 deletions (empty attempt)
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Map ResultSet to TestAnswer object
     */
    private TestAnswer mapResultSetToTestAnswer(ResultSet rs) throws SQLException {
        TestAnswer answer = new TestAnswer();
        answer.setId(rs.getInt("id"));
        answer.setAttemptId(rs.getInt("attempt_id"));
        answer.setQuestionId(rs.getInt("question_id"));
        answer.setSelectedOptionId(rs.getInt("selected_option_id"));
        answer.setCorrect(rs.getBoolean("is_correct"));
        answer.setPointsEarned(rs.getDouble("points_earned"));
        
        Timestamp answeredAt = rs.getTimestamp("answered_at");
        if (answeredAt != null) {
            answer.setAnsweredAt(answeredAt.toLocalDateTime());
        }
        
        return answer;
    }

    /**
     * Inner class for question statistics
     */
    public static class QuestionStatistics {
        private int questionId;
        private String questionText;
        private int orderIndex;
        private double points;
        private int totalAnswers;
        private int correctAnswers;
        private double correctPercentage;

        // Getters and setters
        public int getQuestionId() { return questionId; }
        public void setQuestionId(int questionId) { this.questionId = questionId; }
        
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        
        public int getOrderIndex() { return orderIndex; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
        
        public double getPoints() { return points; }
        public void setPoints(double points) { this.points = points; }
        
        public int getTotalAnswers() { return totalAnswers; }
        public void setTotalAnswers(int totalAnswers) { this.totalAnswers = totalAnswers; }
        
        public int getCorrectAnswers() { return correctAnswers; }
        public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
        
        public double getCorrectPercentage() { return correctPercentage; }
        public void setCorrectPercentage(double correctPercentage) { this.correctPercentage = correctPercentage; }
    }

    /**
     * Inner class for option statistics
     */
    public static class OptionStatistics {
        private int optionId;
        private String optionText;
        private String optionLetter;
        private boolean isCorrect;
        private int selectionCount;
        private double selectionPercentage;

        // Getters and setters
        public int getOptionId() { return optionId; }
        public void setOptionId(int optionId) { this.optionId = optionId; }
        
        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
        
        public String getOptionLetter() { return optionLetter; }
        public void setOptionLetter(String optionLetter) { this.optionLetter = optionLetter; }
        
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
        
        public int getSelectionCount() { return selectionCount; }
        public void setSelectionCount(int selectionCount) { this.selectionCount = selectionCount; }
        
        public double getSelectionPercentage() { return selectionPercentage; }
        public void setSelectionPercentage(double selectionPercentage) { this.selectionPercentage = selectionPercentage; }
    }
}