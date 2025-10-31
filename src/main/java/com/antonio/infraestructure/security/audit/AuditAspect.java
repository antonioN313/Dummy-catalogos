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
@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 🎯 Intercepta métodos com @Auditable
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Captura contexto
        String username = getCurrentUsername();
        String ipAddress = getClientIP();
        String method = joinPoint.getSignature().toShortString();
        Object[] args = maskSensitiveData(joinPoint.getArgs());
        
        AuditLog auditLog = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .username(username)
            .ipAddress(ipAddress)
            .action(auditable.action())
            .description(auditable.description())
            .method(method)
            .arguments(Arrays.toString(args))
            .level(auditable.level())
            .build();
        
        try {
            // Executa método original
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setStatus("SUCCESS");
            auditLog.setDurationMs(duration);
            auditLog.setResult(maskSensitiveData(result));
            
            log.info("✅ AUDIT [{}] - User: {}, Action: {}, Duration: {}ms", 
                     auditable.level(), username, auditable.action(), duration);
            
            auditLogRepository.save(auditLog);
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setStatus("FAILURE");
            auditLog.setDurationMs(duration);
            auditLog.setErrorMessage(e.getMessage());
            auditLog.setLevel(AuditLevel.ERROR);
            
            log.error("❌ AUDIT [ERROR] - User: {}, Action: {}, Error: {}", 
                      username, auditable.action(), e.getMessage());
            
            auditLogRepository.save(auditLog);
            
            throw e;
        }
    }

    /**
     * 🔐 Mascara dados sensíveis
     */
    private Object[] maskSensitiveData(Object[] args) {
        if (args == null) return null;
        
        Object[] masked = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            
            if (arg == null) {
                masked[i] = null;
            } else if (arg.toString().toLowerCase().contains("password")) {
                masked[i] = "***MASKED***";
            } else if (arg.toString().toLowerCase().contains("token")) {
                masked[i] = "***MASKED***";
            } else {
                masked[i] = arg.toString();
            }
        }
        return masked;
    }

    private Object maskSensitiveData(Object result) {
        if (result == null) return null;
        
        String resultStr = result.toString();
        if (resultStr.toLowerCase().contains("password") || 
            resultStr.toLowerCase().contains("token")) {
            return "***MASKED***";
        }
        
        return resultStr;
    }

    /**
     * 👤 Obtém username do contexto de segurança
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    /**
     * 🌐 Obtém IP do cliente
     */
    private String getClientIP() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("Could not retrieve client IP", e);
        }
        return "unknown";
    }
}