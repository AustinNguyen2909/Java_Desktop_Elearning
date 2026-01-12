package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Review entity - represents user reviews on courses
 */
public class Review {
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private int rating; // 1-5
    private String comment;
    private boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields
    private String userName;
    private String userAvatar;

    // Constructors
    public Review() {
    }

    public Review(Integer userId, Integer courseId, int rating, String comment) {
        this.userId = userId;
        this.courseId = courseId;
        this.rating = rating;
        this.comment = comment;
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

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        return "Review{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
