package com.elearning.ui.components;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

/**
 * Centralized UI theme tokens and defaults.
 */
public final class UITheme {
    public static final Color PRIMARY = new Color(8, 145, 178);
    public static final Color PRIMARY_DARK = new Color(6, 123, 150);
    public static final Color SECONDARY = new Color(34, 211, 238);
    public static final Color ACCENT = new Color(34, 197, 94);
    public static final Color DANGER = new Color(225, 29, 72);
    public static final Color BACKGROUND = new Color(236, 254, 255);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TEXT = new Color(22, 78, 99);
    public static final Color MUTED_TEXT = new Color(71, 85, 105);

    private UITheme() {
    }

    public static void applyDefaults() {
        Font baseFont = new Font(resolveFontFamily(), Font.PLAIN, 13);
        Font boldFont = baseFont.deriveFont(Font.BOLD);

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", new Color(224, 248, 252));
        UIManager.put("TableHeader.foreground", TEXT);

        UIManager.put("Label.font", baseFont);
        UIManager.put("Button.font", boldFont);
        UIManager.put("TextField.font", baseFont);
        UIManager.put("PasswordField.font", baseFont);
        UIManager.put("TextArea.font", baseFont);
        UIManager.put("Table.font", baseFont);
        UIManager.put("TableHeader.font", boldFont.deriveFont(12f));
        UIManager.put("TabbedPane.font", boldFont.deriveFont(13f));
    }

    private static String resolveFontFamily() {
        String preferred = "Fira Sans";
        Set<String> names = new HashSet<>();
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            names.add(name);
        }
        return names.contains(preferred) ? preferred : "Segoe UI";
    }
}
