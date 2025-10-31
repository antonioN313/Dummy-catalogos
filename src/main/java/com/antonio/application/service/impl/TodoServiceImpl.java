package com.antonio.application.service.impl;

import com.antonio.application.service.TodoService;
import com.antonio.domain.model.Todo;
import com.antonio.domain.port.TodoRepository;
import lombok.AllArgsConstructor;

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
import java.util.Optional;


@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository repository;

    @Override
    @PreAuthorize("isAuthenticated()")
    @PostFilter("filterObject.userId == authentication.principal.id or hasRole('ADMIN')")
    @Auditable(
            action = "LIST_TODOS",
            description = "User listed todos",
            level = AuditLevel.INFO
    )
    public List<Todo> list(
            @Positive int limit,
            @Positive int skip
    ) throws IOException {

        log.info("📋 Listing todos - limit: {}, skip: {}", limit, skip);

        if (limit > 100) {
            limit = 100;
        }

        List<Todo> todos = repository.list(limit, skip);

        log.info("✅ Retrieved {} todos", todos.size());
        return todos;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Auditable(
            action = "ADD_TODO",
            description = "User created new todo",
            level = AuditLevel.INFO
    )
    public Todo add(
            @Valid String text,
            @Positive int userId
    ) throws IOException {

        log.info("➕ Adding new todo for user: {}", userId);

        // Validações
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo text cannot be empty");
        }

        if (text.length() < 3) {
            throw new IllegalArgumentException("Todo text must have at least 3 characters");
        }

        if (text.length() > 500) {
            throw new IllegalArgumentException("Todo text cannot exceed 500 characters");
        }

        Todo todo = new Todo(
                null,
                text.trim(),
                false,
                userId
        );

        Todo created = repository.add(todo);

        log.info("✅ Todo created with ID: {}", created.getId());
        return created;
    }

    @Override
    @PreAuthorize("@todoSecurityService.canModifyTodo(#id, authentication) or hasRole('ADMIN')")
    @Auditable(
            action = "TOGGLE_TODO",
            description = "User toggled todo completion status",
            level = AuditLevel.INFO
    )
    public Todo toggle(
            @Positive int id,
            boolean completed
    ) throws IOException {

        log.info("✔️ Toggling todo {} to completed={}", id, completed);

        List<Todo> maybe = repository.findById(id);

        if (maybe.isEmpty()) {
            log.warn("⚠️ Todo not found: {}", id);
            throw new IllegalArgumentException("Todo not found: " + id);
        }

        Todo todo = maybe.get();
        todo.setCompleted(completed);

        Todo updated = repository.update(todo);

        log.info("✅ Todo {} updated", id);
        return updated;
    }

    @Override
    @PreAuthorize("@todoSecurityService.canModifyTodo(#id, authentication) or hasRole('ADMIN')")
    @Auditable(
            action = "DELETE_TODO",
            description = "User deleted todo",
            level = AuditLevel.WARN
    )
    public boolean delete(@Positive int id) throws IOException {

        log.info("🗑️ Deleting todo: {}", id);

        // Verifica se existe
        List<Todo> maybe = repository.findById(id);

        if (maybe.isEmpty()) {
            log.warn("⚠️ Todo not found: {}", id);
            return false;
        }

        boolean deleted = repository.delete(id);

        if (deleted) {
            log.info("✅ Todo {} deleted successfully", id);
        } else {
            log.error("❌ Failed to delete todo {}", id);
        }

        return deleted;
    }
}
