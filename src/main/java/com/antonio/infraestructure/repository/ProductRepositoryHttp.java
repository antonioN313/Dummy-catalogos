package com.antonio.infrastructure.repository;

import com.antonio.domain.model.Product;
import com.antonio.domain.port.ProductRepository;
import com.antonio.dto.ProductListResponse;
import com.antonio.infrastructure.http.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class ProductRepositoryHttp implements ProductRepository {
    private final HttpClientWrapper client;
    private final ObjectMapper mapper;

    public ProductRepositoryHttp(HttpClientWrapper client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<Product> list(int limit, int skip) throws IOException {
        String path = "/products?limit=" + limit + "&skip=" + skip;
        String body = client.get(path);
        ProductListResponse r = mapper.readValue(body, ProductListResponse.class);
        return r.getProducts();
    }

    @Override
    public List<Product> search(String q) throws IOException {
        String path = "/products/search?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8);
        String body = client.get(path);
        ProductListResponse r = mapper.readValue(body, ProductListResponse.class);
        return r.getProducts();
    }
}
