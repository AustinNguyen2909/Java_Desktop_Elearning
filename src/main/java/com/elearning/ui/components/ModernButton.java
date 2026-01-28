package com.elearning.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Modern styled button with hover effects
 */
public class ModernButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;

    public ModernButton(String text) {
        super(text);
        init();
    }

    public ModernButton(String text, Color normalColor) {
        super(text);
        this.normalColor = normalColor;
        init();
    }

    private void init() {
        if (normalColor == null) {
            normalColor = new Color(47, 111, 235); // Blue
        }
        hoverColor = brighten(normalColor, 0.2f);
        pressedColor = darken(normalColor, 0.2f);

        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBackground(normalColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 40));

        // Add hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(normalColor);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                setBackground(pressedColor);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                setBackground(hoverColor);
            }
        });
    }

    private Color brighten(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() * (1 + factor)));
        int g = Math.min(255, (int) (color.getGreen() * (1 + factor)));
        int b = Math.min(255, (int) (color.getBlue() * (1 + factor)));
        return new Color(r, g, b);
    }

    private Color darken(Color color, float factor) {
        int r = Math.max(0, (int) (color.getRed() * (1 - factor)));
        int g = Math.max(0, (int) (color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int) (color.getBlue() * (1 - factor)));
        return new Color(r, g, b);
    }
}
