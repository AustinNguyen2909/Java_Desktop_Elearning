package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Model class representing a student's attempt at taking a test
 * Tracks all information about the attempt including score and status
 */
public class TestAttempt {
    private Integer id;
    private Integer testId;
    private Integer userId;
    private Integer courseId;

    // Attempt details
    private Integer attemptNumber;      // 1st, 2nd, 3rd attempt, etc.
    private Integer totalQuestions;
    private Double totalPoints;
    private Double earnedPoints;
    private Double scorePercentage;     // Calculated: (earned/total) * 100

    // Status and timing
    private String status;              // IN_PROGRESS, COMPLETED, ABANDONED
    private Boolean passed;             // True if score >= passing_score
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeSpentSeconds;

    // Transient fields (for display)
    private String userName;
    private String testTitle;

    // Constructors
    public TestAttempt() {
        this.attemptNumber = 1;
        this.earnedPoints = 0.0;
        this.scorePercentage = 0.0;
        this.status = "IN_PROGRESS";
        this.passed = false;
        this.timeSpentSeconds = 0;
    }

    public TestAttempt(Integer testId, Integer userId, Integer courseId) {
        this();
        this.testId = testId;
        this.userId = userId;
        this.courseId = courseId;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Double getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(Double earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public Double getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(Double scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public boolean isPassed() {
        return passed != null && passed;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    /**
     * Check if attempt is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    /**
     * Check if attempt is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Calculate score percentage from earned and total points
     */
    public void calculateScore() {
        if (totalPoints != null && totalPoints > 0) {
            this.scorePercentage = (earnedPoints / totalPoints) * 100.0;
        } else {
            this.scorePercentage = 0.0;
        }
    }

    @Override
    public String toString() {
        return "TestAttempt{" +
                "id=" + id +
                ", testId=" + testId +
                ", userId=" + userId +
                ", attemptNumber=" + attemptNumber +
                ", scorePercentage=" + scorePercentage +
                ", status='" + status + '\'' +
                ", passed=" + passed +
                '}';
    }
}
