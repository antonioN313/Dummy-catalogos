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
public class IpWhitelistFilter extends OncePerRequestFilter {

    // IPs e CIDR ranges permitidos (configurar via properties em produção)
    private static final String[] ALLOWED_IPS = {
        "127.0.0.1",           // localhost
        "::1",                 // localhost IPv6
        "192.168.1.0/24",      // rede local
        "10.0.0.0/8"           // rede privada
    };
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String endpoint = request.getRequestURI();
        
        // Aplicar filtro apenas para endpoints /admin
        if (endpoint.startsWith("/api/admin/")) {
            String clientIp = getClientIP(request);
            
            if (!isIpAllowed(clientIp)) {
                log.warn("🚫 IP BLOCKED - Unauthorized IP {} attempted to access admin endpoint: {}", 
                         clientIp, endpoint);
                
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\": \"Forbidden\", " +
                    "\"message\": \"Access denied from your IP address\"}"
                );
                return;
            }
            
            log.info("✅ IP ALLOWED - {} accessing {}", clientIp, endpoint);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Verifica se IP está na whitelist
     */
    private boolean isIpAllowed(String ip) {
        for (String allowed : ALLOWED_IPS) {
            if (allowed.contains("/")) {
                // CIDR notation
                if (ipMatchesCIDR(ip, allowed)) {
                    return true;
                }
            } else {
                // IP exato
                if (ip.equals(allowed)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifica se IP está dentro do range CIDR
     */
    private boolean ipMatchesCIDR(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            long ipLong = ipToLong(ip);
            long networkLong = ipToLong(networkAddress);
            long mask = -1L << (32 - prefixLength);
            
            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.error("Error parsing CIDR: {}", cidr, e);
            return false;
        }
    }
    
    /**
     * Converte IP string para long
     */
    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += Long.parseLong(octets[i]) << (24 - (8 * i));
        }
        return result;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}