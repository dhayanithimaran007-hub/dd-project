package com.talentiq.ui.admin;

import com.talentiq.model.ActivityLog;
import com.talentiq.service.ActivityLogService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.util.DateUtil;
import com.talentiq.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ActivityLogPanel extends JPanel {

    private final ActivityLogService logService;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Log ID", "Action Type", "Description", "User", "IP Address", "Timestamp"}, 0);
    private final ModernTable table = new ModernTable(tableModel);
    private final JTextField txtFilter = new JTextField(18);

    public ActivityLogPanel(ActivityLogService logService) {
        this.logService = logService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Audit & Activity Logs");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnBar.setOpaque(false);

        txtFilter.putClientProperty("JTextField.placeholderText", "Filter activity logs...");
        txtFilter.setPreferredSize(new Dimension(200, 32));
        txtFilter.addActionListener(e -> filterLogs());

        JButton btnSearch = new ModernButton("Filter", ModernButton.ButtonType.SECONDARY);
        btnSearch.addActionListener(e -> filterLogs());

        JButton btnExport = new ModernButton("Export CSV", ModernButton.ButtonType.SECONDARY);
        btnExport.addActionListener(e -> exportLogs());

        JButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.PRIMARY);
        btnRefresh.addActionListener(e -> loadLogs());

        actionBtnBar.add(txtFilter);
        actionBtnBar.add(btnSearch);
        actionBtnBar.add(btnExport);
        actionBtnBar.add(btnRefresh);

        header.add(actionBtnBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadLogs();
    }

    private void loadLogs() {
        tableModel.setRowCount(0);
        List<ActivityLog> list = logService.getRecentActivities(100);
        for (ActivityLog l : list) {
            tableModel.addRow(new Object[]{
                    l.getLogId(),
                    l.getActionType(),
                    l.getDescription(),
                    l.getUsername() != null ? l.getUsername() : "System",
                    l.getIpAddress(),
                    DateUtil.formatTimestamp(l.getCreatedAt())
            });
        }
    }

    private void filterLogs() {
        String kw = txtFilter.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadLogs();
            return;
        }

        List<ActivityLog> list = logService.getRecentActivities(100);
        tableModel.setRowCount(0);
        for (ActivityLog l : list) {
            String act = l.getActionType().toLowerCase();
            String desc = l.getDescription().toLowerCase();
            String u = (l.getUsername() != null ? l.getUsername() : "").toLowerCase();
            if (act.contains(kw) || desc.contains(kw) || u.contains(kw)) {
                tableModel.addRow(new Object[]{
                        l.getLogId(),
                        l.getActionType(),
                        l.getDescription(),
                        l.getUsername() != null ? l.getUsername() : "System",
                        l.getIpAddress(),
                        DateUtil.formatTimestamp(l.getCreatedAt())
                });
            }
        }
    }

    private void exportLogs() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("TalentIQ_Activity_Logs.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ExportUtil.exportTableToCSV(table, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Logs exported successfully to:\n" + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

