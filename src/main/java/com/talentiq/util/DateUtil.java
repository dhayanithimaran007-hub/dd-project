package com.talentiq.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) return "N/A";
        return ts.toLocalDateTime().format(DATETIME_FORMATTER);
    }

    public static String formatScore(double score) {
        return String.format(java.util.Locale.US, "%.1f%%", score);
    }
}

