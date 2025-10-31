package com.antonio.infrastructure.security.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
class AuthenticationEventListener implements ApplicationListener<AbstractAuthenticationEvent> {

    private final AuditLogRepository auditLogRepository;

    public AuthenticationEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            handleSuccessfulLogin((AuthenticationSuccessEvent) event);
        } else if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            handleFailedLogin((AuthenticationFailureBadCredentialsEvent) event);
        } else if (event instanceof AuthenticationFailureLockedEvent) {
            handleAccountLocked((AuthenticationFailureLockedEvent) event);
        } else if (event instanceof InteractiveAuthenticationSuccessEvent) {
            // Ignora (já tratado por AuthenticationSuccessEvent)
        } else {
            handleOtherAuthEvent(event);
        }
    }

    /**
     * ✅ Login bem-sucedido
     */
    private void handleSuccessfulLogin(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth.getName();
        String ip = getIpAddress(auth);
        
        AuditLog auditLog = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .username(username)
            .ipAddress(ip)
            .action("LOGIN_SUCCESS")
            .description("User successfully logged in")
            .status("SUCCESS")
            .level(AuditLevel.INFO)
            .build();
        
        auditLogRepository.save(auditLog);
        
        log.info("✅ SECURITY_EVENT: Successful login - User: {}, IP: {}", username, ip);
    }

    /**
     * ❌ Falha de login (credenciais inválidas)
     */
    private void handleFailedLogin(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getIpAddress(event.getAuthentication());
        
        AuditLog auditLog = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .username(username)
            .ipAddress(ip)
            .action("LOGIN_FAILED")
            .description("Failed login attempt - Bad credentials")
            .errorMessage(event.getException().getMessage())
            .status("FAILURE")
            .level(AuditLevel.WARN)
            .build();
        
        auditLogRepository.save(auditLog);
        
        log.warn("⚠️ SECURITY_EVENT: Failed login - User: {}, IP: {}", username, ip);
    }

    /**
     * 🔒 Conta bloqueada
     */
    private void handleAccountLocked(AuthenticationFailureLockedEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getIpAddress(event.getAuthentication());
        
        AuditLog auditLog = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .username(username)
            .ipAddress(ip)
            .action("ACCOUNT_LOCKED")
            .description("Login attempt on locked account")
            .errorMessage("Account is locked")
            .status("FAILURE")
            .level(AuditLevel.ERROR)
            .build();
        
        auditLogRepository.save(auditLog);
        
        log.error("🚨 SECURITY_EVENT: Locked account access attempt - User: {}, IP: {}", username, ip);
    }

    /**
     * 🔍 Outros eventos de autenticação
     */
    private void handleOtherAuthEvent(AbstractAuthenticationEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth != null ? auth.getName() : "unknown";
        String ip = getIpAddress(auth);
        
        log.info("ℹ️ SECURITY_EVENT: {} - User: {}, IP: {}", 
                 event.getClass().getSimpleName(), username, ip);
    }

    /**
     * 🌐 Extrai IP do objeto de autenticação
     */
    private String getIpAddress(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof WebAuthenticationDetails) {
            return ((WebAuthenticationDetails) auth.getDetails()).getRemoteAddress();
        }
        return "unknown";
    }
}