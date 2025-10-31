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
public class PasswordPolicyService {

    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;
    
    // Histórico de senhas por usuário (em produção, usar banco)
    private final Map<String, List<String>> passwordHistory = new ConcurrentHashMap<>();
    
    private static final int PASSWORD_HISTORY_LIMIT = 5; // Últimas 5 senhas

    public PasswordPolicyService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = createPasswordValidator();
    }

    /**
     * 🔐 Cria validador de senha com regras rigorosas
     */
    private PasswordValidator createPasswordValidator() {
        return new PasswordValidator(Arrays.asList(
            // Comprimento: mínimo 12, máximo 128 caracteres
            new LengthRule(12, 128),
            
            // Pelo menos 1 letra maiúscula
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            
            // Pelo menos 1 letra minúscula
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            
            // Pelo menos 1 dígito
            new CharacterRule(EnglishCharacterData.Digit, 1),
            
            // Pelo menos 1 caractere especial
            new CharacterRule(EnglishCharacterData.Special, 1),
            
            // Não permitir sequências alfabéticas (abc, xyz)
            new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 3, false),
            
            // Não permitir sequências numéricas (123, 789)
            new IllegalSequenceRule(EnglishSequenceData.Numerical, 3, false),
            
            // Não permitir sequências de teclado (qwerty, asdf)
            new IllegalSequenceRule(EnglishSequenceData.USQwerty, 3, false),
            
            // Não permitir repetição excessiva de caracteres (aaa, 111)
            new RepeatCharacterRegexRule(3),
            
            // Verificar contra dicionário de senhas comuns
            new DictionaryRule(createCommonPasswordDictionary()),
            
            // Não permitir espaços em branco
            new WhitespaceRule()
        ));
    }

    /**
     * 📚 Cria dicionário de senhas comuns
     */
    private Dictionary createCommonPasswordDictionary() {
        // Top 100 senhas mais comuns
        List<String> commonPasswords = Arrays.asList(
            "password", "123456", "12345678", "qwerty", "abc123",
            "monkey", "1234567", "letmein", "trustno1", "dragon",
            "baseball", "iloveyou", "master", "sunshine", "ashley",
            "bailey", "passw0rd", "shadow", "123123", "654321",
            "superman", "qazwsx", "michael", "football", "password1",
            "welcome", "jesus", "ninja", "mustang", "password123",
            "admin", "root", "toor", "pass", "test"
        );
        
        return new WordListDictionary(new ArrayWordList(
            commonPasswords.toArray(new String[0])
        ));
    }

    /**
     * ✅ Valida senha contra políticas
     */
    public void validatePassword(String password, String username) {
        PasswordData passwordData = new PasswordData(password);
        passwordData.setUsername(username);
        
        RuleResult result = passwordValidator.validate(passwordData);
        
        if (!result.isValid()) {
            List<String> messages = passwordValidator.getMessages(result);
            String errorMessage = String.join("; ", messages);
            
            log.warn("❌ Password validation failed for user {}: {}", username, errorMessage);
            throw new WeakPasswordException(errorMessage);
        }
        
        log.info("✅ Password validation successful for user: {}", username);
    }

    /**
     * 🔄 Verifica se senha não foi usada recentemente
     */
    public void checkPasswordHistory(String username, String newPassword) {
        List<String> history = passwordHistory.getOrDefault(username, new ArrayList<>());
        
        for (String oldHashedPassword : history) {
            if (passwordEncoder.matches(newPassword, oldHashedPassword)) {
                log.warn("⚠️ Password reuse detected for user: {}", username);
                throw new PasswordReusedException(
                    "Password was recently used. Please choose a different password."
                );
            }
        }
        
        log.info("✅ Password history check passed for user: {}", username);
    }

    /**
     * 💾 Adiciona senha ao histórico
     */
    public void addToPasswordHistory(String username, String hashedPassword) {
        List<String> history = passwordHistory.getOrDefault(username, new ArrayList<>());
        
        history.add(0, hashedPassword); // Adiciona no início
        
        // Mantém apenas as últimas N senhas
        if (history.size() > PASSWORD_HISTORY_LIMIT) {
            history = history.subList(0, PASSWORD_HISTORY_LIMIT);
        }
        
        passwordHistory.put(username, history);
        
        log.info("💾 Password added to history for user: {}", username);
    }

    /**
     * 🔐 Valida e salva nova senha (completo)
     */
    public String validateAndEncodePassword(String password, String username) {
        // 1. Valida contra políticas
        validatePassword(password, username);
        
        // 2. Codifica senha
        String hashedPassword = passwordEncoder.encode(password);
        
        // 3. Verifica histórico
        checkPasswordHistory(username, password);
        
        // 4. Adiciona ao histórico
        addToPasswordHistory(username, hashedPassword);
        
        return hashedPassword;
    }

    /**
     * 📊 Calcula força da senha (0-100)
     */
    public int calculatePasswordStrength(String password) {
        int strength = 0;
        
        if (password.length() >= 12) strength += 20;
        if (password.length() >= 16) strength += 10;
        if (password.matches(".*[A-Z].*")) strength += 15;
        if (password.matches(".*[a-z].*")) strength += 15;
        if (password.matches(".*\\d.*")) strength += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 15;
        if (!password.matches(".*(\\w)\\1{2,}.*")) strength += 10; // Sem repetições
        
        return Math.min(strength, 100);
    }
}