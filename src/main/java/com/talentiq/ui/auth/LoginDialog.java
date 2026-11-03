package com.talentiq.ui.auth;

import com.talentiq.service.AuthService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JLabel lblError = new JLabel(" ");
    private final AuthService authService;
    private boolean loggedIn = false;

    public LoginDialog(JFrame parent, AuthService authService) {
        super(parent, "TalentIQ - Secure Authentication", true);
        this.authService = authService;

        setSize(460, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(24, 30, 24, 30));

        // Logo & Title Header
        JLabel logo = new JLabel("✦ TalentIQ");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(ThemeManager.COLOR_PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Resume & Skill Matching Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(logo);
        root.add(Box.createVerticalStrut(4));
        root.add(subtitle);
        root.add(Box.createVerticalStrut(24));

        // Form Fields
        JPanel form = new JPanel(new GridLayout(4, 1, 6, 6));
        form.setOpaque(false);

        JLabel lblU = new JLabel("Username or Email:");
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtUsername.setPreferredSize(new Dimension(300, 36));

        JLabel lblP = new JLabel("Password:");
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtPassword.setPreferredSize(new Dimension(300, 36));

        form.add(lblU);
        form.add(txtUsername);
        form.add(lblP);
        form.add(txtPassword);
        root.add(form);

        lblError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblError.setForeground(ThemeManager.COLOR_DANGER);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setBorder(new EmptyBorder(8, 0, 8, 0));
        root.add(lblError);

        // Action Buttons
        JButton btnLogin = new ModernButton("Sign In to TalentIQ", ModernButton.ButtonType.PRIMARY);
        btnLogin.setMaximumSize(new Dimension(400, 40));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRegister = new ModernButton("Register as New Candidate", ModernButton.ButtonType.SECONDARY);
        btnRegister.setMaximumSize(new Dimension(400, 36));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());

        btnRegister.addActionListener(e -> {
            RegisterDialog reg = new RegisterDialog(this, authService);
            reg.setVisible(true);
            if (reg.isRegistered()) {
                txtUsername.setText(reg.getRegisteredUsername());
                txtPassword.setText("");
                lblError.setForeground(ThemeManager.COLOR_SUCCESS);
                lblError.setText("Registration successful! Please sign in.");
            }
        });

        root.add(btnLogin);
        root.add(Box.createVerticalStrut(10));
        root.add(btnRegister);
        root.add(Box.createVerticalStrut(16));

        // Quick login shortcuts for tester convenience
        JPanel quickBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        quickBar.setOpaque(false);
        JLabel qLbl = new JLabel("Demo Logins:");
        qLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        qLbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        quickBar.add(qLbl);

        JButton bAdmin = new JButton("Admin");
        bAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        bAdmin.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("Admin@123");
        });

        JButton bRecruiter = new JButton("Recruiter");
        bRecruiter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        bRecruiter.addActionListener(e -> {
            txtUsername.setText("sarah.recruiter");
            txtPassword.setText("Recruiter@123");
        });

        JButton bCandidate = new JButton("Candidate");
        bCandidate.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        bCandidate.addActionListener(e -> {
            txtUsername.setText("john.doe");
            txtPassword.setText("Candidate@123");
        });

        quickBar.add(bAdmin);
        quickBar.add(bRecruiter);
        quickBar.add(bCandidate);
        root.add(quickBar);

        add(root, BorderLayout.CENTER);

        // Default credentials for convenience
        txtUsername.setText("admin");
        txtPassword.setText("Admin@123");
    }

    private void performLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            lblError.setForeground(ThemeManager.COLOR_DANGER);
            lblError.setText("Please enter username and password.");
            return;
        }

        lblError.setText(" ");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return authService.login(u, p);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    boolean success = get();
                    if (success) {
                        loggedIn = true;
                        dispose();
                    } else {
                        lblError.setForeground(ThemeManager.COLOR_DANGER);
                        lblError.setText("Invalid credentials or account is inactive.");
                    }
                } catch (Exception ex) {
                    lblError.setForeground(ThemeManager.COLOR_DANGER);
                    lblError.setText("Connection error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}

