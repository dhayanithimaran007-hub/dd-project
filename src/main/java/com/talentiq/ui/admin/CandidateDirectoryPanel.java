package com.talentiq.ui.admin;

import com.talentiq.model.CandidateProfile;
import com.talentiq.service.AuthService;
import com.talentiq.service.CandidateService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.SearchFilterPanel;
import com.talentiq.ui.dialogs.CandidateDetailDialog;
import com.talentiq.ui.dialogs.CandidateEditDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CandidateDirectoryPanel extends JPanel {

    private final CandidateService candidateService;
    private final AuthService authService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Candidate ID", "Full Name", "Email", "Phone", "Headline", "Total Exp", "Education", "Current Company", "Location"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    private final SearchFilterPanel searchPanel = new SearchFilterPanel("Search name, email, headline...",
            new String[]{"All Education Levels", "Doctorate", "Master", "Bachelor", "Associate", "High School"});

    public CandidateDirectoryPanel(CandidateService candidateService, AuthService authService) {
        this.candidateService = candidateService;
        this.authService = authService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Talent Directory & Candidate Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnAdd = new ModernButton("+ Add Candidate", ModernButton.ButtonType.PRIMARY);
        JButton btnView = new ModernButton("View Full Dossier", ModernButton.ButtonType.SECONDARY);
        JButton btnEdit = new ModernButton("Edit Profile", ModernButton.ButtonType.SECONDARY);
        JButton btnDelete = new ModernButton("Delete", ModernButton.ButtonType.DANGER);
        JButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnBar.add(btnAdd);
        btnBar.add(btnView);
        btnBar.add(btnEdit);
        btnBar.add(btnDelete);
        btnBar.add(btnRefresh);
        topBar.add(btnBar, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(topBar, BorderLayout.NORTH);
        north.add(searchPanel, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
            CandidateEditDialog dlg = new CandidateEditDialog(top, new CandidateProfile(), true, candidateService, authService);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadCandidates();
        });

        btnView.addActionListener(e -> {
            CandidateProfile cp = getSelectedCandidate();
            if (cp != null) {
                JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
                new CandidateDetailDialog(top, cp, candidateService).setVisible(true);
            }
        });

        btnEdit.addActionListener(e -> {
            CandidateProfile cp = getSelectedCandidate();
            if (cp != null) {
                JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
                CandidateEditDialog dlg = new CandidateEditDialog(top, cp, false, candidateService, authService);
                dlg.setVisible(true);
                if (dlg.isSaved()) loadCandidates();
            }
        });

        btnDelete.addActionListener(e -> {
            CandidateProfile cp = getSelectedCandidate();
            if (cp != null) {
                int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete candidate '" + cp.getFullName() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    candidateService.deleteCandidate(cp.getCandidateId());
                    loadCandidates();
                }
            }
        });

        btnRefresh.addActionListener(e -> loadCandidates());
        searchPanel.addSearchListener(e -> loadCandidates());
        searchPanel.addResetListener(e -> loadCandidates());

        loadCandidates();
    }

    public void loadCandidates() {
        String kw = searchPanel.getSearchText();
        String edu = searchPanel.getSelectedFilter();
        if ("All Education Levels".equalsIgnoreCase(edu)) edu = null;

        List<CandidateProfile> list = candidateService.getAllCandidates(kw, null, edu);
        tableModel.setRowCount(0);
        for (CandidateProfile c : list) {
            tableModel.addRow(new Object[]{
                    c.getCandidateId(),
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getHeadline(),
                    c.getTotalExperienceYears() + " yrs",
                    c.getHighestEducationLevel(),
                    c.getCurrentCompany(),
                    c.getLocation()
            });
        }
    }

    private CandidateProfile getSelectedCandidate() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a candidate row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int cId = (int) tableModel.getValueAt(r, 0);
        return candidateService.getCandidateById(cId);
    }
}

