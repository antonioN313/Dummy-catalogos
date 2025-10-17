package com.antonio.domain.port;

import com.antonio.domain.model.Todo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    List<Todo> list(int limit, int skip) throws IOException;
    Todo add(Todo todo) throws IOException;
    Todo update(Todo todo) throws IOException;
    boolean delete(int id) throws IOException;
    Optional<Todo> findById(int id) throws IOException;
}
