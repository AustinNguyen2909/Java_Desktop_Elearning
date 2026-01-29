package com.elearning.util;

import com.elearning.service.AnalyticsService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

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
            dataset.addValue(ratingDistribution[i], "Reviews", stars + " \u2605");
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
        plot.setSectionPaint("Approved", new Color(34, 197, 94));
        plot.setSectionPaint("Pending", new Color(241, 196, 15));
        plot.setSectionPaint("Rejected", new Color(225, 29, 72));
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
        plot.setSectionPaint("In Progress", new Color(47, 111, 235));
        plot.setSectionPaint("Completed", new Color(34, 197, 94));
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    /**
     * Create a user role distribution pie chart (Students and Instructors only)
     */
    public static ChartPanel createUserRolePieChart(int students, int instructors) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (students > 0) dataset.setValue("Students", students);
        if (instructors > 0) dataset.setValue("Instructors", instructors);

        JFreeChart chart = ChartFactory.createPieChart(
                "User Distribution",
                dataset,
                true,
                true,
                false
        );

        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Students", new Color(47, 111, 235));
        plot.setSectionPaint("Instructors", new Color(155, 89, 182));
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

    /**
     * Create a line chart for user registration trends
     * Shows Students and Instructors as separate lines over time
     */
    public static ChartPanel createUserRegistrationTrendsChart(Map<String, AnalyticsService.UserRegistrationData> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add data for both Students and Instructors
        for (Map.Entry<String, AnalyticsService.UserRegistrationData> entry : data.entrySet()) {
            String date = entry.getKey();
            AnalyticsService.UserRegistrationData regData = entry.getValue();

            dataset.addValue(regData.getStudents(), "Students", date);
            dataset.addValue(regData.getInstructors(), "Instructors", date);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "New User Registrations",
                "Date",
                "Number of Users",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        chart.setBackgroundPaint(Color.WHITE);

        // Customize line renderer
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(47, 111, 235)); // Students - Blue
        renderer.setSeriesPaint(1, new Color(155, 89, 182)); // Instructors - Purple
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);

        // Rotate category labels if there are many dates
        if (data.size() > 10) {
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(850, 300));
        return chartPanel;
    }
}
