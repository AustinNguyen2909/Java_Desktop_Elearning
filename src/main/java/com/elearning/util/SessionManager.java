package com.elearning.util;

import com.elearning.model.User;

/**
 * Singleton class for managing user session
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    /**
     * Get singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Login user
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /**
     * Logout current user
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Get current logged in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equals(role);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is teacher
     */
    public boolean isTeacher() {
        return hasRole("TEACHER");
    }

    /**
     * Check if current user is student
     */
    public boolean isUser() {
        return hasRole("USER");
    }

    /**
     * Get current user ID
     */
    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
}
