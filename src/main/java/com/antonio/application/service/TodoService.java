package com.antonio.application.service;

import com.antonio.domain.model.Todo;

import java.io.IOException;
import java.util.List;

public interface TodoService {
    List<Todo> list(int limit, int skip) throws IOException;
    Todo add(String text, int userId) throws IOException;
    Todo toggle(int id, boolean completed) throws IOException;
    boolean delete(int id) throws IOException;
}
