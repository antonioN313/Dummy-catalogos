package com.antonio.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import com.antonio.infrastructure.security.validation.NoXSS;

class NoXSSValidator implements ConstraintValidator<NoXSS, String> {
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        ".*(<script|javascript:|onerror=|onload=|<iframe|<object|<embed).*",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        
        return !XSS_PATTERN.matcher(value).matches();
    }
}