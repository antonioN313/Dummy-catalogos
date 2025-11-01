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
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> ipBucketCache = new ConcurrentHashMap<>();
    
    private static final int DEFAULT_CAPACITY = 50;
    private static final Duration DEFAULT_REFILL = Duration.ofMinutes(1);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIP(request);
        String endpoint = request.getRequestURI();
        
        // Rate limit mais restritivo para endpoints sensíveis
        Bucket bucket = resolveBucket(clientIp, endpoint);
        
        if (bucket.tryConsume(1)) {
            // Adiciona headers informativos
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit excedido
            log.warn("⚠️ RATE LIMIT EXCEEDED - IP: {}, Endpoint: {}", clientIp, endpoint);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Too many requests\", " +
                "\"message\": \"Rate limit exceeded. Please try again later.\", " +
                "\"retryAfter\": 60}"
            );
        }
    }
    
    private Bucket resolveBucket(String ip, String endpoint) {
        String key = ip + ":" + endpoint;
        
        return ipBucketCache.computeIfAbsent(key, k -> {
            
            if (endpoint.contains("/auth/") || endpoint.contains("/login")) {
                return createBucket(10, Duration.ofMinutes(5)); 
            }
            
            
            return createBucket(DEFAULT_CAPACITY, DEFAULT_REFILL);
        });
    }
    
    private Bucket createBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(
            capacity,
            Refill.intervally(capacity, refillDuration)
        );
        return Bucket.builder().addLimit(limit).build();
    }
    
    
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
       
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
}