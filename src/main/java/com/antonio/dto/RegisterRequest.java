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
class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
    @NoSQLInjection
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 12, max = 128)
    // Validação de senha forte será feita por serviço separado
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    @NoXSS
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    @NoXSS
    private String lastName;
}