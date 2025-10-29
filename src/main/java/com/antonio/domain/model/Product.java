package com.antonio.domain.model;

import com.antonio.infrastructure.security.validation.NoSQLInjection;
import com.antonio.infrastructure.security.validation.NoXSS;
import com.antonio.infrastructure.security.validation.SafeText;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Positive(message = "Product ID must be positive")
    private Integer id;
    
    @NotBlank(message = "Product title cannot be empty")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @NoSQLInjection
    @NoXSS
    @SafeText
    private String title;
    
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    private Double price;
}
