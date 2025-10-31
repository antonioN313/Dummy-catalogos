package com.antonio.dto;

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
class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    @NoSQLInjection
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 128)
    private String password;
    
    // Campo opcional para "Lembrar-me"
    private Boolean rememberMe = false;
}