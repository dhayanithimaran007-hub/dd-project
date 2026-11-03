package com.talentiq.ui.components;

import javax.swing.*;
import java.awt.*;

public class ModernButton extends JButton {

    public enum ButtonType {
        PRIMARY, SUCCESS, DANGER, SECONDARY
    }

    private final ButtonType type;

    public ModernButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    public ModernButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        initStyle();
    }

    private void initStyle() {
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        Color bg;
        Color fg = Color.WHITE;

        switch (type) {
            case SUCCESS:
                bg = new Color(16, 185, 129);
                break;
            case DANGER:
                bg = new Color(239, 68, 68);
                break;
            case SECONDARY:
                bg = new Color(100, 116, 139);
                break;
            case PRIMARY:
            default:
                bg = new Color(37, 99, 235);
                break;
        }

        setBackground(bg);
        setForeground(fg);
    }
}

