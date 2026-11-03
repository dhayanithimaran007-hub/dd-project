package com.talentiq.ui.components;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProgressBarCellRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressBarCellRenderer() {
        super(0, 100);
        setStringPainted(true);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int intVal = 0;
        if (value instanceof Number) {
            intVal = (int) Math.round(((Number) value).doubleValue());
        } else if (value != null) {
            try {
                String clean = value.toString().replace("%", "").trim();
                intVal = (int) Math.round(Double.parseDouble(clean));
            } catch (Exception ignored) {}
        }

        setValue(intVal);
        setString(intVal + "%");

        Color barColor;
        if (intVal >= 85) {
            barColor = new Color(16, 185, 129); // Green
        } else if (intVal >= 70) {
            barColor = new Color(37, 99, 235);  // Blue
        } else if (intVal >= 50) {
            barColor = new Color(245, 158, 11); // Amber
        } else {
            barColor = new Color(239, 68, 68);  // Red
        }

        setForeground(barColor);
        setBackground(new Color(150, 150, 150, 40));

        return this;
    }
}

