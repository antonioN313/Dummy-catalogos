package com.antonio.infrastructure.security.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.passay.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TwoFactorAuthService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    
    // Cache de secrets por usuário (em produção, usar banco de dados)
    private final Map<String, String> userSecrets = new ConcurrentHashMap<>();
    
    // Cache de backup codes por usuário
    private final Map<String, Set<String>> userBackupCodes = new ConcurrentHashMap<>();

    /**
     * 📱 Gera secret para novo usuário
     */
    public String generateSecret(String username) {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();
        
        userSecrets.put(username, secret);
        
        log.info("🔑 2FA Secret generated for user: {}", username);
        return secret;
    }

    /**
     * 📸 Gera URL para QR Code
     */
    public String generateQRCodeUrl(String username, String secret) {
        String issuer = "DummyJSON-App";
        
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            issuer,
            username,
            new GoogleAuthenticatorKey.Builder(secret).build()
        );
    }

    /**
     * ✅ Verifica código TOTP
     */
    public boolean verifyCode(String username, int code) {
        String secret = userSecrets.get(username);
        
        if (secret == null) {
            log.warn("⚠️ No 2FA secret found for user: {}", username);
            return false;
        }
        
        boolean valid = gAuth.authorize(secret, code);
        
        if (valid) {
            log.info("✅ 2FA verification successful for user: {}", username);
        } else {
            log.warn("❌ 2FA verification failed for user: {}", username);
        }
        
        return valid;
    }

    /**
     * 🔐 Gera códigos de backup
     */
    public Set<String> generateBackupCodes(String username) {
        Set<String> backupCodes = new HashSet<>();
        Random random = new Random();
        
        // Gera 10 códigos de 8 dígitos
        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            backupCodes.add(code);
        }
        
        userBackupCodes.put(username, backupCodes);
        
        log.info("🔑 Backup codes generated for user: {}", username);
        return backupCodes;
    }

    /**
     * 🔓 Verifica código de backup
     */
    public boolean verifyBackupCode(String username, String code) {
        Set<String> codes = userBackupCodes.get(username);
        
        if (codes == null || !codes.contains(code)) {
            return false;
        }
        
        // Remove código usado (uso único)
        codes.remove(code);
        
        log.info("✅ Backup code used for user: {}", username);
        return true;
    }

    /**
     * 🗑️ Desabilita 2FA para usuário
     */
    public void disable2FA(String username) {
        userSecrets.remove(username);
        userBackupCodes.remove(username);
        
        log.info("🔓 2FA disabled for user: {}", username);
    }

    /**
     * 📊 Verifica se usuário tem 2FA habilitado
     */
    public boolean is2FAEnabled(String username) {
        return userSecrets.containsKey(username);
    }
}