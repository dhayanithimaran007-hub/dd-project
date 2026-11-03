package com.talentiq.ui.admin;

import com.talentiq.config.AppConfig;
import com.talentiq.model.SystemSetting;
import com.talentiq.service.SystemSettingService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SystemSettingsPanel extends JPanel {

    private final SystemSettingService settingService;

    // Weight spinners
    private final JSpinner spnReqWeight = new JSpinner(new SpinnerNumberModel(0.60, 0.0, 1.0, 0.05));
    private final JSpinner spnPrefWeight = new JSpinner(new SpinnerNumberModel(0.20, 0.0, 1.0, 0.05));
    private final JSpinner spnExpWeight = new JSpinner(new SpinnerNumberModel(0.10, 0.0, 1.0, 0.05));
    private final JSpinner spnEduWeight = new JSpinner(new SpinnerNumberModel(0.10, 0.0, 1.0, 0.05));
    private final JLabel lblWeightSum = new JLabel("Total Weight: 100%");

    // Settings table
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Setting Key", "Setting Value", "Description"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    public SystemSettingsPanel(SystemSettingService settingService) {
        this.settingService = settingService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("System Settings & Algorithm Scoring Weights");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnSaveAll = new ModernButton("Save Algorithm Weights", ModernButton.ButtonType.PRIMARY);
        btnSaveAll.addActionListener(e -> saveWeights());
        header.add(btnSaveAll, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Algorithm Weights Configuration Card
        JPanel weightsCard = new JPanel(new GridLayout(3, 2, 14, 10));
        weightsCard.setBorder(BorderFactory.createTitledBorder("Configurable Matching Algorithm Weights (Must sum to 100%)"));

        weightsCard.add(createWeightRow("Required Skills Weight (default 0.60 = 60%):", spnReqWeight));
        weightsCard.add(createWeightRow("Preferred Skills Weight (default 0.20 = 20%):", spnPrefWeight));
        weightsCard.add(createWeightRow("Experience Match Weight (default 0.10 = 10%):", spnExpWeight));
        weightsCard.add(createWeightRow("Education Match Weight (default 0.10 = 10%):", spnEduWeight));

        lblWeightSum.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWeightSum.setForeground(ThemeManager.COLOR_SUCCESS);
        weightsCard.add(lblWeightSum);

        content.add(weightsCard);
        content.add(Box.createVerticalStrut(16));

        // Settings Table Card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBorder(BorderFactory.createTitledBorder("All Persistent Database Configuration Parameters"));
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(tableCard);

        add(content, BorderLayout.CENTER);

        // Spinner listeners
        spnReqWeight.addChangeListener(e -> updateWeightSum());
        spnPrefWeight.addChangeListener(e -> updateWeightSum());
        spnExpWeight.addChangeListener(e -> updateWeightSum());
        spnEduWeight.addChangeListener(e -> updateWeightSum());

        loadSettings();
    }

    private JPanel createWeightRow(String label, JSpinner spinner) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(320, 26));
        spinner.setPreferredSize(new Dimension(80, 28));
        p.add(l);
        p.add(spinner);
        return p;
    }

    private void updateWeightSum() {
        double w1 = ((Number) spnReqWeight.getValue()).doubleValue();
        double w2 = ((Number) spnPrefWeight.getValue()).doubleValue();
        double w3 = ((Number) spnExpWeight.getValue()).doubleValue();
        double w4 = ((Number) spnEduWeight.getValue()).doubleValue();
        double sum = Math.round((w1 + w2 + w3 + w4) * 100.0) / 100.0;

        lblWeightSum.setText(String.format("Total Sum: %.0f%% (1.00 is required)", sum * 100.0));
        if (Math.abs(sum - 1.0) < 0.001) {
            lblWeightSum.setForeground(ThemeManager.COLOR_SUCCESS);
        } else {
            lblWeightSum.setForeground(ThemeManager.COLOR_DANGER);
        }
    }

    private void loadSettings() {
        spnReqWeight.setValue(AppConfig.getRequiredSkillWeight());
        spnPrefWeight.setValue(AppConfig.getPreferredSkillWeight());
        spnExpWeight.setValue(AppConfig.getExperienceWeight());
        spnEduWeight.setValue(AppConfig.getEducationWeight());
        updateWeightSum();

        tableModel.setRowCount(0);
        List<SystemSetting> list = settingService.getAllSettings();
        for (SystemSetting s : list) {
            tableModel.addRow(new Object[]{s.getSettingKey(), s.getSettingValue(), s.getSettingDescription()});
        }
    }

    private void saveWeights() {
        double w1 = ((Number) spnReqWeight.getValue()).doubleValue();
        double w2 = ((Number) spnPrefWeight.getValue()).doubleValue();
        double w3 = ((Number) spnExpWeight.getValue()).doubleValue();
        double w4 = ((Number) spnEduWeight.getValue()).doubleValue();
        double sum = Math.round((w1 + w2 + w3 + w4) * 100.0) / 100.0;

        if (Math.abs(sum - 1.0) > 0.001) {
            JOptionPane.showMessageDialog(this,
                    "Scoring weights must add up to exactly 1.00 (100%). Current sum is: " + (sum * 100.0) + "%",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        settingService.saveSetting("matching.weight.required", String.valueOf(w1), "Weight for required skills");
        settingService.saveSetting("matching.weight.preferred", String.valueOf(w2), "Weight for preferred skills");
        settingService.saveSetting("matching.weight.experience", String.valueOf(w3), "Weight for experience years");
        settingService.saveSetting("matching.weight.education", String.valueOf(w4), "Weight for education level");

        JOptionPane.showMessageDialog(this, "Scoring weights updated successfully in database and runtime config!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadSettings();
    }
}

