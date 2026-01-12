package com.elearning.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern card panel with shadow effect
 */
public class CardPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius;

    public CardPanel() {
        this(new Color(255, 255, 255), 10);
    }

    public CardPanel(Color backgroundColor, int cornerRadius) {
        this.backgroundColor = backgroundColor;
        this.cornerRadius = cornerRadius;
        init();
    }

    private void init() {
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, cornerRadius, cornerRadius);

        // Draw card background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
}
