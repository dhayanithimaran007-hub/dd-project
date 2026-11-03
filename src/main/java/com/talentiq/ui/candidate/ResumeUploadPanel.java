package com.talentiq.ui.candidate;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.Resume;
import com.talentiq.service.ResumeService;
import com.talentiq.service.SkillService;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.dialogs.ResumeCorrectionDialog;
import com.talentiq.util.DateUtil;
import com.talentiq.util.ResumeParser;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ResumeUploadPanel extends JPanel {

    private final ResumeService resumeService;
    private final SkillService skillService;

    private final JLabel lblSelectedFile = new JLabel("No file selected (Supports: PDF, DOCX, TXT)");
    private File selectedFile;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Resume ID", "File Name", "Type", "File Size", "Primary", "Upload Date"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    public ResumeUploadPanel(ResumeService resumeService, SkillService skillService) {
        this.resumeService = resumeService;
        this.skillService = skillService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Resume Management & Rule-Based Parsing");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Upload Drop/Picker Card
        JPanel uploadCard = new JPanel(new BorderLayout(12, 12));
        uploadCard.setBorder(BorderFactory.createTitledBorder("Upload & Parse Resume (PDFBox & Apache POI)"));
        uploadCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel pickerBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pickerBar.setOpaque(false);

        JButton btnBrowse = new ModernButton("Choose Resume File...", ModernButton.ButtonType.SECONDARY);
        JButton btnUploadParse = new ModernButton("Upload & Extract Information", ModernButton.ButtonType.PRIMARY);

        lblSelectedFile.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        pickerBar.add(btnBrowse);
        pickerBar.add(lblSelectedFile);
        pickerBar.add(btnUploadParse);
        uploadCard.add(pickerBar, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("<html><i>Conventional parsing extracts email, phone, experience duration, education degree, and dictionary skills. No AI/NLP used.</i></html>");
        lblHint.setBorder(new EmptyBorder(0, 14, 8, 0));
        uploadCard.add(lblHint, BorderLayout.SOUTH);
        content.add(uploadCard);
        content.add(Box.createVerticalStrut(16));

        // Resumes List Table
        JPanel listCard = new JPanel(new BorderLayout(10, 10));
        listCard.setBorder(BorderFactory.createTitledBorder("My Uploaded Resumes"));

        table.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());
        listCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnViewText = new ModernButton("View Extracted Text", ModernButton.ButtonType.SECONDARY);
        JButton btnSetPrimary = new ModernButton("Set as Primary", ModernButton.ButtonType.PRIMARY);
        JButton btnDownload = new ModernButton("Download File", ModernButton.ButtonType.SECONDARY);
        JButton btnDelete = new ModernButton("Delete Resume", ModernButton.ButtonType.DANGER);

        actionBtnBar.add(btnViewText);
        actionBtnBar.add(btnSetPrimary);
        actionBtnBar.add(btnDownload);
        actionBtnBar.add(btnDelete);
        listCard.add(actionBtnBar, BorderLayout.SOUTH);

        content.add(listCard);
        add(content, BorderLayout.CENTER);

        // Browse Action
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Resume Documents (*.pdf, *.docx, *.txt)", "pdf", "docx", "txt"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                lblSelectedFile.setText("Selected: " + selectedFile.getName() + " (" + (selectedFile.length() / 1024) + " KB)");
            }
        });

        // Upload & Parse Action
        btnUploadParse.addActionListener(e -> {
            if (selectedFile == null || !selectedFile.exists()) {
                JOptionPane.showMessageDialog(this, "Please choose a resume file first.", "File Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
            if (cp == null) {
                JOptionPane.showMessageDialog(this, "No active candidate session found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<ResumeParser.ParsedResumeResult, Void> worker = new SwingWorker<>() {
                @Override
                protected ResumeParser.ParsedResumeResult doInBackground() throws Exception {
                    return resumeService.uploadAndParseResume(cp.getCandidateId(), selectedFile);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        ResumeParser.ParsedResumeResult result = get();
                        loadResumes();
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(ResumeUploadPanel.this);
                        ResumeCorrectionDialog dlg = new ResumeCorrectionDialog(topFrame, cp.getCandidateId(), result, resumeService, skillService);
                        dlg.setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ResumeUploadPanel.this, "Upload / Parsing failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        // View Extracted Text
        btnViewText.addActionListener(e -> {
            Resume r = getSelectedResume();
            if (r != null) {
                JTextArea txt = new JTextArea(r.getExtractedText(), 20, 50);
                txt.setLineWrap(true);
                txt.setWrapStyleWord(true);
                txt.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(txt), "Extracted Text - " + r.getFileName(), JOptionPane.PLAIN_MESSAGE);
            }
        });

        // Set Primary
        btnSetPrimary.addActionListener(e -> {
            Resume r = getSelectedResume();
            if (r != null) {
                CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
                if (cp != null) {
                    resumeService.setPrimaryResume(cp.getCandidateId(), r.getResumeId());
                    loadResumes();
                }
            }
        });

        // Download
        btnDownload.addActionListener(e -> {
            Resume r = getSelectedResume();
            if (r != null) {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("Download_" + r.getFileName()));
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        byte[] bytes = Files.readAllBytes(Paths.get(r.getStoredFilePath()));
                        try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                            fos.write(bytes);
                        }
                        JOptionPane.showMessageDialog(this, "Resume downloaded successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to download: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Delete
        btnDelete.addActionListener(e -> {
            Resume r = getSelectedResume();
            if (r != null) {
                int conf = JOptionPane.showConfirmDialog(this, "Delete resume '" + r.getFileName() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    resumeService.deleteResume(r.getResumeId());
                    loadResumes();
                }
            }
        });

        loadResumes();
    }

    public void loadResumes() {
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp == null) {
            tableModel.setRowCount(0);
            return;
        }

        List<Resume> list = resumeService.getCandidateResumes(cp.getCandidateId());
        tableModel.setRowCount(0);
        for (Resume r : list) {
            tableModel.addRow(new Object[]{
                    r.getResumeId(),
                    r.getFileName(),
                    r.getFileType(),
                    r.getFormattedSize(),
                    r.isPrimary() ? "ACTIVE" : "INACTIVE",
                    DateUtil.formatTimestamp(r.getUploadedAt())
            });
        }
    }

    private Resume getSelectedResume() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resume row first.");
            return null;
        }
        int resumeId = (int) tableModel.getValueAt(row, 0);
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp != null) {
            List<Resume> list = resumeService.getCandidateResumes(cp.getCandidateId());
            for (Resume r : list) {
                if (r.getResumeId() == resumeId) return r;
            }
        }
        return null;
    }
}

