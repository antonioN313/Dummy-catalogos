package com.antonio.application.service.impl;

import com.antonio.application.service.ProductService;
import com.antonio.domain.model.Product;
import com.antonio.domain.port.ProductRepository;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    @Override
    public List<Product> list(int limit, int skip) throws IOException {
        return repository.list(limit, skip);
    }

    @Override
    public List<Product> search(String q) throws IOException {
        return repository.search(q);
    }
}
