package com.antonio.infrastructure.repository;

import com.antonio.domain.model.Todo;
import com.antonio.domain.port.TodoRepository;
import com.antonio.dto.TodoListResponse;
import com.antonio.infrastructure.http.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TodoRepositoryHttp implements TodoRepository {
    private final HttpClientWrapper client;
    private final ObjectMapper mapper;

    public TodoRepositoryHttp(HttpClientWrapper client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<Todo> list(int limit, int skip) throws IOException {
        String path = "/todos?limit=" + limit + "&skip=" + skip;
        String body = client.get(path);
        TodoListResponse r = mapper.readValue(body, TodoListResponse.class);
        return r.getTodos();
    }

    @Override
    public Todo add(Todo todo) throws IOException {
        String body = client.post("/todos/add", todo);
        // API DummyJSON returns created todo (id assigned) or similar; map back
        return mapper.readValue(body, Todo.class);
    }

    @Override
    public Todo update(Todo todo) throws IOException {
        // DummyJSON uses /todos/{id} put
        String body = client.put("/todos/" + todo.getId(), todo);
        return mapper.readValue(body, Todo.class);
    }

    @Override
    public boolean delete(int id) throws IOException {
        client.delete("/todos/" + id);
        return true;
    }

    @Override
    public Optional<Todo> findById(int id) throws IOException {
        try {
            String body = client.get("/todos/" + id);
            Todo t = mapper.readValue(body, Todo.class);
            return Optional.ofNullable(t);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
