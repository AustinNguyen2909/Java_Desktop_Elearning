package com.elearning.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern styled password field with placeholder
 */
public class ModernPasswordField extends JPasswordField {
    private String placeholder;
    private Color placeholderColor = new Color(150, 150, 150);

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        init();
    }

    public ModernPasswordField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        setPreferredSize(new Dimension(300, 45));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw placeholder if password is empty
        if (getPassword().length == 0 && placeholder != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(placeholderColor);
            g2.setFont(getFont());
            
            FontMetrics fm = g2.getFontMetrics();
            int x = getInsets().left;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
