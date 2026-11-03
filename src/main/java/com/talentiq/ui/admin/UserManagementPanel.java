package com.talentiq.ui.admin;

import com.talentiq.dao.UserDAO;
import com.talentiq.model.Role;
import com.talentiq.model.User;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.util.DateUtil;
import com.talentiq.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"User ID", "Username", "Full Name", "Email", "Role", "Status", "Created At"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("System Users & Accounts");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnAdd = new ModernButton("+ Add User", ModernButton.ButtonType.PRIMARY);
        JButton btnResetPwd = new ModernButton("Reset Password", ModernButton.ButtonType.SECONDARY);
        JButton btnToggle = new ModernButton("Toggle Active", ModernButton.ButtonType.SECONDARY);
        JButton btnDelete = new ModernButton("Delete User", ModernButton.ButtonType.DANGER);
        JButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnBar.add(btnAdd);
        btnBar.add(btnResetPwd);
        btnBar.add(btnToggle);
        btnBar.add(btnDelete);
        btnBar.add(btnRefresh);

        header.add(btnBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        table.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new BadgeCellRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showAddUserDialog());
        btnResetPwd.addActionListener(e -> handleResetPassword());
        btnToggle.addActionListener(e -> handleToggleStatus());
        btnDelete.addActionListener(e -> handleDeleteUser());
        btnRefresh.addActionListener(e -> loadUsers());

        loadUsers();
    }

    public void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                return userDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    tableModel.setRowCount(0);
                    for (User u : list) {
                        tableModel.addRow(new Object[]{
                                u.getUserId(),
                                u.getUsername(),
                                u.getFullName(),
                                u.getEmail(),
                                u.getRoleName(),
                                u.isActive() ? "ACTIVE" : "INACTIVE",
                                DateUtil.formatTimestamp(u.getCreatedAt())
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void showAddUserDialog() {
        JTextField txtU = new JTextField(15);
        JPasswordField txtP = new JPasswordField(15);
        JTextField txtName = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"ADMIN", "RECRUITER"});

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.add(new JLabel("Username:"));
        p.add(txtU);
        p.add(new JLabel("Password:"));
        p.add(txtP);
        p.add(new JLabel("Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Role:"));
        p.add(cmbRole);

        int res = JOptionPane.showConfirmDialog(this, p, "Create System User", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String u = txtU.getText().trim();
            String pwd = new String(txtP.getPassword());
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            int roleId = "ADMIN".equals(cmbRole.getSelectedItem()) ? Role.ROLE_ADMIN_ID : Role.ROLE_RECRUITER_ID;

            if (u.isEmpty() || pwd.isEmpty() || name.isEmpty() || !ValidationUtil.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Please provide all valid required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = new User();
            user.setUsername(u);
            user.setFullName(name);
            user.setEmail(email);
            user.setRoleId(roleId);
            user.setActive(true);

            try {
                userDAO.create(user, pwd);
                JOptionPane.showMessageDialog(this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleResetPassword() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.");
            return;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        String newPwd = JOptionPane.showInputDialog(this, "Enter new password for " + username + ":", "Reset Password", JOptionPane.PLAIN_MESSAGE);
        if (newPwd != null && !newPwd.trim().isEmpty()) {
            boolean ok = userDAO.updatePassword(userId, newPwd.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated successfully for " + username);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.");
            return;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        User u = userDAO.findById(userId);
        if (u != null) {
            u.setActive(!u.isActive());
            userDAO.update(u);
            loadUsers();
        }
    }

    private void handleDeleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.");
            return;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user '" + username + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            userDAO.delete(userId);
            loadUsers();
        }
    }
}

