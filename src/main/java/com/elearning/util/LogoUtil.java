package com.elearning.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Utility to render an HPSL-style logo with gradient text.
 */
public final class LogoUtil {
    private LogoUtil() {
    }

    public static BufferedImage renderLogo(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String brand = "HPSL";
        String tagline = "HIGH PERFORMANCE SELF-LEARNING";

        Font brandFont = new Font("Segoe UI Black", Font.BOLD, Math.max(36, height / 2));
        Font taglineFont = new Font("Segoe UI", Font.PLAIN, Math.max(10, height / 8));

        FontMetrics brandMetrics = g2.getFontMetrics(brandFont);
        int brandWidth = brandMetrics.stringWidth(brand);
        int brandX = (width - brandWidth) / 2;
        int brandY = height / 2 + brandMetrics.getAscent() / 2 - 6;

        // Shadow
        g2.setFont(brandFont);
        g2.setColor(new Color(10, 35, 55, 120));
        g2.drawString(brand, brandX + 2, brandY + 2);

        // Gradient text
        g2.setPaint(new GradientPaint(0, 0, new Color(22, 178, 204),
                width, height, new Color(74, 220, 242)));
        g2.drawString(brand, brandX, brandY);

        // Outline
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(9, 132, 163));
        g2.drawString(brand, brandX, brandY);

        // Tagline
        g2.setFont(taglineFont);
        FontMetrics taglineMetrics = g2.getFontMetrics(taglineFont);
        int taglineWidth = taglineMetrics.stringWidth(tagline);
        int taglineX = (width - taglineWidth) / 2;
        int taglineY = brandY + taglineMetrics.getAscent() + 8;
        g2.setColor(new Color(29, 37, 59));
        g2.drawString(tagline, taglineX, taglineY);

        g2.dispose();
        return image;
    }
}
