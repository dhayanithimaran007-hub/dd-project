package com.talentiq.util;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Text extraction utility for Microsoft Word DOCX files using Apache POI.
 * Traditional text extraction; does not use AI or NLP services.
 */
public class DocxTextExtractor {

    private static final Logger LOGGER = Logger.getLogger(DocxTextExtractor.class.getName());

    private DocxTextExtractor() {}

    /**
     * Extracts text content from a DOCX file.
     *
     * @param file DOCX file
     * @return extracted plain text
     * @throws IOException on read error
     */
    public static String extractText(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("DOCX file does not exist");
        }

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to extract text from DOCX file: " + file.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Extracts text from an input stream.
     */
    public static String extractText(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        try (XWPFDocument doc = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        }
    }
}

