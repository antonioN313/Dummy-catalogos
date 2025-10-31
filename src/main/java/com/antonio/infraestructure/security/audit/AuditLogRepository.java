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
class AuditLogRepository {
    
    private final Map<Long, AuditLog> store = new ConcurrentHashMap<>();
    private Long currentId = 0L;

    public AuditLog save(AuditLog auditLog) {
        if (auditLog.getId() == null) {
            auditLog.setId(++currentId);
        }
        
        store.put(auditLog.getId(), auditLog);
        
        // Log estruturado para análise externa (SIEM, ELK, etc)
        logStructured(auditLog);
        
        return auditLog;
    }

    /**
     * 📊 Log estruturado (formato JSON para SIEM)
     */
    private void logStructured(AuditLog auditLog) {
        String jsonLog = String.format(
            "{\"timestamp\":\"%s\", \"username\":\"%s\", \"ip\":\"%s\", " +
            "\"action\":\"%s\", \"status\":\"%s\", \"duration\":%d, \"level\":\"%s\"}",
            auditLog.getTimestamp(),
            auditLog.getUsername(),
            auditLog.getIpAddress(),
            auditLog.getAction(),
            auditLog.getStatus(),
            auditLog.getDurationMs(),
            auditLog.getLevel()
        );
        
        switch (auditLog.getLevel()) {
            case CRITICAL -> log.error("AUDIT_CRITICAL: {}", jsonLog);
            case ERROR -> log.error("AUDIT_ERROR: {}", jsonLog);
            case WARN -> log.warn("AUDIT_WARN: {}", jsonLog);
            default -> log.info("AUDIT_INFO: {}", jsonLog);
        }
    }

    public java.util.List<AuditLog> findAll() {
        return new java.util.ArrayList<>(store.values());
    }

    public java.util.List<AuditLog> findByUsername(String username) {
        return store.values().stream()
            .filter(log -> username.equals(log.getUsername()))
            .toList();
    }

    public java.util.List<AuditLog> findByAction(String action) {
        return store.values().stream()
            .filter(log -> action.equals(log.getAction()))
            .toList();
    }
}