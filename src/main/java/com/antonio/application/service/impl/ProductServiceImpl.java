package com.antonio.application.service.impl;

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
@Service
@Validated
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Auditable(
            action = "LIST_PRODUCTS",
            description = "User listed products with pagination",
            level = AuditLevel.INFO
    )
    public List<Product> list(
            @Positive(message = "Limit must be positive") int limit,
            @Positive(message = "Skip must be zero or positive") int skip
    ) throws IOException {

        log.info("📋 Listing products - limit: {}, skip: {}", limit, skip);

        if (limit > 100) {
            log.warn("⚠️ Limit {} exceeds maximum (100), using 100", limit);
            limit = 100;
        }

        List<Product> products = repository.list(limit, skip);

        log.info("✅ Retrieved {} products", products.size());
        return products;
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Auditable(
            action = "SEARCH_PRODUCTS",
            description = "User searched products",
            level = AuditLevel.INFO
    )
    public List<Product> search(@Valid String query) throws IOException {

        log.info("🔍 Searching products with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        if (query.trim().length() < 2) {
            throw new IllegalArgumentException("Search query must have at least 2 characters");
        }

        List<Product> results = repository.search(query.trim());

        log.info("✅ Found {} products matching query", results.size());
        return results;
    }
}
