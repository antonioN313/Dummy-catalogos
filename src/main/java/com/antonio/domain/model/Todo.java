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
public class Todo {
    
    @Positive(message = "Todo ID must be positive")
    private Integer id;
    
    @NotBlank(message = "Todo text cannot be empty")
    @Size(min = 3, max = 500, message = "Todo must be between 3 and 500 characters")
    @NoSQLInjection
    @NoXSS
    @SafeText(pattern = "^[a-zA-Z0-9\\s.,!?@#&()\\-:]+$")
    private String todo;
    
    @NotNull(message = "Completed status cannot be null")
    private Boolean completed;
    
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Integer userId;
}