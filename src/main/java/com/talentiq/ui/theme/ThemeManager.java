package com.talentiq.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String PREF_DARK_MODE = "talentiq.darkmode";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static boolean isDarkMode = prefs.getBoolean(PREF_DARK_MODE, true);

    // Accent and branding colors
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);     // Blue #2563EB
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);   // Green #10B981
    public static final Color COLOR_WARNING = new Color(245, 158, 11);   // Amber #F59E0B
    public static final Color COLOR_DANGER = new Color(239, 68, 68);     // Red #EF4444
    public static final Color COLOR_INFO = new Color(14, 165, 233);      // Sky #0EA5E9
    public static final Color COLOR_NAVY = new Color(30, 41, 59);        // Slate Navy

    private ThemeManager() {}

    public static void setupTheme() {
        try {
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");

            if (isDarkMode) {
                FlatMacDarkLaf.setup();
            } else {
                FlatMacLightLaf.setup();
            }

            configureUIDefaults();
        } catch (Exception e) {
            // Fallback to standard FlatDarkLaf
            try {
                if (isDarkMode) FlatDarkLaf.setup();
                else FlatLightLaf.setup();
                configureUIDefaults();
            } catch (Exception ignored) {}
        }
    }

    private static void configureUIDefaults() {
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 12);

        // Apply clean modern font
        Font baseFont = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", baseFont);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void toggleTheme(JFrame frame) {
        isDarkMode = !isDarkMode;
        prefs.putBoolean(PREF_DARK_MODE, isDarkMode);
        setupTheme();
        if (frame != null) {
            SwingUtilities.updateComponentTreeUI(frame);
        }
    }
}

