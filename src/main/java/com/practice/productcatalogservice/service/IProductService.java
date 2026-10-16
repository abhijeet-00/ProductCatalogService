package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    Product getProductById(Long id);
    Product createProduct(ProductDto productDto);
    boolean deleteProduct(Long id);
    Product replaceProduct(ProductDto productDto);
    Product updateProduct(ProductDto productDto);
    int loadAllProducts();
    Page<Product> getAllProducts(Pageable pageable);
    List<Product> findAllProducts();

}
