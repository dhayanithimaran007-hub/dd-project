package com.talentiq.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Text extraction utility for PDF documents using Apache PDFBox 3.x.
 * Pure conventional text extraction; does not use AI or NLP services.
 */
public class PDFTextExtractor {

    private static final Logger LOGGER = Logger.getLogger(PDFTextExtractor.class.getName());

    private PDFTextExtractor() {}

    /**
     * Extracts all text from a given PDF file.
     *
     * @param file PDF file on disk
     * @return extracted plain text
     * @throws IOException if the file cannot be read or parsed
     */
    public static String extractText(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("PDF file does not exist");
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to extract text from PDF file: " + file.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Extracts text from an input stream containing PDF bytes.
     */
    public static String extractText(byte[] pdfBytes) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return "";
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        }
    }
}

