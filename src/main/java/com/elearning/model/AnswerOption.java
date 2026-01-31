package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Model class representing an answer option for a test question
 * Each question has exactly 4 options: A, B, C, D
 * Exactly one option must be marked as correct
 */
public class AnswerOption {
    private Integer id;
    private Integer questionId;
    private String optionText;
    private Boolean isCorrect;          // Only 1 should be true per question
    private String optionLetter;        // A, B, C, or D
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AnswerOption() {
        this.isCorrect = false;
    }

    public AnswerOption(Integer questionId, String optionText, Boolean isCorrect, String optionLetter) {
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.optionLetter = optionLetter;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getOptionLetter() {
        return optionLetter;
    }

    public void setOptionLetter(String optionLetter) {
        this.optionLetter = optionLetter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Validate that option letter is A, B, C, or D
     */
    public boolean isValidOptionLetter() {
        return optionLetter != null &&
               (optionLetter.equals("A") || optionLetter.equals("B") ||
                optionLetter.equals("C") || optionLetter.equals("D"));
    }

    @Override
    public String toString() {
        return "AnswerOption{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", optionLetter='" + optionLetter + '\'' +
                ", optionText='" + optionText + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
