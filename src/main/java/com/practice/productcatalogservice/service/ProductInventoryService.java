package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.CategoryDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import com.practice.productcatalogservice.repository.CategoryRepository;
import com.practice.productcatalogservice.repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Primary
public class ProductInventoryService implements IProductService{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductInventoryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findByIdAndState(id, State.ACTIVE).orElse(null);
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        Product product = from(productDto);
        product.setCategory(findOrCreateCategory(productDto.getCategoryDto()));
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        product.setState(State.ACTIVE);
        return productRepository.save(product);
    }

    @Override
    public boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setState(State.INACTIVE);
            product.setUpdatedAt(new Date());
            productRepository.save(product);
            return true;
        }
        return false;
    }

    private Product from(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        return product;
    }
    private Category findOrCreateCategory(CategoryDto categoryDto) {
        Category category = categoryRepository.findByName(categoryDto.getName()).orElse(null);
        if (category == null) {
            category = new Category();
            category.setName(categoryDto.getName());
            category.setDescription(categoryDto.getDescription());
            category.setCreatedAt(new Date());
        }
        category.setUpdatedAt(new Date());
        return category;
    }

}
