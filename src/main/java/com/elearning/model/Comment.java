package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Comment entity - represents user comments on lessons
 */
public class Comment {
    private Integer id;
    private Integer userId;
    private Integer lessonId;
    private Integer parentId;
    private String content;
    private boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields
    private String userName;
    private String userAvatar;

    // Constructors
    public Comment() {
    }

    public Comment(Integer userId, Integer lessonId, String content) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.content = content;
        this.isEdited = false;
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
