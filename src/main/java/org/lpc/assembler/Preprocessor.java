package org.lpc.assembler;

import java.util.ArrayList;
import java.util.List;

public class Preprocessor {

    public String preprocess(String source) {
        List<String> lines = new ArrayList<>();

        for (String line : source.split("\n")) {
            String processed = preprocessLine(line);
            if (!processed.isEmpty()) {
                lines.add(processed);
            }
        }

        return String.join("\n", lines);
    }

    private String preprocessLine(String line) {
        // Remove comments (both ; and # style)
        String withoutComments = removeComments(line);

        // Normalize whitespace
        String normalized = normalizeWhitespace(withoutComments);

        // Skip empty lines
        if (normalized.trim().isEmpty()) {
            return "";
        }

        return normalized;
    }

    private String removeComments(String line) {
        int semicolon = line.indexOf(';');
        int hash = line.indexOf('#');

        int commentStart = -1;
        if (semicolon != -1 && hash != -1) {
            commentStart = Math.min(semicolon, hash);
        } else if (semicolon != -1) {
            commentStart = semicolon;
        } else if (hash != -1) {
            commentStart = hash;
        }

        if (commentStart != -1) {
            return line.substring(0, commentStart);
        }

        return line;
    }

    private String normalizeWhitespace(String line) {
        // Replace multiple spaces/tabs with single spaces
        return line.replaceAll("\\s+", " ").trim();
    }
}