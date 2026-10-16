package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CategoryService implements ICategoryService{
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public BigDecimal getAvgPriceByCategory(String categoryName) {
        BigDecimal avgPrice = categoryRepository.getAvgPriceByCategory(categoryName);
        if (avgPrice != null) {
            return avgPrice;
        }
        return BigDecimal.ZERO;
    }
}
