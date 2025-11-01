package com.antonio.infrastructure.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TwoFactorAuthFilter extends OncePerRequestFilter {

    // Endpoints que não requerem 2FA
    private static final String[] EXCLUDED_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/2fa/verify",
        "/api/public/"
    };
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String endpoint = request.getRequestURI();
        
        // Verificar se endpoint requer 2FA
        if (shouldExclude(endpoint)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Se usuário autenticado mas 2FA não verificado
        if (auth != null && auth.isAuthenticated() && !is2FAVerified(auth)) {
            log.warn("🔒 2FA NOT VERIFIED - User {} attempting to access protected resource", 
                     auth.getName());
            
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"2FA Required\", " +
                "\"message\": \"Two-factor authentication verification required\", " +
                "\"redirectTo\": \"/api/auth/2fa/verify\"}"
            );
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Verifica se endpoint deve ser excluído da verificação 2FA
     */
    private boolean shouldExclude(String endpoint) {
        for (String excluded : EXCLUDED_PATHS) {
            if (endpoint.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica se 2FA foi validado
     * (Em produção, verificar atributo na sessão ou JWT claim)
     */
    private boolean is2FAVerified(Authentication auth) {
        // Implementação: verificar se usuário tem atributo "2fa_verified" na sessão
        // ou se o JWT contém claim "2fa_verified: true"
        
        // Exemplo simplificado:
        Object twoFactorVerified = auth.getDetails();
        return twoFactorVerified != null && 
               twoFactorVerified.toString().contains("2fa_verified=true");
    }
}