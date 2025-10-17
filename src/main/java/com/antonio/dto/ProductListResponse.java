package com.antonio.dto;

import com.antonio.domain.model.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductListResponse {
    private List<Product> products;
    private int total;
    private int skip;
    private int limit;
}