package com.elearning.dao;

import com.elearning.model.CourseReviewComment;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for course review comments.
 */
public class CourseReviewCommentDAO {

    public List<CourseReviewComment> findTopLevelComments(int reviewId) {
        List<CourseReviewComment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                "FROM course_review_comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.review_id = ? AND c.parent_id IS NULL " +
                "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<CourseReviewComment> findReplies(int parentId) {
        List<CourseReviewComment> replies = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                "FROM course_review_comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.parent_id = ? " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                replies.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }

    public CourseReviewComment findById(int id) {
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                "FROM course_review_comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(CourseReviewComment comment) {
        String sql = "INSERT INTO course_review_comments (review_id, user_id, parent_id, content, is_edited) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, comment.getReviewId());
            stmt.setInt(2, comment.getUserId());
            if (comment.getParentId() != null) {
                stmt.setInt(3, comment.getParentId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, comment.getContent());
            stmt.setBoolean(5, comment.isEdited());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private CourseReviewComment mapResultSet(ResultSet rs) throws SQLException {
        CourseReviewComment comment = new CourseReviewComment();
        comment.setId(rs.getInt("id"));
        comment.setReviewId(rs.getInt("review_id"));
        comment.setUserId(rs.getInt("user_id"));
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            comment.setParentId(parentId);
        }
        comment.setContent(rs.getString("content"));
        comment.setEdited(rs.getBoolean("is_edited"));
        comment.setUserName(rs.getString("user_name"));
        comment.setUserAvatar(rs.getString("user_avatar"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            comment.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            comment.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return comment;
    }
}
