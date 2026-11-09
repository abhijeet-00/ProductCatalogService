package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.clients.DummyClient;
import com.practice.productcatalogservice.dto.DummyProductServiceDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import com.practice.productcatalogservice.repository.CategoryRepository;
import com.practice.productcatalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DummyProductService implements IDummyProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DummyClient dummyClient;

    @Override
    public Product getProductById(Long id) {
        return convertToProduct(dummyClient.getProduct(id));
    }


    public Product createProduct(ProductDto productDto) {
        return null;
    }

    public boolean deleteProduct(Long id) {
        return false;
    }

    public Product replaceProduct(ProductDto productDto) {
        return null;
    }

    public Product updateProduct(ProductDto productDto) {
        return null;
    }

    @Override
    public int loadAllProducts() {
        int count = 0;
        for (DummyProductServiceDto dummyProductServiceDto : dummyClient.getAllProducts()) {
            productRepository.save(convertToProduct(dummyProductServiceDto));
            count++;
        }
        return count;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return null;
    }

    public List<Product> findAllProducts() {
        return List.of();
    }

    public Page<Product> findProductsBetweenMinAndMaxPrice(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return Page.empty();
    }

    private Product convertToProduct(DummyProductServiceDto dummyProductServiceDto) {
        Product product = new Product();
        product.setName(dummyProductServiceDto.getTitle());
        product.setDescription(dummyProductServiceDto.getDescription());
        product.setPrice(dummyProductServiceDto.getPrice());
        product.setImageUrl(dummyProductServiceDto.getImages().get(0));
        product.setCategory(findOrCreateCategory(dummyProductServiceDto.getCategory()));
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        product.setState(State.ACTIVE);
        return product;
    }
    private Category findOrCreateCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName).orElse(null);
        if (category == null) {
            category = new Category();
            category.setName(categoryName);
            category.setDescription("Description for " + categoryName);
            category.setState(State.ACTIVE);
            category.setCreatedAt(new Date());
        }
        category.setUpdatedAt(new Date());
        return category;
    }
}
