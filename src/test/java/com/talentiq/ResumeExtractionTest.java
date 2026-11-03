package com.talentiq;

import com.talentiq.util.DocxTextExtractor;
import com.talentiq.util.PDFTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Resume Extraction & File Generation Tests")
public class ResumeExtractionTest {

    @Test
    @DisplayName("Generate and extract text from sample PDF using PDFBox")
    public void testPdfGenerationAndExtraction() throws IOException {
        File dir = new File("sample_resumes");
        if (!dir.exists()) dir.mkdirs();

        File pdfFile = new File(dir, "resume_john_doe.pdf");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(bold, 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("Johnathan Doe - Senior Java Backend Engineer");
                cs.endText();

                cs.beginText();
                cs.setFont(regular, 10);
                cs.newLineAtOffset(50, 720);
                cs.showText("Email: john.doe@email.com | Phone: +1-555-0201 | San Francisco, CA");
                cs.endText();

                cs.beginText();
                cs.setFont(regular, 10);
                cs.newLineAtOffset(50, 690);
                cs.showText("Summary: 5.5 years of experience building microservices with Java, Spring Boot, MySQL, and Docker.");
                cs.endText();

                cs.beginText();
                cs.setFont(regular, 10);
                cs.newLineAtOffset(50, 660);
                cs.showText("Education: Master of Science in Computer Science - Stanford University.");
                cs.endText();
            }
            doc.save(pdfFile);
        }

        assertTrue(pdfFile.exists());
        String extracted = PDFTextExtractor.extractText(pdfFile);
        assertNotNull(extracted);
        assertTrue(extracted.contains("Johnathan Doe"));
        assertTrue(extracted.contains("john.doe@email.com"));
        assertTrue(extracted.contains("Java"));
        assertTrue(extracted.contains("Spring Boot"));
    }

    @Test
    @DisplayName("Generate and extract text from sample DOCX using Apache POI")
    public void testDocxGenerationAndExtraction() throws IOException {
        File dir = new File("sample_resumes");
        if (!dir.exists()) dir.mkdirs();

        File docxFile = new File(dir, "resume_sarah_connor.docx");

        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(docxFile)) {

            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun rTitle = title.createRun();
            rTitle.setText("Sarah Connor - Full Stack Java and Cloud Specialist");
            rTitle.setBold(true);

            XWPFParagraph contact = doc.createParagraph();
            XWPFRun rContact = contact.createRun();
            rContact.setText("Email: sarah.connor@email.com | Phone: +1-555-0301 | Los Angeles, CA");

            XWPFParagraph summary = doc.createParagraph();
            XWPFRun rSummary = summary.createRun();
            rSummary.setText("Software engineer with 4.0 years of experience in Java, Spring Boot, React, MySQL, Docker, AWS, and Git. Bachelor of Science in Computer Science.");

            doc.write(fos);
        }

        assertTrue(docxFile.exists());
        String extracted = DocxTextExtractor.extractText(docxFile);
        assertNotNull(extracted);
        assertTrue(extracted.contains("Sarah Connor"));
        assertTrue(extracted.contains("sarah.connor@email.com"));
        assertTrue(extracted.contains("Java"));
        assertTrue(extracted.contains("React"));
    }
}

