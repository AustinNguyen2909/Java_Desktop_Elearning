package com.elearning.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a test question
 * Each question has exactly 4 answer options (A, B, C, D)
 */
public class TestQuestion {
    private Integer id;
    private Integer testId;
    private String questionText;
    private Integer orderIndex;         // For ordering questions
    private Double points;              // Points awarded for correct answer, default 1.0
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient field - list of answer options (loaded separately)
    private List<AnswerOption> options;

    // Constructors
    public TestQuestion() {
        this.orderIndex = 0;
        this.points = 1.0;
        this.options = new ArrayList<>();
    }

    public TestQuestion(Integer testId, String questionText) {
        this();
        this.testId = testId;
        this.questionText = questionText;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
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

    public List<AnswerOption> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOption> options) {
        this.options = options;
    }

    public void addOption(AnswerOption option) {
        this.options.add(option);
    }

    /**
     * Get the correct answer option
     */
    public AnswerOption getCorrectOption() {
        return options.stream()
                .filter(AnswerOption::getIsCorrect)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if question has exactly 4 options
     */
    public boolean hasValidOptionCount() {
        return options != null && options.size() == 4;
    }

    /**
     * Check if question has exactly 1 correct option
     */
    public boolean hasValidCorrectOption() {
        if (options == null) return false;
        long correctCount = options.stream().filter(AnswerOption::getIsCorrect).count();
        return correctCount == 1;
    }

    @Override
    public String toString() {
        return "TestQuestion{" +
                "id=" + id +
                ", testId=" + testId +
                ", questionText='" + questionText + '\'' +
                ", orderIndex=" + orderIndex +
                ", points=" + points +
                ", optionsCount=" + (options != null ? options.size() : 0) +
                '}';
    }
}
