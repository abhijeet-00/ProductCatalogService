package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IProductService {
    Product getProductById(UUID id);
    Product createProduct(ProductDto productDto);
    boolean deleteProduct(UUID id);
    Product replaceProduct(ProductDto productDto);
    Product updateProduct(ProductDto productDto);
    int loadAllProducts();
    Page<Product> getAllProducts(Pageable pageable);
    List<Product> findAllProducts();
    Page<Product> findProductsBetweenMinAndMaxPrice(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
