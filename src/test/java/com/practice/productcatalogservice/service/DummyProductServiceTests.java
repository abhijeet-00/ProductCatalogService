package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.clients.DummyClient;
import com.practice.productcatalogservice.dto.DummyProductServiceDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import com.practice.productcatalogservice.repository.CategoryRepository;
import com.practice.productcatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DummyProductServiceTests {
    private static final String CATEGORY_NAME = "Beauty";
    private static final String IMAGE_URL = "https://dummyjson.com/image/1";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private DummyClient dummyClient;
    @InjectMocks
    private DummyProductService dummyProductService;

    // ---------- getProductById ----------

    @Test
    void getProductById_existingDtoWithImages_mapsAllFields() {
        DummyProductServiceDto dummyDto = buildDummyDto();
        Category existingCategory = new Category();
        existingCategory.setName(CATEGORY_NAME);
        existingCategory.setState(State.ACTIVE);
        when(dummyClient.getProduct(1L)).thenReturn(dummyDto);
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.of(existingCategory));

        Product product = dummyProductService.getProductById(1L);

        assertEquals(dummyDto.getTitle(), product.getName());
        assertEquals(dummyDto.getDescription(), product.getDescription());
        assertEquals(dummyDto.getPrice(), product.getPrice());
        assertEquals(IMAGE_URL, product.getImageUrl());
        assertSame(existingCategory, product.getCategory());
        assertEquals(State.ACTIVE, product.getState());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    void getProductById_newCategory_createsActiveCategory() {
        DummyProductServiceDto dummyDto = buildDummyDto();
        when(dummyClient.getProduct(1L)).thenReturn(dummyDto);
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.empty());

        Product product = dummyProductService.getProductById(1L);

        Category category = product.getCategory();
        assertNotNull(category);
        assertEquals(CATEGORY_NAME, category.getName());
        assertEquals("Description for " + CATEGORY_NAME, category.getDescription());
        assertEquals(State.ACTIVE, category.getState());
        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
    }

    @Test
    void getProductById_clientThrows_propagatesException() {
        when(dummyClient.getProduct(99L)).thenThrow(new RestClientException("dummy service is down"));

        assertThrows(RestClientException.class, () -> dummyProductService.getProductById(99L));
        verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    void getProductById_missingImages_leavesImageUrlNull() {
        DummyProductServiceDto dummyDto = buildDummyDto();
        dummyDto.setImages(List.of());
        when(dummyClient.getProduct(1L)).thenReturn(dummyDto);
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.empty());

        Product product = dummyProductService.getProductById(1L);

        assertNull(product.getImageUrl());
        assertEquals(dummyDto.getTitle(), product.getName());
        assertEquals(dummyDto.getPrice(), product.getPrice());
        assertEquals(State.ACTIVE, product.getState());
    }

    // ---------- loadAllProducts ----------

    @Test
    void loadAllProducts_twoProducts_savesAllAndReturnsCount() {
        DummyProductServiceDto first = buildDummyDto();
        DummyProductServiceDto second = buildDummyDto();
        second.setId(2L);
        second.setTitle("Second Product");
        when(dummyClient.getAllProducts()).thenReturn(List.of(first, second));
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.empty());

        assertEquals(2, dummyProductService.loadAllProducts());

        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void loadAllProducts_noProducts_returnsZeroAndNeverSaves() {
        when(dummyClient.getAllProducts()).thenReturn(List.of());

        assertEquals(0, dummyProductService.loadAllProducts());

        verify(productRepository, never()).save(any(Product.class));
    }

    // ---------- fixtures ----------

    private static DummyProductServiceDto buildDummyDto() {
        DummyProductServiceDto dummyDto = new DummyProductServiceDto();
        dummyDto.setId(1L);
        dummyDto.setTitle("Essence Mascara");
        dummyDto.setDescription("A volumizing mascara");
        dummyDto.setPrice(new BigDecimal("9.99"));
        dummyDto.setCategory(CATEGORY_NAME);
        dummyDto.setImages(List.of(IMAGE_URL));
        return dummyDto;
    }
}
