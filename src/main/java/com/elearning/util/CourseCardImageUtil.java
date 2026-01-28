package com.elearning.util;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility for course card thumbnails with a graceful placeholder.
 */
public final class CourseCardImageUtil {
    private static final Color[][] PALETTES = new Color[][]{
            {new Color(47, 111, 235), new Color(99, 102, 241)},
            {new Color(34, 197, 94), new Color(16, 185, 129)},
            {new Color(59, 130, 246), new Color(14, 165, 233)},
            {new Color(124, 58, 237), new Color(139, 92, 246)},
            {new Color(245, 158, 11), new Color(251, 191, 36)}
    };

    private CourseCardImageUtil() {
    }

    public static ImageIcon loadCourseThumbnail(String path, String title, int width, int height) {
        ImageIcon icon = loadFromPath(path, width, height);
        if (icon != null) {
            return icon;
        }
        return createPlaceholder(title, width, height);
    }

    private static ImageIcon loadFromPath(String path, int width, int height) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static ImageIcon createPlaceholder(String title, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color[] palette = PALETTES[Math.abs((title != null ? title.hashCode() : 0)) % PALETTES.length];
        GradientPaint paint = new GradientPaint(0, 0, palette[0], width, height, palette[1]);
        g2.setPaint(paint);
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(-20, height - 90, 120, 120);
        g2.fillOval(width - 100, -30, 120, 120);

        String initials = getInitials(title);
        Font font = new Font("Segoe UI", Font.BOLD, Math.max(28, height / 4));
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getAscent();
        int x = (width - textWidth) / 2;
        int y = (height + textHeight) / 2 - 6;
        g2.setColor(Color.WHITE);
        g2.drawString(initials, x, y);

        g2.dispose();
        return new ImageIcon(image);
    }

    private static String getInitials(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "EL";
        }
        String[] parts = title.trim().split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase();
        String second = parts.length > 1 ? parts[1].substring(0, 1).toUpperCase() : "";
        String result = first + second;
        if (result.length() < 2 && parts[0].length() >= 2) {
            result = parts[0].substring(0, 2).toUpperCase();
        }
        return result;
    }
}
