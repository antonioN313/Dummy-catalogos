package com.antonio.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import com.antonio.infrastructure.security.validation.SafeText;

class SafeTextValidator implements ConstraintValidator<SafeText, String> {
    
    private Pattern pattern;
    
    @Override
    public void initialize(SafeText constraintAnnotation) {
        this.pattern = Pattern.compile(constraintAnnotation.pattern());
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        
        return pattern.matcher(value).matches();
    }
}