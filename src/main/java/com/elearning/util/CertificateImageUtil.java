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
        g2.setPaint(new GradientPaint(0, 0, new Color(236, 254, 255), 0, height, Color.WHITE));
        g2.fillRect(0, 0, width, height);

        // Frame
        g2.setColor(new Color(8, 145, 178));
        g2.setStroke(new BasicStroke(6f));
        g2.draw(new RoundRectangle2D.Double(40, 40, width - 80, height - 80, 24, 24));

        // Left header
        g2.setColor(new Color(8, 145, 178));
        g2.setFont(new Font("Serif", Font.BOLD, 42));
        g2.drawString("E-Learning", 90, 140);
        g2.setFont(new Font("Serif", Font.PLAIN, 22));
        g2.setColor(new Color(71, 85, 105));
        g2.drawString("Course Certificate", 92, 170);

        // Certificate title
        g2.setColor(new Color(22, 78, 99));
        g2.setFont(new Font("Serif", Font.BOLD, 36));
        g2.drawString("Certificate of Completion", 90, 250);

        // Body text
        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(new Color(71, 85, 105));
        g2.drawString("This certifies that", 90, 310);

        g2.setFont(new Font("Serif", Font.BOLD, 28));
        g2.setColor(new Color(22, 78, 99));
        g2.drawString(studentName, 90, 360);

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(new Color(71, 85, 105));
        g2.drawString("has successfully completed the course", 90, 400);

        g2.setFont(new Font("Serif", Font.BOLD, 24));
        g2.setColor(new Color(8, 145, 178));
        g2.drawString(courseTitle, 90, 440);

        // Footer
        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        g2.setColor(new Color(71, 85, 105));
        String dateText = issuedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        g2.drawString("Issued: " + dateText, 90, 520);
        g2.drawString("Certificate Code: " + code, 90, 550);

        // Signature line
        g2.setColor(new Color(8, 145, 178));
        g2.drawLine(90, 620, 380, 620);
        g2.setFont(new Font("Serif", Font.PLAIN, 14));
        g2.setColor(new Color(71, 85, 105));
        g2.drawString("E-Learning Platform", 90, 645);

        g2.dispose();
        return image;
    }
}
