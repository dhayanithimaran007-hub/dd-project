package com.talentiq.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class ModernTable extends JTable {

    public ModernTable() {
        super();
        initStyle();
    }

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    private void initStyle() {
        setRowHeight(36);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));
        setFillsViewportHeight(true);

        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        // Center / Left default align
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        setDefaultRenderer(Object.class, leftRenderer);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            Color bg = row % 2 == 0 ? UIManager.getColor("Table.background") : UIManager.getColor("Table.alternateRowColor");
            if (bg == null) {
                bg = row % 2 == 0 ? getBackground() : new Color(getBackground().getRed() - 5, getBackground().getGreen() - 5, getBackground().getBlue() - 5);
            }
            c.setBackground(bg);
            c.setForeground(UIManager.getColor("Table.foreground"));
        }
        return c;
    }
}

