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
public class User {
    
    @Positive(message = "User ID must be positive")
    private Integer id;
    
    @NotBlank(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @NoXSS
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;
    
    @NotBlank(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @NoXSS
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    @NoSQLInjection
    private String username;
    
    // Senha não é exposta em JSON (tratada separadamente)
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
    private String password;
    
    @NotNull(message = "Enabled status cannot be null")
    private Boolean enabled = true;
    
    @NotNull(message = "Account non-expired status cannot be null")
    private Boolean accountNonExpired = true;
    
    @NotNull(message = "Account non-locked status cannot be null")
    private Boolean accountNonLocked = true;
    
    @NotNull(message = "Credentials non-expired status cannot be null")
    private Boolean credentialsNonExpired = true;
}