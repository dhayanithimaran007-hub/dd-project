package com.talentiq.ui.auth;

import com.talentiq.service.AuthService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JPasswordField txtConfirmPassword = new JPasswordField(20);
    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtHeadline = new JTextField(20);
    private final JLabel lblError = new JLabel(" ");

    private boolean registered = false;
    private String registeredUsername = "";

    public RegisterDialog(JDialog parent, AuthService authService) {
        super(parent, "TalentIQ - Candidate Registration", true);
        setSize(460, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(20, 26, 20, 26));

        JLabel title = new JLabel("Create Candidate Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ThemeManager.COLOR_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(16));

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setOpaque(false);

        form.add(new JLabel("Full Name:"));
        form.add(txtFullName);

        form.add(new JLabel("Username:"));
        form.add(txtUsername);

        form.add(new JLabel("Email Address:"));
        form.add(txtEmail);

        form.add(new JLabel("Phone Number:"));
        form.add(txtPhone);

        form.add(new JLabel("Professional Headline:"));
        form.add(txtHeadline);
        txtHeadline.setText("Software Professional");

        form.add(new JLabel("Password (min 6 chars):"));
        form.add(txtPassword);

        form.add(new JLabel("Confirm Password:"));
        form.add(txtConfirmPassword);

        root.add(form);

        lblError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblError.setForeground(ThemeManager.COLOR_DANGER);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setBorder(new EmptyBorder(8, 0, 8, 0));
        root.add(lblError);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnSubmit = new ModernButton("Create Account", ModernButton.ButtonType.SUCCESS);
        JButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);

        btnSubmit.addActionListener(e -> {
            String u = txtUsername.getText().trim();
            String name = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String p1 = new String(txtPassword.getPassword());
            String p2 = new String(txtConfirmPassword.getPassword());

            if (!ValidationUtil.isNotEmpty(u) || !ValidationUtil.isNotEmpty(name)) {
                lblError.setText("Username and Full Name are required.");
                return;
            }
            if (!ValidationUtil.isValidEmail(email)) {
                lblError.setText("Valid email is required.");
                return;
            }
            if (!ValidationUtil.isValidPassword(p1)) {
                lblError.setText("Password must be at least 6 characters.");
                return;
            }
            if (!p1.equals(p2)) {
                lblError.setText("Passwords do not match.");
                return;
            }

            try {
                authService.registerCandidate(u, p1, email, name, txtPhone.getText().trim(), txtHeadline.getText().trim());
                registered = true;
                registeredUsername = u;
                dispose();
            } catch (Exception ex) {
                lblError.setText(ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);
        root.add(btnPanel);

        add(root, BorderLayout.CENTER);
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }
}

