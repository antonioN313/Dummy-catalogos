package com.antonio.infrastructure.security.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class DataEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    private final SecretKey secretKey;

    public DataEncryptionService(
        @Value("${encryption.key:mySecretEncryptionKey1234567890}") String encryptionKey
    ) {
        // Gera chave de 256 bits a partir da string
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32]; // 256 bits
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));
        
        this.secretKey = new SecretKeySpec(key, "AES");
        
        log.info("🔐 Data Encryption Service initialized with AES-256-GCM");
    }

    /**
     * 🔒 Criptografa texto
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // Gera IV aleatório
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Configura cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Criptografa
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combina IV + CipherText
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            
            // Retorna em Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            log.error("❌ Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * 🔓 Descriptografa texto
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            // Decodifica Base64
            byte[] cipherMessage = Base64.getDecoder().decode(encryptedText);
            
            // Extrai IV e CipherText
            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            
            // Configura cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Descriptografa
            byte[] plainText = cipher.doFinal(cipherText);
            
            return new String(plainText, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("❌ Decryption failed", e);
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * 🎭 Mascara dados sensíveis (para logs)
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        String maskedUsername = username.length() > 2 
            ? username.substring(0, 2) + "***" 
            : "***";
        
        return maskedUsername + "@" + domain;
    }

    /**
     * 🎭 Mascara CPF
     */
    public String maskCPF(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        
        return "***.***.***-" + cpf.substring(cpf.length() - 2);
    }

    /**
     * 🎭 Mascara cartão de crédito
     */
    public String maskCreditCard(String card) {
        if (card == null || card.length() < 4) {
            return "****";
        }
        
        return "**** **** **** " + card.substring(card.length() - 4);
    }

    /**
     * 🎭 Mascara telefone
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        
        return "(**) ****-" + phone.substring(phone.length() - 4);
    }

    /**
     * 🔐 Hash de dados (one-way)
     */
    public String hash(String data) {
        if (data == null) return null;
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new EncryptionException("Failed to hash data", e);
        }
    }
}