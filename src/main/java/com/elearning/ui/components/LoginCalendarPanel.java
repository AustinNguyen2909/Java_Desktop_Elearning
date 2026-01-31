package com.elearning.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

/**
 * Calendar panel that highlights login days.
 */
public class LoginCalendarPanel extends JPanel {
    private static final Color LOGIN_BG = new Color(209, 250, 229);
    private static final Color LOGIN_TEXT = new Color(5, 150, 105);
    private static final String CHECK_MARK = "\u2713";

    private YearMonth month;
    private Set<LocalDate> loginDates = new HashSet<>();

    private final JPanel headerRow;
    private final JPanel gridPanel;

    public LoginCalendarPanel() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        headerRow = new JPanel(new GridLayout(1, 7, 6, 6));
        headerRow.setOpaque(false);

        gridPanel = new JPanel(new GridLayout(6, 7, 6, 6));
        gridPanel.setOpaque(false);

        add(headerRow, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        buildHeader();
    }

    public void setMonth(YearMonth month) {
        this.month = month;
        rebuildCalendar();
    }

    public void setLoginDates(Set<LocalDate> loginDates) {
        this.loginDates = loginDates != null ? new HashSet<>(loginDates) : new HashSet<>();
        rebuildCalendar();
    }

    private void buildHeader() {
        headerRow.removeAll();
        for (String label : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
            JLabel dayLabel = new JLabel(label, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Fira Sans", Font.BOLD, 12));
            dayLabel.setForeground(UITheme.MUTED_TEXT);
            headerRow.add(dayLabel);
        }
    }

    private void rebuildCalendar() {
        if (month == null) {
            return;
        }
        gridPanel.removeAll();

        LocalDate firstDay = month.atDay(1);
        int daysInMonth = month.lengthOfMonth();
        int firstOffset = dayOfWeekIndex(firstDay.getDayOfWeek());

        for (int i = 0; i < firstOffset; i++) {
            gridPanel.add(createEmptyCell());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            boolean loggedIn = loginDates.contains(date);
            gridPanel.add(createDayCell(day, loggedIn));
        }

        int filled = firstOffset + daysInMonth;
        int totalCells = 42;
        for (int i = filled; i < totalCells; i++) {
            gridPanel.add(createEmptyCell());
        }

        revalidate();
        repaint();
    }

    private int dayOfWeekIndex(DayOfWeek dayOfWeek) {
        int value = dayOfWeek.getValue(); // Monday=1..Sunday=7
        return value % 7; // Sunday => 0
    }

    private JPanel createDayCell(int day, boolean loggedIn) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(true);
        cell.setBackground(loggedIn ? LOGIN_BG : UITheme.SURFACE);
        cell.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        JLabel dayLabel = new JLabel(String.valueOf(day));
        dayLabel.setFont(new Font("Fira Sans", Font.BOLD, 12));
        dayLabel.setForeground(UITheme.TEXT);
        dayLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 0));

        JLabel markLabel = new JLabel(loggedIn ? CHECK_MARK : "");
        markLabel.setHorizontalAlignment(SwingConstants.CENTER);
        markLabel.setFont(new Font("Fira Sans", Font.BOLD, 18));
        markLabel.setForeground(LOGIN_TEXT);

        cell.add(dayLabel, BorderLayout.NORTH);
        cell.add(markLabel, BorderLayout.CENTER);

        return cell;
    }

    private JPanel createEmptyCell() {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(true);
        cell.setBackground(UITheme.BACKGROUND);
        cell.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        return cell;
    }
}
