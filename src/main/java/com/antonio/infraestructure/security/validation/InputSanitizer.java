package com.antonio.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

class InputSanitizer {

    public static String escapeHtml(String input) {
        if (input == null) return null;
        return StringEscapeUtils.escapeHtml4(input);
    }

    public static String escapeSql(String input) {
        if (input == null) return null;
        return input.replace("'", "''")
                    .replace("\"", "\"\"")
                    .replace("\\", "\\\\");
    }

    public static String removeDangerousChars(String input) {
        if (input == null) return null;
        return input.replaceAll("[<>\"'%;)(&+]", "");
    }

    public static String normalizeWhitespace(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }

    public static String sanitize(String input) {
        if (input == null) return null;
        
        String sanitized = input;
        sanitized = normalizeWhitespace(sanitized);
        sanitized = removeDangerousChars(sanitized);
        sanitized = escapeHtml(sanitized);
        
        return sanitized;
    }
}