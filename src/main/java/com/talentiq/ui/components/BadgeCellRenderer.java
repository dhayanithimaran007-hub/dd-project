package com.talentiq.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BadgeCellRenderer extends DefaultTableCellRenderer {

    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    private final JLabel badge = new JLabel();

    public BadgeCellRenderer() {
        panel.setOpaque(false);
        badge.setOpaque(true);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        panel.add(badge);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String text = value != null ? value.toString() : "";
        badge.setText(text.replace("_", " "));

        Color bg;
        Color fg = Color.WHITE;

        String upper = text.toUpperCase();
        switch (upper) {
            case "SHORTLISTED":
            case "HIRED":
            case "ACTIVE":
            case "EXPERT":
            case "DOCTORATE":
                bg = new Color(16, 185, 129); // Green
                break;
            case "INTERVIEW_SCHEDULED":
            case "ADVANCED":
            case "MASTER":
                bg = new Color(37, 99, 235); // Blue
                break;
            case "APPLIED":
            case "INTERMEDIATE":
            case "BACHELOR":
                bg = new Color(245, 158, 11); // Amber
                break;
            case "REJECTED":
            case "CLOSED":
                bg = new Color(239, 68, 68); // Red
                break;
            case "DRAFT":
            case "BEGINNER":
            case "ASSOCIATE":
            default:
                bg = new Color(100, 116, 139); // Slate Grey
                break;
        }

        badge.setBackground(bg);
        badge.setForeground(fg);

        return panel;
    }
}

