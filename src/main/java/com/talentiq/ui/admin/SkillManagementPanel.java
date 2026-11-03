package com.talentiq.ui.admin;

import com.talentiq.model.Skill;
import com.talentiq.model.SkillCategory;
import com.talentiq.service.SkillService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.SearchFilterPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SkillManagementPanel extends JPanel {

    private final SkillService skillService;

    // Categories list
    private final DefaultListModel<SkillCategory> categoryListModel = new DefaultListModel<>();
    private final JList<SkillCategory> categoryList = new JList<>(categoryListModel);

    // Skills table
    private final DefaultTableModel skillTableModel = new DefaultTableModel(
            new String[]{"ID", "Skill Name", "Category", "Aliases", "Description"}, 0);
    private final ModernTable skillTable = new ModernTable(skillTableModel);

    private final JTextField txtSearch = new JTextField(16);

    public SkillManagementPanel(SkillService skillService) {
        this.skillService = skillService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Centralized Skill Dictionary (8 Industry Categories)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnBar.setOpaque(false);

        txtSearch.putClientProperty("JTextField.placeholderText", "Search skill name or alias...");
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.addActionListener(e -> filterSkills());

        JButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.addActionListener(e -> filterSkills());

        JButton btnAddSkill = new ModernButton("+ Add Skill", ModernButton.ButtonType.PRIMARY);
        JButton btnEditSkill = new ModernButton("Edit Skill", ModernButton.ButtonType.SECONDARY);
        JButton btnDeleteSkill = new ModernButton("Delete Skill", ModernButton.ButtonType.DANGER);
        JButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        actionBtnBar.add(txtSearch);
        actionBtnBar.add(btnSearch);
        actionBtnBar.add(btnAddSkill);
        actionBtnBar.add(btnEditSkill);
        actionBtnBar.add(btnDeleteSkill);
        actionBtnBar.add(btnRefresh);

        topBar.add(actionBtnBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Split Pane: Left = Categories, Right = Skills
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(220);
        splitPane.setBorder(null);

        // Left Panel (Categories)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Skill Categories"));

        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSkillsForSelectedCategory();
            }
        });

        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // Right Panel (Skills Table)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Skills in Category"));
        rightPanel.add(new JScrollPane(skillTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Event Listeners
        btnAddSkill.addActionListener(e -> showAddEditSkillDialog(null));
        btnEditSkill.addActionListener(e -> {
            int row = skillTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a skill from the table first.");
                return;
            }
            int sId = (int) skillTableModel.getValueAt(row, 0);
            Skill skill = skillService.getSkillById(sId);
            if (skill != null) showAddEditSkillDialog(skill);
        });

        btnDeleteSkill.addActionListener(e -> {
            int row = skillTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a skill from the table first.");
                return;
            }
            int sId = (int) skillTableModel.getValueAt(row, 0);
            String name = (String) skillTableModel.getValueAt(row, 1);
            int conf = JOptionPane.showConfirmDialog(this, "Delete skill '" + name + "' from dictionary?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                skillService.deleteSkill(sId);
                loadSkillsForSelectedCategory();
            }
        });

        btnRefresh.addActionListener(e -> loadCategories());

        loadCategories();
    }

    private void loadCategories() {
        categoryListModel.clear();
        SkillCategory allCat = new SkillCategory(0, "All Categories", "Show all skills", 0);
        categoryListModel.addElement(allCat);

        List<SkillCategory> list = skillService.getAllCategories();
        for (SkillCategory c : list) {
            categoryListModel.addElement(c);
        }

        if (categoryListModel.size() > 0) {
            categoryList.setSelectedIndex(0);
        }
    }

    private void loadSkillsForSelectedCategory() {
        SkillCategory selected = categoryList.getSelectedValue();
        if (selected == null) return;

        skillTableModel.setRowCount(0);
        List<Skill> list;
        if (selected.getCategoryId() == 0) {
            list = skillService.getAllSkills();
        } else {
            list = skillService.getSkillsByCategory(selected.getCategoryId());
        }

        for (Skill s : list) {
            skillTableModel.addRow(new Object[]{
                    s.getSkillId(),
                    s.getSkillName(),
                    s.getCategoryName(),
                    s.getAliases() != null ? s.getAliases() : "",
                    s.getDescription() != null ? s.getDescription() : ""
            });
        }
    }

    private void filterSkills() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadSkillsForSelectedCategory();
            return;
        }

        List<Skill> all = skillService.getAllSkills();
        skillTableModel.setRowCount(0);
        for (Skill s : all) {
            boolean matchName = s.getSkillName().toLowerCase().contains(kw);
            boolean matchAlias = s.getAliases() != null && s.getAliases().toLowerCase().contains(kw);
            if (matchName || matchAlias) {
                skillTableModel.addRow(new Object[]{
                        s.getSkillId(),
                        s.getSkillName(),
                        s.getCategoryName(),
                        s.getAliases() != null ? s.getAliases() : "",
                        s.getDescription() != null ? s.getDescription() : ""
                });
            }
        }
    }

    private void showAddEditSkillDialog(Skill existing) {
        JTextField txtName = new JTextField(existing != null ? existing.getSkillName() : "", 18);
        JTextField txtAliases = new JTextField(existing != null ? existing.getAliases() : "", 18);
        JTextField txtDesc = new JTextField(existing != null ? existing.getDescription() : "", 18);

        List<SkillCategory> cats = skillService.getAllCategories();
        JComboBox<SkillCategory> cmbCats = new JComboBox<>(cats.toArray(new SkillCategory[0]));

        if (existing != null) {
            for (int i = 0; i < cats.size(); i++) {
                if (cats.get(i).getCategoryId() == existing.getCategoryId()) {
                    cmbCats.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Category:"));
        p.add(cmbCats);
        p.add(new JLabel("Skill Name:"));
        p.add(txtName);
        p.add(new JLabel("Aliases (comma-sep):"));
        p.add(txtAliases);
        p.add(new JLabel("Description:"));
        p.add(txtDesc);

        int res = JOptionPane.showConfirmDialog(this, p, existing != null ? "Edit Skill" : "Add Skill to Dictionary", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Skill name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SkillCategory selCat = (SkillCategory) cmbCats.getSelectedItem();
            if (selCat == null) return;

            try {
                if (existing != null) {
                    existing.setCategoryId(selCat.getCategoryId());
                    existing.setSkillName(name);
                    existing.setAliases(txtAliases.getText().trim());
                    existing.setDescription(txtDesc.getText().trim());
                    skillService.updateSkill(existing);
                } else {
                    Skill s = new Skill();
                    s.setCategoryId(selCat.getCategoryId());
                    s.setSkillName(name);
                    s.setAliases(txtAliases.getText().trim());
                    s.setDescription(txtDesc.getText().trim());
                    skillService.addSkill(s);
                }
                loadSkillsForSelectedCategory();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving skill: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

