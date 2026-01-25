package com.elearning.model;

import java.time.LocalDateTime;

/**
 * LessonProgress entity - tracks individual lesson completion for users
 */
public class LessonProgress {
    private Integer id;
    private Integer userId;
    private Integer lessonId;
    private boolean isCompleted;
    private LocalDateTime completedAt;
    private LocalDateTime lastOpenedAt;

    // Additional fields for display
    private String lessonTitle;
    private String userName;

    // Constructors
    public LessonProgress() {
    }

    public LessonProgress(Integer userId, Integer lessonId) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.isCompleted = false;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getLastOpenedAt() {
        return lastOpenedAt;
    }

    public void setLastOpenedAt(LocalDateTime lastOpenedAt) {
        this.lastOpenedAt = lastOpenedAt;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "LessonProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", lessonId=" + lessonId +
                ", isCompleted=" + isCompleted +
                ", lastOpenedAt=" + lastOpenedAt +
                '}';
    }
}
