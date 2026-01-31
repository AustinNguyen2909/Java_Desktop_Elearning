package com.elearning.util;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility to render a certificate image as PNG.
 */
public class CertificateImageUtil {
    private CertificateImageUtil() {
    }

    public static BufferedImage renderCertificate(String studentName, String courseTitle, String code, LocalDate issuedDate) {
        int width = 1200;
        int height = 800;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setPaint(new GradientPaint(0, 0, new Color(225, 246, 250), 0, height, Color.WHITE));
        g2.fillRect(0, 0, width, height);

        // Subtle pattern
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(228, 240, 244));
        for (int i = -height; i < width; i += 28) {
            g2.drawLine(i, 0, i + height, height);
        }

        // Soft vignette
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 200), 0, height,
                new Color(210, 240, 245, 120)));
        g2.fillRect(0, 0, width, height);

        // Header band
        g2.setPaint(new GradientPaint(0, 0, new Color(8, 145, 178), width, 0, new Color(34, 211, 238)));
        g2.fillRoundRect(60, 60, width - 120, 80, 18, 18);

        // Frame
        g2.setColor(new Color(8, 145, 178));
        g2.setStroke(new BasicStroke(6f));
        g2.draw(new RoundRectangle2D.Double(40, 40, width - 80, height - 80, 24, 24));

        // Inner frame
        g2.setColor(new Color(34, 211, 238));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(60, 60, width - 120, height - 120, 18, 18));

        // Corner ornaments
        g2.setColor(new Color(8, 145, 178));
        g2.setStroke(new BasicStroke(3f));
        g2.drawArc(70, 70, 80, 80, 180, 90);
        g2.drawArc(width - 150, 70, 80, 80, 270, 90);
        g2.drawArc(70, height - 150, 80, 80, 90, 90);
        g2.drawArc(width - 150, height - 150, 80, 80, 0, 90);

        // Center seal
        int sealSize = 110;
        int sealX = (width - sealSize) / 2;
        int sealY = 150;
        g2.setColor(new Color(245, 158, 11));
        g2.fillOval(sealX, sealY, sealSize, sealSize);
        g2.setColor(new Color(217, 119, 6));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(sealX + 6, sealY + 6, sealSize - 12, sealSize - 12);
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        drawCenteredString(g2, "CERTIFIED", new Rectangle(sealX, sealY, sealSize, sealSize), g2.getFont());

        // Ribbon
        g2.setColor(new Color(14, 116, 144));
        g2.fillRoundRect(sealX - 90, sealY + sealSize + 8, sealSize + 180, 28, 14, 14);
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        drawCenteredString(g2, "EXCELLENCE", new Rectangle(sealX - 90, sealY + sealSize + 8, sealSize + 180, 28), g2.getFont());

        // Left header
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 40));
        g2.drawString("E-Learning", 90, 105);
        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.drawString("Course Certificate", 92, 128);

        // Certificate title (centered)
        g2.setColor(new Color(22, 78, 99));
        g2.setFont(new Font("Serif", Font.BOLD, 36));
        drawCenteredString(g2, "Certificate of Completion", new Rectangle(80, 290, width - 160, 50), g2.getFont());

        // Body text
        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(new Color(71, 85, 105));
        drawCenteredString(g2, "This certifies that", new Rectangle(80, 350, width - 160, 30), g2.getFont());

        g2.setFont(new Font("Serif", Font.BOLD, 28));
        g2.setColor(new Color(22, 78, 99));
        drawCenteredString(g2, studentName, new Rectangle(80, 380, width - 160, 36), g2.getFont());

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(new Color(71, 85, 105));
        drawCenteredString(g2, "has successfully completed the course", new Rectangle(80, 420, width - 160, 28), g2.getFont());

        g2.setFont(new Font("Serif", Font.BOLD, 24));
        g2.setColor(new Color(8, 145, 178));
        drawCenteredString(g2, courseTitle, new Rectangle(80, 455, width - 160, 36), g2.getFont());

        // Footer band
        g2.setColor(new Color(226, 232, 240));
        g2.fillRoundRect(180, 520, width - 360, 90, 16, 16);

        // Footer text
        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        g2.setColor(new Color(71, 85, 105));
        String dateText = issuedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        drawCenteredString(g2, "Issued: " + dateText, new Rectangle(180, 545, width - 360, 22), g2.getFont());
        drawCenteredString(g2, "Certificate Code: " + code, new Rectangle(180, 570, width - 360, 22), g2.getFont());

        // Signature line
        g2.setColor(new Color(8, 145, 178));
        g2.drawLine(width / 2 - 140, 670, width / 2 + 140, 670);
        g2.setFont(new Font("Serif", Font.PLAIN, 14));
        g2.setColor(new Color(71, 85, 105));
        drawCenteredString(g2, "E-Learning Platform", new Rectangle(80, 675, width - 160, 24), g2.getFont());

        g2.dispose();
        return image;
    }

    private static void drawCenteredString(Graphics2D g2, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g2.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(text, x, y);
    }
}
