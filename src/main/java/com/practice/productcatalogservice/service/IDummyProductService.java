package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.model.Product;

public interface IDummyProductService {
    Product getProductById(Long id);
    int loadAllProducts();
}
