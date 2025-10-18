package com.antonio.domain.port;

import com.antonio.domain.model.Product;

import java.io.IOException;
import java.util.List;

public interface ProductRepository {
    List<Product> list(int limit, int skip) throws IOException;
    List<Product> search(String q) throws IOException;
}
