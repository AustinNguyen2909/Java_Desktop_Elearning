package com.elearning.service;

import com.elearning.dao.UserDAO;
import com.elearning.model.User;
import com.elearning.util.PasswordUtil;

/**
 * Service for authentication and user registration
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user with username and password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String username, String password) {
        try {
            User user = userDAO.findByUsername(username);
            if (user != null && user.isActive()) {
                if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register new user
     * @return true if registration successful, false otherwise
     */
    public boolean register(User user, String password) {
        try {
            // Check if username already exists
            if (userDAO.findByUsername(user.getUsername()) != null) {
                return false;
            }

            // Check if email already exists
            if (userDAO.findByEmail(user.getEmail()) != null) {
                return false;
            }

            // Hash password
            String passwordHash = PasswordUtil.hashPassword(password);
            user.setPasswordHash(passwordHash);
            user.setStatus("ACTIVE");  // Set user status to ACTIVE upon registration

            // Insert user
            return userDAO.insert(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change user password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            User user = userDAO.findById(userId);
            if (user != null && PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                String newPasswordHash = PasswordUtil.hashPassword(newPassword);
                user.setPasswordHash(newPasswordHash);
                return userDAO.update(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
