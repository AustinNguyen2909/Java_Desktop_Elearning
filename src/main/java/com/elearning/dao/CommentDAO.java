package com.elearning.dao;

import com.elearning.model.Comment;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Comment entity
 */
public class CommentDAO {

    /**
     * Find all comments for a lesson (with user info)
     */
    public List<Comment> findByLessonId(int lessonId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                     "FROM comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.lesson_id = ? " +
                     "ORDER BY c.created_at ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lessonId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * Find top-level comments (no parent) for a lesson
     */
    public List<Comment> findTopLevelComments(int lessonId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                     "FROM comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.lesson_id = ? AND c.parent_id IS NULL " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lessonId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * Find replies to a comment
     */
    public List<Comment> findReplies(int parentId) {
        List<Comment> replies = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                     "FROM comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.parent_id = ? " +
                     "ORDER BY c.created_at ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                replies.add(mapResultSetToComment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }

    /**
     * Find comment by ID
     */
    public Comment findById(int id) {
        String sql = "SELECT c.*, u.full_name as user_name, u.avatar_path as user_avatar " +
                     "FROM comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToComment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert new comment
     */
    public boolean insert(Comment comment) {
        String sql = "INSERT INTO comments (user_id, lesson_id, parent_id, content, is_edited) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getUserId());
            stmt.setInt(2, comment.getLessonId());

            if (comment.getParentId() != null) {
                stmt.setInt(3, comment.getParentId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, comment.getContent());
            stmt.setBoolean(5, comment.isEdited());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
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

    /**
     * Update comment
     */
    public boolean update(Comment comment) {
        String sql = "UPDATE comments SET content = ?, is_edited = TRUE, updated_at = NOW() " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete comment and its replies (cascade)
     */
    public boolean delete(int commentId) {
        // First delete all replies
        String deleteRepliesSql = "DELETE FROM comments WHERE parent_id = ?";
        String deleteCommentSql = "DELETE FROM comments WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement repliesStmt = conn.prepareStatement(deleteRepliesSql);
                 PreparedStatement commentStmt = conn.prepareStatement(deleteCommentSql)) {

                // Delete replies
                repliesStmt.setInt(1, commentId);
                repliesStmt.executeUpdate();

                // Delete comment
                commentStmt.setInt(1, commentId);
                int rowsAffected = commentStmt.executeUpdate();

                conn.commit();
                return rowsAffected > 0;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Count comments for a lesson
     */
    public int countByLessonId(int lessonId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE lesson_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lessonId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Count replies for a comment
     */
    public int countReplies(int commentId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE parent_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Map ResultSet to Comment object
     */
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setLessonId(rs.getInt("lesson_id"));

        Integer parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            comment.setParentId(parentId);
        }

        comment.setContent(rs.getString("content"));
        comment.setEdited(rs.getBoolean("is_edited"));

        // User info
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
