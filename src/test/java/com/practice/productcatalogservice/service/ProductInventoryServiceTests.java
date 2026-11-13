package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.CategoryDto;
import com.practice.productcatalogservice.dto.ProductDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductInventoryServiceTests {
    private static final String CATEGORY_NAME = "Makeup";
    private static final String CATEGORY_DESCRIPTION = "Makeup products";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private ProductInventoryService productInventoryService;

    // ---------- getProductById ----------

    @Test
    void getProductById_activeProduct_returnsProduct() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id);
        when(productRepository.findByIdAndState(id, State.ACTIVE)).thenReturn(Optional.of(product));

        Product result = productInventoryService.getProductById(id);

        assertSame(product, result);
        verify(productRepository).findByIdAndState(id, State.ACTIVE);
    }

    @Test
    void getProductById_productMissing_returnsNull() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdAndState(id, State.ACTIVE)).thenReturn(Optional.empty());

        assertNull(productInventoryService.getProductById(id));
    }

    // ---------- createProduct ----------

    @Test
    void createProduct_existingCategory_savesWithActiveState() {
        ProductDto productDto = buildProductDto(UUID.randomUUID());
        Category existingCategory = buildCategory();
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.of(existingCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product createdProduct = productInventoryService.createProduct(productDto);

        assertEquals(productDto.getName(), createdProduct.getName());
        assertEquals(productDto.getDescription(), createdProduct.getDescription());
        assertEquals(productDto.getPrice(), createdProduct.getPrice());
        assertSame(existingCategory, createdProduct.getCategory());
        assertEquals(State.ACTIVE, createdProduct.getState());
        assertNotNull(createdProduct.getCreatedAt());
        assertNotNull(createdProduct.getUpdatedAt());
        verify(productRepository).save(createdProduct);
    }

    @Test
    void createProduct_categoryMissing_createsCategoryWithDerivedDescription() {
        ProductDto productDto = buildProductDto(UUID.randomUUID());
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product createdProduct = productInventoryService.createProduct(productDto);

        Category category = createdProduct.getCategory();
        assertNotNull(category);
        assertEquals(CATEGORY_NAME, category.getName());
        assertEquals(CATEGORY_DESCRIPTION, category.getDescription());
        assertEquals(State.ACTIVE, category.getState());
        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
    }

    // ---------- deleteProduct ----------

    @Test
    void deleteProduct_productExists_marksInactiveAndReturnsTrue() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id);
        product.setState(State.ACTIVE);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(productInventoryService.deleteProduct(id));

        assertEquals(State.INACTIVE, product.getState());
        assertNotNull(product.getUpdatedAt());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_productMissing_returnsFalseAndNeverSaves() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertFalse(productInventoryService.deleteProduct(id));

        verify(productRepository, never()).save(any(Product.class));
    }

    // ---------- replaceProduct ----------

    @Test
    void replaceProduct_productExists_deactivatesOldAndCreatesNew() {
        UUID id = UUID.randomUUID();
        Product existingProduct = buildProduct(id);
        existingProduct.setState(State.ACTIVE);
        ProductDto productDto = buildProductDto(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.of(buildCategory()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product replacedProduct = productInventoryService.replaceProduct(productDto);

        assertEquals(State.INACTIVE, existingProduct.getState());
        assertNotNull(replacedProduct);
        assertEquals(productDto.getName(), replacedProduct.getName());
        assertEquals(productDto.getPrice(), replacedProduct.getPrice());
        assertEquals(State.ACTIVE, replacedProduct.getState());
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void replaceProduct_productMissing_returnsNullAndNeverSaves() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(productInventoryService.replaceProduct(buildProductDto(id)));

        verify(productRepository, never()).save(any(Product.class));
    }

    // ---------- updateProduct ----------

    @Test
    void updateProduct_allFieldsProvided_appliesAllAndSaves() {
        UUID id = UUID.randomUUID();
        Product existingProduct = buildProduct(id);
        existingProduct.setCategory(buildCategory());
        ProductDto productDto = buildProductDto(id);
        productDto.setName("Updated Mascara");
        productDto.setDescription("Updated description");
        productDto.setPrice(new BigDecimal("99.99"));
        CategoryDto updatedCategoryDto = new CategoryDto();
        updatedCategoryDto.setName("Skincare");
        updatedCategoryDto.setDescription("Skincare products");
        productDto.setCategoryDto(updatedCategoryDto);
        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct = productInventoryService.updateProduct(productDto);

        assertEquals("Updated Mascara", updatedProduct.getName());
        assertEquals("Updated description", updatedProduct.getDescription());
        assertEquals(new BigDecimal("99.99"), updatedProduct.getPrice());
        assertEquals("Skincare", updatedProduct.getCategory().getName());
        assertEquals("Skincare products", updatedProduct.getCategory().getDescription());
        assertNotNull(updatedProduct.getUpdatedAt());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_productMissing_returnsNullAndNeverSaves() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(productInventoryService.updateProduct(buildProductDto(id)));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_nullCategory_createsCategoryFromDto() {
        UUID id = UUID.randomUUID();
        Product existingProduct = buildProduct(id);
        existingProduct.setCategory(null);
        ProductDto productDto = buildProductDto(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct = productInventoryService.updateProduct(productDto);

        Category category = updatedProduct.getCategory();
        assertNotNull(category);
        assertEquals(CATEGORY_NAME, category.getName());
        assertEquals(CATEGORY_DESCRIPTION, category.getDescription());
        assertEquals(State.ACTIVE, category.getState());
        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
        verify(productRepository).save(existingProduct);
    }

    // ---------- loadAllProducts ----------

    @Test
    void loadAllProducts_returnsZero() {
        assertEquals(0, productInventoryService.loadAllProducts());
    }

    @Test
    void loadAllProducts_doesNotTouchRepositories() {
        productInventoryService.loadAllProducts();

        // Documents that this implementation is still a placeholder.
        verifyNoInteractions(productRepository, categoryRepository);
    }

    // ---------- getAllProducts ----------

    @Test
    void getAllProducts_delegatesActiveStateAndPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(buildProduct(UUID.randomUUID())), pageable, 1);
        when(productRepository.findByState(State.ACTIVE, pageable)).thenReturn(expectedPage);

        Page<Product> result = productInventoryService.getAllProducts(pageable);

        assertSame(expectedPage, result);
        verify(productRepository).findByState(State.ACTIVE, pageable);
    }

    @Test
    void getAllProducts_noActiveProducts_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByState(State.ACTIVE, pageable)).thenReturn(Page.empty(pageable));

        Page<Product> result = productInventoryService.getAllProducts(pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ---------- findAllProducts ----------

    @Test
    void findAllProducts_returnsRepositoryList() {
        List<Product> expectedProducts = List.of(buildProduct(UUID.randomUUID()), buildProduct(UUID.randomUUID()));
        when(productRepository.findAllProducts()).thenReturn(expectedProducts);

        assertSame(expectedProducts, productInventoryService.findAllProducts());
    }

    @Test
    void findAllProducts_noProducts_returnsEmptyList() {
        when(productRepository.findAllProducts()).thenReturn(List.of());

        assertTrue(productInventoryService.findAllProducts().isEmpty());
    }

    // ---------- findProductsBetweenMinAndMaxPrice ----------

    @Test
    void findProductsBetweenMinAndMaxPrice_forwardsPriceRange() {
        BigDecimal minPrice = new BigDecimal("10.00");
        BigDecimal maxPrice = new BigDecimal("50.00");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(buildProduct(UUID.randomUUID())), pageable, 1);
        when(productRepository.findByPriceBetween(minPrice, maxPrice, pageable)).thenReturn(expectedPage);

        Page<Product> result = productInventoryService.findProductsBetweenMinAndMaxPrice(minPrice, maxPrice, pageable);

        assertSame(expectedPage, result);
        verify(productRepository).findByPriceBetween(minPrice, maxPrice, pageable);
    }

    @Test
    void findProductsBetweenMinAndMaxPrice_invertedRange_returnsEmptyPage() {
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("10.00");
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByPriceBetween(minPrice, maxPrice, pageable)).thenReturn(Page.empty(pageable));

        Page<Product> result = productInventoryService.findProductsBetweenMinAndMaxPrice(minPrice, maxPrice, pageable);

        assertTrue(result.isEmpty());
        // Bounds must be forwarded untouched - the service must not silently swap them.
        verify(productRepository).findByPriceBetween(minPrice, maxPrice, pageable);
    }

    // ---------- fixtures ----------

    private static ProductDto buildProductDto(UUID id) {
        ProductDto productDto = new ProductDto();
        productDto.setId(id);
        productDto.setName("Test Product");
        productDto.setDescription("Test Description");
        productDto.setPrice(new BigDecimal("23.99"));
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(CATEGORY_NAME);
        categoryDto.setDescription(CATEGORY_DESCRIPTION);
        productDto.setCategoryDto(categoryDto);
        return productDto;
    }

    private static Product buildProduct(UUID id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Mascara");
        product.setDescription("A volumizing mascara");
        product.setPrice(new BigDecimal("15.50"));
        return product;
    }

    private static Category buildCategory() {
        Category category = new Category();
        category.setName(CATEGORY_NAME);
        category.setDescription(CATEGORY_DESCRIPTION);
        category.setState(State.ACTIVE);
        return category;
    }
}
