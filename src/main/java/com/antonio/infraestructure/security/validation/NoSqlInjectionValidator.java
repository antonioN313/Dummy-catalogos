package com.antonio.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import com.antonio.infrastructure.security.validation.NoSQLInjection;

class NoSQLInjectionValidator implements ConstraintValidator<NoSQLInjection, String> {
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(\\bUNION\\b|\\bSELECT\\b|\\bDROP\\b|\\bINSERT\\b|\\bUPDATE\\b|\\bDELETE\\b|--|;|/\\*|\\*/|xp_|sp_).*",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        
        return !SQL_INJECTION_PATTERN.matcher(value).matches();
    }
}