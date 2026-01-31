package com.elearning.model;

import java.time.LocalDateTime;

/**
 * Model class representing a certificate issued to a student who passed a course test
 * One certificate per user per course
 */
public class Certificate {
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private Integer testId;             // For test-based certificates
    private Integer attemptId;          // The passing attempt that earned the certificate

    // Certificate details
    private String certificateCode;     // Unique verification code (e.g., CERT-2024-001-0123-A1B2C3)
    private String studentName;         // Denormalized for display
    private String courseTitle;         // Denormalized for display
    private String instructorName;      // Denormalized for display
    private Double scoreAchieved;       // Score percentage (for test-based certificates)

    // Legacy support for course completion certificates
    private String filePath;            // Path to certificate image file

    // Dates
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;    // NULL = never expires

    // Status
    private Boolean isRevoked;          // Can be revoked by admin
    private LocalDateTime revokedAt;
    private String revokedReason;

    // Constructors
    public Certificate() {
        this.isRevoked = false;
    }

    public Certificate(Integer userId, Integer courseId, Integer testId, Integer attemptId) {
        this();
        this.userId = userId;
        this.courseId = courseId;
        this.testId = testId;
        this.attemptId = attemptId;
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

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public String getCertificateCode() {
        return certificateCode;
    }

    public void setCertificateCode(String certificateCode) {
        this.certificateCode = certificateCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public Double getScoreAchieved() {
        return scoreAchieved;
    }

    public void setScoreAchieved(Double scoreAchieved) {
        this.scoreAchieved = scoreAchieved;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Boolean isRevoked) {
        this.isRevoked = isRevoked;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Check if certificate is valid (not revoked and not expired)
     */
    public boolean isValid() {
        if (isRevoked) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    /**
     * Check if certificate is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "id=" + id +
                ", certificateCode='" + certificateCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", courseTitle='" + courseTitle + '\'' +
                ", scoreAchieved=" + scoreAchieved +
                ", isRevoked=" + isRevoked +
                '}';
    }
}
