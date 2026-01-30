package com.elearning.dao;

import com.elearning.model.Certificate;
import com.elearning.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for certificates.
 */
public class CertificateDAO {

    public Certificate findByUserAndCourse(int userId, int courseId) {
        String sql = "SELECT c.*, co.title as course_title " +
                "FROM certificates c " +
                "JOIN courses co ON c.course_id = co.id " +
                "WHERE c.user_id = ? AND c.course_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Certificate> findByUserId(int userId) {
        List<Certificate> list = new ArrayList<>();
        String sql = "SELECT c.*, co.title as course_title " +
                "FROM certificates c " +
                "JOIN courses co ON c.course_id = co.id " +
                "WHERE c.user_id = ? " +
                "ORDER BY c.issued_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Certificate certificate) {
        String sql = "INSERT INTO certificates (user_id, course_id, certificate_code, issued_at, file_path) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, certificate.getUserId());
            stmt.setInt(2, certificate.getCourseId());
            stmt.setString(3, certificate.getCertificateCode());
            stmt.setTimestamp(4, Timestamp.valueOf(certificate.getIssuedAt()));
            stmt.setString(5, certificate.getFilePath());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    certificate.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFilePath(int certificateId, String filePath) {
        String sql = "UPDATE certificates SET file_path = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filePath);
            stmt.setInt(2, certificateId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Certificate mapResultSet(ResultSet rs) throws SQLException {
        Certificate cert = new Certificate();
        cert.setId(rs.getInt("id"));
        cert.setUserId(rs.getInt("user_id"));
        cert.setCourseId(rs.getInt("course_id"));
        cert.setCertificateCode(rs.getString("certificate_code"));
        cert.setFilePath(rs.getString("file_path"));
        cert.setCourseTitle(rs.getString("course_title"));

        Timestamp issuedAt = rs.getTimestamp("issued_at");
        if (issuedAt != null) {
            cert.setIssuedAt(issuedAt.toLocalDateTime());
        }
        return cert;
    }
}
