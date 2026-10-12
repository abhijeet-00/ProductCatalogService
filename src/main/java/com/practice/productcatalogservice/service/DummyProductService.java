package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.DummyProductServiceDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DummyProductService implements IProductService {
    private RestTemplate restTemplate;

    public DummyProductService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public Product getProductById(Long id) {
        ResponseEntity<DummyProductServiceDto> response = restTemplate.getForEntity("https://dummyjson.com/products/{id}", DummyProductServiceDto.class, id);
        return convertToProduct(response.getBody());
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
