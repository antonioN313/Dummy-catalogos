package com.antonio.application.service.impl;

import com.antonio.application.service.TodoService;
import com.antonio.domain.model.Todo;
import com.antonio.domain.port.TodoRepository;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class TodoServiceImpl implements TodoService {
    private final TodoRepository repository;

    @Override
    public List<Todo> list(int limit, int skip) throws IOException {
        return repository.list(limit, skip);
    }

    @Override
    public Todo add(String text, int userId) throws IOException {
        Todo t = new Todo(null, text, false, userId);
        return repository.add(t);
    }

    @Override
    public Todo toggle(int id, boolean completed) throws IOException {
        Optional<Todo> maybe = repository.findById(id);
        if (maybe.isEmpty()) throw new IllegalArgumentException("Todo não encontrado: " + id);
        Todo t = maybe.get();
        t.setCompleted(completed);
        return repository.update(t);
    }

    @Override
    public boolean delete(int id) throws IOException {
        return repository.delete(id);
    }
}
