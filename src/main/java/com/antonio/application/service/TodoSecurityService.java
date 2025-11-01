package com.antonio.application.service;

import com.antonio.application.service.ProductService;
import com.antonio.domain.model.Product;
import com.antonio.domain.port.ProductRepository;
import com.antonio.infrastructure.security.audit.Auditable;
import com.antonio.infrastructure.security.audit.AuditLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service("todoSecurityService")
@RequiredArgsConstructor
class TodoSecurityService {
    
    private final TodoRepository todoRepository;

    /**
     * Verifica se usuário autenticado pode modificar o todo
     */
    public boolean canModifyTodo(int todoId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("⚠️ Unauthenticated access attempt to todo: {}", todoId);
            return false;
        }
        
        try {
            Optional<Todo> todoOpt = todoRepository.findById(todoId);
            
            if (todoOpt.isEmpty()) {
                log.warn("⚠️ Todo not found: {}", todoId);
                return false;
            }
            
            Todo todo = todoOpt.get();
            
            // Extrai ID do usuário do principal
            // (assumindo que o principal tem um método getId())
            Object principal = authentication.getPrincipal();
            Integer userId = extractUserId(principal);
            
            boolean canModify = todo.getUserId().equals(userId);
            
            if (!canModify) {
                log.warn("🚫 User {} attempted to modify todo {} owned by user {}", 
                         userId, todoId, todo.getUserId());
            }
            
            return canModify;
            
        } catch (Exception e) {
            log.error("❌ Error checking todo ownership", e);
            return false;
        }
    }

    /**
     * Extrai ID do usuário do principal
     */
    private Integer extractUserId(Object principal) {
        // Implementação depende do tipo de UserDetails usado
        // Exemplo simplificado:
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // Assumindo que username contém o ID ou podemos buscar do banco
            return 1; // Placeholder
        }
        return null;
    }
}