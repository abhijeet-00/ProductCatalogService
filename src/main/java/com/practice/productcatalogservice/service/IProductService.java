package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.model.Product;

public interface IProductService {
    Product getProductById(Long id);
}
