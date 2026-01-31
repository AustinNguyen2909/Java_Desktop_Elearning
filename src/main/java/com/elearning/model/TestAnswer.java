package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Model class representing a student's answer to a specific question in a test attempt
 * One record per question per attempt
 */
public class TestAnswer {
    private Integer id;
    private Integer attemptId;
    private Integer questionId;
    private Integer selectedOptionId;   // Which option the user selected
    private Boolean isCorrect;          // Was the selected option correct?
    private Double pointsEarned;        // Points awarded (0 or question.points)
    private LocalDateTime answeredAt;

    // Transient fields (for display)
    private String questionText;
    private Double questionPoints;
    private String selectedOptionText;
    private String selectedOptionLetter;
    private String correctOptionText;
    private String correctOptionLetter;

    // Constructors
    public TestAnswer() {
        this.isCorrect = false;
        this.pointsEarned = 0.0;
    }

    public TestAnswer(Integer attemptId, Integer questionId, Integer selectedOptionId) {
        this();
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public boolean isCorrect() {
        return isCorrect != null && isCorrect;
    }

    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
    }

    public Double getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Double getQuestionPoints() {
        return questionPoints;
    }

    public void setQuestionPoints(Double questionPoints) {
        this.questionPoints = questionPoints;
    }

    public String getSelectedOptionText() {
        return selectedOptionText;
    }

    public void setSelectedOptionText(String selectedOptionText) {
        this.selectedOptionText = selectedOptionText;
    }

    public String getSelectedOptionLetter() {
        return selectedOptionLetter;
    }

    public void setSelectedOptionLetter(String selectedOptionLetter) {
        this.selectedOptionLetter = selectedOptionLetter;
    }

    public String getCorrectOptionText() {
        return correctOptionText;
    }

    public void setCorrectOptionText(String correctOptionText) {
        this.correctOptionText = correctOptionText;
    }

    public String getCorrectOptionLetter() {
        return correctOptionLetter;
    }

    public void setCorrectOptionLetter(String correctOptionLetter) {
        this.correctOptionLetter = correctOptionLetter;
    }

    @Override
    public String toString() {
        return "TestAnswer{" +
                "id=" + id +
                ", attemptId=" + attemptId +
                ", questionId=" + questionId +
                ", selectedOptionId=" + selectedOptionId +
                ", isCorrect=" + isCorrect +
                ", pointsEarned=" + pointsEarned +
                '}';
    }
}
