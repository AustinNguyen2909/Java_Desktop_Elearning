package com.elearning.service;

import com.elearning.dao.LoginLogDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

/**
 * Service for user login logging.
 */
public class LoginLogService {
    private final LoginLogDAO loginLogDAO;

    private LoginLogService() {
        this.loginLogDAO = new LoginLogDAO();
    }

    private static class SingletonHolder {
        private static final LoginLogService INSTANCE = new LoginLogService();
    }

    public static LoginLogService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void recordLogin(int userId) {
        loginLogDAO.insertLogin(userId, LocalDateTime.now());
    }

    public Set<LocalDate> getLoginDatesForMonth(int userId, YearMonth month) {
        return loginLogDAO.findLoginDatesForMonth(userId, month);
    }
}
