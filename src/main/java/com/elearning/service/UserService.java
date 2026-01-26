package com.elearning.service;

import com.elearning.dao.UserDAO;
import com.elearning.model.User;
import com.elearning.util.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * Service layer for user management operations
 * Handles business logic and authorization
 */
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Get all users (Admin only)
     */
    public List<User> getAllUsers(String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can view all users");
        }
        return userDAO.findAll();
    }

    /**
     * Get users by role (Admin only)
     */
    public List<User> getUsersByRole(String role, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can filter users");
        }
        return userDAO.findByRole(role);
    }

    /**
     * Search users (Admin only)
     */
    public List<User> searchUsers(String keyword, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only admins can search users");
        }
        return userDAO.searchUsers(keyword);
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        return userDAO.findById(userId);
    }

    /**
     * Create new user (Admin only)
     */
    public boolean createUser(User user, String password, String adminRole) {
        if (!"ADMIN".equals(adminRole)) {
            throw new SecurityException("Only admins can create users");
        }

        // Validate inputs
        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!ValidationUtil.hasMinLength(password, 6)) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (ValidationUtil.isEmpty(user.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }

        // Check if username or email already exists
        if (userDAO.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userDAO.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        user.setPasswordHash(passwordHash);

        // Set default status if not provided
        if (user.getStatus() == null || user.getStatus().isEmpty()) {
            user.setStatus("ACTIVE");
        }

        return userDAO.insert(user);
    }

    /**
     * Update user (Admin only, or user updating own profile)
     */
    public boolean updateUser(User user, Integer currentUserId, String currentUserRole) {
        // Admin can update anyone, users can update themselves
        if (!"ADMIN".equals(currentUserRole) && !user.getId().equals(currentUserId)) {
            throw new SecurityException("You can only update your own profile");
        }

        // Validate inputs
        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (ValidationUtil.isEmpty(user.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }

        // Check if username is taken by another user
        User existingByUsername = userDAO.findByUsername(user.getUsername());
        if (existingByUsername != null && !existingByUsername.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email is taken by another user
        User existingByEmail = userDAO.findByEmail(user.getEmail());
        if (existingByEmail != null && !existingByEmail.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Non-admins cannot change their own role or status
        if (!"ADMIN".equals(currentUserRole)) {
            User currentUser = userDAO.findById(user.getId());
            user.setRole(currentUser.getRole());
            user.setStatus(currentUser.getStatus());
        }

        return userDAO.update(user);
    }

    /**
     * Update user password (Admin or self)
     */
    public boolean updatePassword(int userId, String newPassword, Integer currentUserId, String currentUserRole) {
        // Admin can update anyone, users can update themselves
        if (!"ADMIN".equals(currentUserRole) && !Integer.valueOf(userId).equals(currentUserId)) {
            throw new SecurityException("You can only update your own password");
        }

        if (!ValidationUtil.hasMinLength(newPassword, 6)) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        String passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        user.setPasswordHash(passwordHash);

        return userDAO.update(user);
    }

    /**
     * Update user status (Admin only)
     */
    public boolean updateUserStatus(int userId, String status, String adminRole) {
        if (!"ADMIN".equals(adminRole)) {
            throw new SecurityException("Only admins can change user status");
        }

        // Validate status
        if (!status.equals("ACTIVE") && !status.equals("SUSPENDED") && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid status. Must be ACTIVE, SUSPENDED, or PENDING");
        }

        return userDAO.updateStatus(userId, status);
    }

    /**
     * Delete user (soft delete - Admin only)
     */
    public boolean deleteUser(int userId, String adminRole) {
        if (!"ADMIN".equals(adminRole)) {
            throw new SecurityException("Only admins can delete users");
        }

        // Cannot delete yourself
        // This check would require current user ID - adding parameter would be better
        // For now, just perform the soft delete
        return userDAO.delete(userId);
    }

    /**
     * Activate user (Admin only)
     */
    public boolean activateUser(int userId, String adminRole) {
        return updateUserStatus(userId, "ACTIVE", adminRole);
    }

    /**
     * Suspend user (Admin only)
     */
    public boolean suspendUser(int userId, String adminRole) {
        return updateUserStatus(userId, "SUSPENDED", adminRole);
    }
}
