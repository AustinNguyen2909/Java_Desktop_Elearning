package com.elearning.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;

/**
 * Custom star rating renderer (supports half ratings).
 */
public class StarRatingPanel extends JPanel {
    private static final int STAR_COUNT = 5;
    private static final Color STAR_FILLED = new Color(245, 158, 11);
    private static final Color STAR_EMPTY = new Color(229, 231, 235);
    private static final Color TEXT_COLOR = new Color(17, 24, 39);

    private double rating;
    private final int starSize;
    private final int gap;
    private final Font textFont;

    public StarRatingPanel(double rating) {
        this(rating, 16, 4);
    }

    public StarRatingPanel(double rating, int starSize, int gap) {
        this.rating = rating;
        this.starSize = starSize;
        this.gap = gap;
        this.textFont = new Font("Segoe UI", Font.BOLD, 12);
        setOpaque(false);
    }

    public void setRating(double rating) {
        this.rating = rating;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        String text = String.format("%.1f", rating);
        FontMetrics fm = getFontMetrics(textFont);
        int textWidth = fm.stringWidth(text);
        int width = STAR_COUNT * starSize + (STAR_COUNT - 1) * gap + 8 + textWidth;
        int height = Math.max(starSize, fm.getHeight());
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double clamped = Math.max(0, Math.min(5, rating));
        int full = (int) Math.floor(clamped);
        double remainder = clamped - full;

        int x = 0;
        int y = 0;
        for (int i = 0; i < STAR_COUNT; i++) {
            Shape star = createStar(x + starSize / 2.0, y + starSize / 2.0, starSize / 2.0, starSize / 2.6);
            g2.setColor(STAR_EMPTY);
            g2.fill(star);

            double fill = 0.0;
            if (i < full) {
                fill = 1.0;
            } else if (i == full && remainder >= 0.25) {
                fill = remainder >= 0.75 ? 1.0 : 0.5;
            }

            if (fill > 0) {
                g2.setColor(STAR_FILLED);
                int clipWidth = (int) Math.round(starSize * fill);
                g2.setClip(x, y, clipWidth, starSize);
                g2.fill(star);
                g2.setClip(null);
            }

            x += starSize + gap;
        }

        g2.setFont(textFont);
        g2.setColor(TEXT_COLOR);
        String text = String.format("%.1f", rating);
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + 4;
        int textY = y + (starSize + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    private Shape createStar(double centerX, double centerY, double outerRadius, double innerRadius) {
        Path2D path = new Path2D.Double();
        double angle = -Math.PI / 2;
        double step = Math.PI / 5;
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + Math.cos(angle) * r;
            double y = centerY + Math.sin(angle) * r;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            angle += step;
        }
        path.closePath();
        return path;
    }
}
