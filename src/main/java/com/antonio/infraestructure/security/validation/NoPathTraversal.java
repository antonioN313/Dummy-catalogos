package com.antonio.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoPathTraversalValidator.class)
@Documented
public @interface NoPathTraversal {
    String message() default "Potential path traversal attack detected";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}