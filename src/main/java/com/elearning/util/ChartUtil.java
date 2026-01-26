package com.elearning.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for creating charts and visualizations using JFreeChart
 */
public class ChartUtil {

    /**
     * Create a rating distribution bar chart
     */
    public static ChartPanel createRatingDistributionChart(int[] ratingDistribution) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < ratingDistribution.length; i++) {
            int stars = i + 1;
            dataset.addValue(ratingDistribution[i], "Reviews", stars + " ★");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Rating Distribution",
                "Rating",
                "Number of Reviews",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    /**
     * Create a course status pie chart
     */
    public static ChartPanel createCourseStatusPieChart(int approved, int pending, int rejected) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (approved > 0) dataset.setValue("Approved", approved);
        if (pending > 0) dataset.setValue("Pending", pending);
        if (rejected > 0) dataset.setValue("Rejected", rejected);

        JFreeChart chart = ChartFactory.createPieChart(
                "Course Status Distribution",
                dataset,
                true,
                true,
                false
        );

        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Approved", new Color(46, 204, 113));
        plot.setSectionPaint("Pending", new Color(241, 196, 15));
        plot.setSectionPaint("Rejected", new Color(231, 76, 60));
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    /**
     * Create an enrollment status pie chart
     */
    public static ChartPanel createEnrollmentStatusPieChart(int active, int completed) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (active > 0) dataset.setValue("In Progress", active);
        if (completed > 0) dataset.setValue("Completed", completed);

        JFreeChart chart = ChartFactory.createPieChart(
                "Enrollment Status",
                dataset,
                true,
                true,
                false
        );

        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("In Progress", new Color(52, 152, 219));
        plot.setSectionPaint("Completed", new Color(46, 204, 113));
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    /**
     * Create a user role distribution pie chart
     */
    public static ChartPanel createUserRolePieChart(int students, int instructors, int admins) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (students > 0) dataset.setValue("Students", students);
        if (instructors > 0) dataset.setValue("Instructors", instructors);
        if (admins > 0) dataset.setValue("Admins", admins);

        JFreeChart chart = ChartFactory.createPieChart(
                "User Distribution",
                dataset,
                true,
                true,
                false
        );

        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Students", new Color(52, 152, 219));
        plot.setSectionPaint("Instructors", new Color(155, 89, 182));
        plot.setSectionPaint("Admins", new Color(231, 76, 60));
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    /**
     * Create a top courses bar chart
     */
    public static ChartPanel createTopCoursesChart(java.util.List<String> courseTitles,
                                                   java.util.List<Integer> enrollmentCounts,
                                                   int limit) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int count = Math.min(limit, Math.min(courseTitles.size(), enrollmentCounts.size()));
        for (int i = 0; i < count; i++) {
            String title = courseTitles.get(i);
            if (title.length() > 20) {
                title = title.substring(0, 17) + "...";
            }
            dataset.addValue(enrollmentCounts.get(i), "Enrollments", title);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top Courses by Enrollment",
                "Course",
                "Enrollments",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        return chartPanel;
    }

    /**
     * Create an instructor performance chart
     */
    public static ChartPanel createInstructorPerformanceChart(int totalCourses,
                                                               int approvedCourses,
                                                               int publishedCourses) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(totalCourses, "Courses", "Total");
        dataset.addValue(approvedCourses, "Courses", "Approved");
        dataset.addValue(publishedCourses, "Courses", "Published");

        JFreeChart chart = ChartFactory.createBarChart(
                "Course Status Overview",
                "Status",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }
}
