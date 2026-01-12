package com.elearning.dao;

import com.elearning.model.User;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO for User entity
 */
public class UserDAO {

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find user by ID
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert new user
     */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, email, full_name, avatar_path, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getFullName());
            stmt.setString(6, user.getAvatarPath());
            stmt.setBoolean(7, user.isActive());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update user
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ?, email = ?, " +
                     "full_name = ?, avatar_path = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getFullName());
            stmt.setString(6, user.getAvatarPath());
            stmt.setBoolean(7, user.isActive());
            stmt.setInt(8, user.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete user (soft delete)
     */
    public boolean delete(int id) {
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
}
