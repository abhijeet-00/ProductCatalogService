package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.CategoryDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.repository.CategoryRepository;
import com.practice.productcatalogservice.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CategoryServiceIntegrationTests {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductInventoryService productInventoryService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void getAvgPriceByCategory_returnsDatabaseAverage() {
        createProduct("Cheap Mascara", "10.00", "Average Category");
        createProduct("Pricey Mascara", "20.00", "Average Category");
        flushAndClear();

        BigDecimal average = categoryService.getAvgPriceByCategory("Average Category");

        // AVG() returns a wider scale, so compare numerically rather than with equals().
        assertEquals(0, new BigDecimal("15.00").compareTo(average));
    }

    @Test
    void getAvgPriceByCategory_unknownCategory_returnsZero() {
        assertEquals(BigDecimal.ZERO, categoryService.getAvgPriceByCategory("No Such Category"));
    }

    private void createProduct(String name, String price, String categoryName) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(categoryName);
        categoryDto.setDescription(categoryName + " description");

        ProductDto productDto = new ProductDto();
        productDto.setName(name);
        productDto.setPrice(new BigDecimal(price));
        productDto.setDescription(name + " description");
        productDto.setCategoryDto(categoryDto);

        productInventoryService.createProduct(productDto);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
