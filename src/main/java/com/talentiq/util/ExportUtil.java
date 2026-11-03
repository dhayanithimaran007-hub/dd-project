package com.talentiq.util;

import com.talentiq.model.MatchDetail;
import com.talentiq.model.MatchResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportUtil {

    private ExportUtil() {}

    /**
     * Exports any JTable content to a standard comma-separated CSV file.
     */
    public static void exportTableToCSV(JTable table, File targetFile) throws IOException {
        TableModel model = table.getModel();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            // Write column headers
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.write(escapeCSV(model.getColumnName(i)));
                if (i < model.getColumnCount() - 1) writer.write(",");
            }
            writer.newLine();

            // Write row data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    writer.write(escapeCSV(val != null ? val.toString() : ""));
                    if (col < model.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();
            }
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Exports a comprehensive candidate evaluation and match analysis to PDF using Apache PDFBox 3.x.
     */
    public static void exportMatchReportToPDF(MatchResult match, File targetFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float y = 790;

                // Header Banner
                cs.setNonStrokingColor(30, 58, 138); // Deep Navy
                cs.addRect(margin, y - 5, 495, 30);
                cs.fill();

                cs.beginText();
                cs.setFont(fontBold, 16);
                cs.setNonStrokingColor(255, 255, 255); // White text
                cs.newLineAtOffset(margin + 12, y + 5);
                cs.showText("TalentIQ – Candidate Evaluation & Skill Match Report");
                cs.endText();

                y -= 45;

                // Candidate & Job Info Block
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.setNonStrokingColor(15, 23, 42); // Dark slate
                cs.newLineAtOffset(margin, y);
                cs.showText("Candidate: " + sanitize(match.getCandidateName()));
                cs.endText();

                y -= 20;
                cs.beginText();
                cs.setFont(fontRegular, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("Target Position: " + sanitize(match.getJobTitle()));
                cs.endText();

                y -= 20;
                cs.beginText();
                cs.setFont(fontBold, 13);
                cs.setNonStrokingColor(16, 185, 129); // Accent green
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("Overall Matching Score: %.1f%%", match.getOverallScore()));
                cs.endText();

                y -= 30;

                // Score Breakdown Table
                cs.setNonStrokingColor(15, 23, 42);
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(margin, y);
                cs.showText("Score Component Breakdown (Rule-Based Weighted Calculation):");
                cs.endText();

                y -= 20;
                String[] rows = {
                        String.format("Required Skills (60%% Weight):   %.1f%%", match.getRequiredSkillScore()),
                        String.format("Preferred Skills (20%% Weight):  %.1f%%", match.getPreferredSkillScore()),
                        String.format("Experience Match (10%% Weight):  %.1f%%", match.getExperienceScore()),
                        String.format("Education Match (10%% Weight):   %.1f%%", match.getEducationScore())
                };

                for (String r : rows) {
                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText(r);
                    cs.endText();
                    y -= 16;
                }

                y -= 15;

                // Match Details & Skills
                MatchDetail d = match.getDetails();
                if (d != null) {
                    cs.beginText();
                    cs.setFont(fontBold, 11);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Skill Gap Analysis:");
                    cs.endText();
                    y -= 18;

                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText("Matched Required Skills: " + sanitize(MatchDetail.toCommaString(d.getMatchedRequiredSkills())));
                    cs.endText();
                    y -= 16;

                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.setNonStrokingColor(220, 38, 38); // Red
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText("Missing Required Skills: " + sanitize(MatchDetail.toCommaString(d.getMissingRequiredSkills())));
                    cs.endText();
                    cs.setNonStrokingColor(15, 23, 42);
                    y -= 16;

                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText("Matched Preferred Skills: " + sanitize(MatchDetail.toCommaString(d.getMatchedPreferredSkills())));
                    cs.endText();
                    y -= 16;

                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.setNonStrokingColor(217, 119, 6); // Amber
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText("Missing Preferred Skills: " + sanitize(MatchDetail.toCommaString(d.getMissingPreferredSkills())));
                    cs.endText();
                    cs.setNonStrokingColor(15, 23, 42);
                    y -= 25;

                    // Qualifications
                    cs.beginText();
                    cs.setFont(fontBold, 11);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Experience & Educational Compatibility:");
                    cs.endText();
                    y -= 18;

                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText("Experience: " + sanitize(d.getExperienceCompatibility()));
                    cs.endText();
                    y -= 16;

                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(margin + 15, y);
                    cs.showText("Education: " + sanitize(d.getEducationCompatibility()));
                    cs.endText();
                    y -= 25;

                    // Recommendations
                    if (d.getRecommendations() != null && !d.getRecommendations().isBlank()) {
                        cs.beginText();
                        cs.setFont(fontBold, 11);
                        cs.newLineAtOffset(margin, y);
                        cs.showText("Recruiter Recommendations:");
                        cs.endText();
                        y -= 18;

                        cs.beginText();
                        cs.setFont(fontOblique, 10);
                        cs.newLineAtOffset(margin + 15, y);
                        cs.showText(sanitize(d.getRecommendations()));
                        cs.endText();
                        y -= 25;
                    }
                }

                // Footer
                cs.beginText();
                cs.setFont(fontRegular, 8);
                cs.setNonStrokingColor(100, 116, 139);
                cs.newLineAtOffset(margin, 40);
                cs.showText("Generated by TalentIQ – Deterministic Rule-Based Resume & Skill Matching System");
                cs.endText();
            }

            doc.save(targetFile);
        }
    }

    private static String sanitize(String text) {
        if (text == null) return "None";
        // Clean non-ASCII characters that standard Helvetica doesn't support
        return text.replaceAll("[^\\x20-\\x7E]", " ").replaceAll("\\s+", " ").trim();
    }
}

