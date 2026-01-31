package com.elearning.dao;

import com.elearning.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DAO for user login logs.
 */
public class LoginLogDAO {

    /**
     * Insert a login log entry.
     */
    public boolean insertLogin(int userId, LocalDateTime loginAt) {
        ensureTableExists();
        String sql = "INSERT INTO user_login_logs (user_id, login_at) VALUES (?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(loginAt));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get distinct login dates for a user within a given month.
     */
    public Set<LocalDate> findLoginDatesForMonth(int userId, YearMonth month) {
        ensureTableExists();
        Set<LocalDate> dates = new LinkedHashSet<>();
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        String sql = "SELECT DISTINCT DATE(login_at) AS login_date " +
                "FROM user_login_logs " +
                "WHERE user_id = ? AND login_at >= ? AND login_at < ? " +
                "ORDER BY login_date ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                java.sql.Date date = rs.getDate("login_date");
                if (date != null) {
                    dates.add(date.toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    private void ensureTableExists() {
        String ddl = "CREATE TABLE IF NOT EXISTS user_login_logs (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id INT NOT NULL, " +
                "login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_login_user (user_id), " +
                "INDEX idx_login_at (login_at), " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
