package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Enrollment entity - represents user enrollment in a course
 */
public class Enrollment {
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private LocalDateTime enrolledAt;
    private double progressPercent;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt;

    // Additional fields
    private String courseTitle;
    private String courseThumbnail;
    private String userName;

    // Constructors
    public Enrollment() {
    }

    public Enrollment(Integer userId, Integer courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.progressPercent = 0.0;
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

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseThumbnail() {
        return courseThumbnail;
    }

    public void setCourseThumbnail(String courseThumbnail) {
        this.courseThumbnail = courseThumbnail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isCompleted() {
        return completedAt != null || progressPercent >= 100.0;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", progressPercent=" + progressPercent +
                '}';
    }
}
