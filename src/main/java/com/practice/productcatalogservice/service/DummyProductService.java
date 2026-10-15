package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.DummyProductServiceDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import com.practice.productcatalogservice.repository.ProductRepository;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Service
public class DummyProductService implements IProductService {
    private final ProductRepository productRepository;
    private RestTemplate restTemplate;

    public DummyProductService(RestTemplateBuilder builder, ProductRepository productRepository) {
        this.restTemplate = builder.build();
        this.productRepository = productRepository;
    }

    @Override
    public Product getProductById(Long id) {
        ResponseEntity<DummyProductServiceDto> response = restTemplate.getForEntity("https://dummyjson.com/products/{id}", DummyProductServiceDto.class, id);
        return convertToProduct(response.getBody());
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        return null;
    }

    @Override
    public boolean deleteProduct(Long id) {
        return false;
    }

    private Product convertToProduct(DummyProductServiceDto dummyProductServiceDto) {
        Product product = new Product();
        product.setId(dummyProductServiceDto.getId());
        product.setName(dummyProductServiceDto.getTitle());
        product.setDescription(dummyProductServiceDto.getDescription());
        product.setPrice(dummyProductServiceDto.getPrice());
        Category category = new Category();
        category.setName(dummyProductServiceDto.getCategory());
        product.setCategory(category);
        return product;
    }
}
