package com.talentiq.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SearchFilterPanel extends JPanel {

    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<String> cmbFilter = new JComboBox<>();
    private final JButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
    private final JButton btnReset = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);

    public SearchFilterPanel(String searchPlaceholder, String[] filterOptions) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setOpaque(false);

        txtSearch.putClientProperty("JTextField.placeholderText", searchPlaceholder);
        txtSearch.setPreferredSize(new Dimension(220, 34));

        if (filterOptions != null) {
            for (String opt : filterOptions) {
                cmbFilter.addItem(opt);
            }
            cmbFilter.setPreferredSize(new Dimension(140, 34));
        }

        add(new JLabel("Search:"));
        add(txtSearch);

        if (filterOptions != null && filterOptions.length > 0) {
            add(new JLabel("Filter:"));
            add(cmbFilter);
        }

        add(btnSearch);
        add(btnReset);
    }

    public String getSearchText() {
        return txtSearch.getText().trim();
    }

    public String getSelectedFilter() {
        return (String) cmbFilter.getSelectedItem();
    }

    public void addSearchListener(ActionListener listener) {
        btnSearch.addActionListener(listener);
        txtSearch.addActionListener(listener);
    }

    public void addResetListener(ActionListener listener) {
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            if (cmbFilter.getItemCount() > 0) cmbFilter.setSelectedIndex(0);
            listener.actionPerformed(e);
        });
    }
}

