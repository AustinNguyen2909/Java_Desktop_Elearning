package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Model class representing a course test
 * One test per course - students must pass to earn certificate
 */
public class CourseTest {
    private Integer id;
    private Integer courseId;
    private String title;
    private String description;
    private Double passingScore;         // Percentage (0-100), default 80.0
    private Integer timeLimitMinutes;    // NULL = no time limit
    private Boolean shuffleQuestions;    // Randomize question order
    private Boolean shuffleOptions;      // Randomize answer options
    private Integer maxAttempts;         // NULL = unlimited attempts
    private Boolean isPublished;         // Only published tests are visible to students
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields (not in database, calculated)
    private Integer questionCount;
    private Integer totalPoints;

    // Constructors
    public CourseTest() {
        this.passingScore = 80.0;
        this.shuffleQuestions = false;
        this.shuffleOptions = false;
        this.isPublished = false;
    }

    public CourseTest(Integer courseId, String title, String description) {
        this();
        this.courseId = courseId;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Double passingScore) {
        this.passingScore = passingScore;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public Boolean getShuffleQuestions() {
        return shuffleQuestions;
    }

    public void setShuffleQuestions(Boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    public Boolean getShuffleOptions() {
        return shuffleOptions;
    }

    public void setShuffleOptions(Boolean shuffleOptions) {
        this.shuffleOptions = shuffleOptions;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
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

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    @Override
    public String toString() {
        return "CourseTest{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", title='" + title + '\'' +
                ", passingScore=" + passingScore +
                ", isPublished=" + isPublished +
                ", questionCount=" + questionCount +
                '}';
    }
}
