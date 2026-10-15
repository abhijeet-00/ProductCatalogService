package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Product;

public interface IProductService {
    Product getProductById(Long id);
    Product createProduct(ProductDto productDto);
    boolean deleteProduct(Long id);
}
