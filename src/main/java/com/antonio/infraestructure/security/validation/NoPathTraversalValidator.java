package com.antonio.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import com.antonio.infrastructure.security.validation.NoPathTraversal;

class NoPathTraversalValidator implements ConstraintValidator<NoPathTraversal, String> {
    
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        ".*(\\.\\./|\\.\\\\|%2e%2e|%252e%252e).*",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        
        return !PATH_TRAVERSAL_PATTERN.matcher(value).matches();
    }
}