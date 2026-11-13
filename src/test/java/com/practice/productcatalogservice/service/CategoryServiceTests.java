package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAvgPriceByCategory_repositoryReturnsAverage_returnsAverage() {
        BigDecimal average = new BigDecimal("24.75");
        when(categoryRepository.getAvgPriceByCategory("Beauty")).thenReturn(average);

        assertEquals(average, categoryService.getAvgPriceByCategory("Beauty"));
    }

    @Test
    void getAvgPriceByCategory_repositoryReturnsNull_returnsZero() {
        when(categoryRepository.getAvgPriceByCategory("Empty Category")).thenReturn(null);

        assertEquals(BigDecimal.ZERO, categoryService.getAvgPriceByCategory("Empty Category"));
    }
}
