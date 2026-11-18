package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.CategoryDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import com.practice.productcatalogservice.repository.CategoryRepository;
import com.practice.productcatalogservice.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductInventoryServiceIntegrationTests {
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
        // Runs inside the test transaction and is rolled back afterwards, so this only
        // guards against rows that were left behind in the test schema.
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // ---------- createProduct ----------

    @Test
    void createProduct_newCategory_persistsProductAndCategory() {
        ProductDto productDto = buildProductDto("Integration Mascara", "19.99", "New Category", "New Category description");

        Product created = productInventoryService.createProduct(productDto);
        flushAndClear();

        assertNotNull(created.getId());
        assertEquals(State.ACTIVE, created.getState());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());

        Product reloaded = productRepository.findById(created.getId()).orElseThrow();
        assertEquals("Integration Mascara", reloaded.getName());
        assertEquals(new BigDecimal("19.99"), reloaded.getPrice());
        assertEquals(State.ACTIVE, reloaded.getState());
        assertEquals("New Category", reloaded.getCategory().getName());
        assertEquals("New Category description", reloaded.getCategory().getDescription());
        assertEquals(State.ACTIVE, reloaded.getCategory().getState());
    }

    @Test
    void createProduct_existingCategory_reusesCategory() {
        Category existingCategory = new Category();
        existingCategory.setName("Shared Category");
        existingCategory.setDescription("Shared description");
        existingCategory.setState(State.ACTIVE);
        existingCategory.setCreatedAt(new Date());
        categoryRepository.save(existingCategory);
        flushAndClear();

        Product created = productInventoryService.createProduct(
                buildProductDto("Shared Category Product", "5.00", "Shared Category", "Shared description"));
        flushAndClear();

        assertEquals(1, categoryRepository.count());
        assertEquals(existingCategory.getId(), created.getCategory().getId());
    }

    @Test
    void createProduct_nullCategoryDto_throwsNullPointerException() {
        ProductDto productDto = buildProductDto("No Category", "5.00", "Unused", "Unused");
        productDto.setCategoryDto(null);

        // Known bug: findOrCreateCategory() dereferences the DTO without a null check.
        assertThrows(NullPointerException.class, () -> productInventoryService.createProduct(productDto));
    }

    // ---------- getProductById ----------

    @Test
    void getProductById_persistedProduct_returnsProduct() {
        Product created = productInventoryService.createProduct(
                buildProductDto("Persisted Product", "12.50", "Lookup Category", "Lookup description"));
        flushAndClear();

        Product found = productInventoryService.getProductById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Persisted Product", found.getName());
    }

    @Test
    void getProductById_randomUuid_returnsNull() {
        assertNull(productInventoryService.getProductById(UUID.randomUUID()));
    }

    // ---------- deleteProduct ----------

    @Test
    void deleteProduct_existingProduct_softDeletes() {
        Product created = productInventoryService.createProduct(
                buildProductDto("Deletable Product", "8.00", "Delete Category", "Delete description"));
        flushAndClear();

        assertTrue(productInventoryService.deleteProduct(created.getId()));
        flushAndClear();

        Product reloaded = productRepository.findById(created.getId()).orElseThrow();
        assertEquals(State.INACTIVE, reloaded.getState());
        assertNull(productInventoryService.getProductById(created.getId()));
    }

    @Test
    void deleteProduct_randomUuid_returnsFalse() {
        assertFalse(productInventoryService.deleteProduct(UUID.randomUUID()));
    }

    // ---------- updateProduct ----------

    @Test
    void updateProduct_partialPayload_changesOnlyProvidedFields() {
        Product created = productInventoryService.createProduct(
                buildProductDto("Original Name", "15.00", "Update Category", "Original description"));
        flushAndClear();

        ProductDto patch = new ProductDto();
        patch.setId(created.getId());
        patch.setName("Patched Name");

        Product updated = productInventoryService.updateProduct(patch);
        flushAndClear();

        assertNotNull(updated);
        Product reloaded = productRepository.findById(created.getId()).orElseThrow();
        assertEquals("Patched Name", reloaded.getName());
        assertEquals(new BigDecimal("15.00"), reloaded.getPrice());
        assertEquals("Original Name description", reloaded.getDescription());
        assertNotNull(reloaded.getUpdatedAt());
    }

    @Test
    void updateProduct_randomUuid_returnsNull() {
        ProductDto patch = new ProductDto();
        patch.setId(UUID.randomUUID());
        patch.setName("Nowhere");

        assertNull(productInventoryService.updateProduct(patch));
    }

    // ---------- replaceProduct ----------

    @Test
    void replaceProduct_existingProduct_leavesOldRowInactiveAndCreatesNew() {
        Product original = productInventoryService.createProduct(
                buildProductDto("Original Product", "30.00", "Replace Category", "Original description"));
        flushAndClear();

        ProductDto replacementDto = buildProductDto("Replacement Product", "45.00", "Replace Category", "Replacement description");
        replacementDto.setId(original.getId());

        Product replacement = productInventoryService.replaceProduct(replacementDto);
        flushAndClear();

        assertNotNull(replacement);
        assertNotEquals(original.getId(), replacement.getId());
        assertEquals("Replacement Product", replacement.getName());
        assertEquals(State.ACTIVE, replacement.getState());

        Product oldRow = productRepository.findById(original.getId()).orElseThrow();
        assertEquals(State.INACTIVE, oldRow.getState());
        assertEquals(2, productRepository.count());
    }

    @Test
    void replaceProduct_randomUuid_returnsNull() {
        ProductDto replacementDto = buildProductDto("Ghost Product", "1.00", "Ghost Category", "Ghost description");
        replacementDto.setId(UUID.randomUUID());

        assertNull(productInventoryService.replaceProduct(replacementDto));
        assertEquals(0, productRepository.count());
    }

    // ---------- getAllProducts ----------

    @Test
    void getAllProducts_returnsOnlyActivePaged() {
        productInventoryService.createProduct(buildProductDto("Active One", "11.00", "Paged Category", "Paged description"));
        productInventoryService.createProduct(buildProductDto("Active Two", "12.00", "Paged Category", "Paged description"));
        Product toDelete = productInventoryService.createProduct(
                buildProductDto("Inactive One", "13.00", "Paged Category", "Paged description"));
        productInventoryService.deleteProduct(toDelete.getId());
        flushAndClear();

        Page<Product> page = productInventoryService.getAllProducts(PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        List<String> names = page.getContent().stream().map(Product::getName).toList();
        assertTrue(names.containsAll(List.of("Active One", "Active Two")));
        page.forEach(product -> assertEquals(State.ACTIVE, product.getState()));
    }

    @Test
    void getAllProducts_onlyInactive_returnsEmptyPage() {
        Product inactive = productInventoryService.createProduct(
                buildProductDto("Only Inactive", "14.00", "Paged Category", "Paged description"));
        productInventoryService.deleteProduct(inactive.getId());
        flushAndClear();

        Page<Product> page = productInventoryService.getAllProducts(PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
    }

    // ---------- findAllProducts ----------

    @Test
    void findAllProducts_returnsAllStatesWithCategoriesLoaded() {
        productInventoryService.createProduct(buildProductDto("Kept Product", "21.00", "Fetch Category", "Fetch description"));
        Product removed = productInventoryService.createProduct(
                buildProductDto("Removed Product", "22.00", "Fetch Category", "Fetch description"));
        productInventoryService.deleteProduct(removed.getId());
        flushAndClear();

        List<Product> products = productInventoryService.findAllProducts();

        assertEquals(2, products.size());
        // The custom LEFT JOIN FETCH query must initialise the category association.
        products.forEach(product -> assertNotNull(product.getCategory().getName()));
    }

    @Test
    void findAllProducts_emptyTable_returnsEmptyList() {
        assertTrue(productInventoryService.findAllProducts().isEmpty());
    }

    // ---------- findProductsBetweenMinAndMaxPrice ----------

    @Test
    void findProductsBetweenMinAndMaxPrice_returnsOnlyProductsInRange() {
        productInventoryService.createProduct(buildProductDto("Cheap", "10.00", "Range Category", "Range description"));
        productInventoryService.createProduct(buildProductDto("Mid", "20.00", "Range Category", "Range description"));
        productInventoryService.createProduct(buildProductDto("Expensive", "30.00", "Range Category", "Range description"));
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = productInventoryService.findProductsBetweenMinAndMaxPrice(
                new BigDecimal("15.00"), new BigDecimal("25.00"), pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Mid", page.getContent().get(0).getName());
    }

    @Test
    void findProductsBetweenMinAndMaxPrice_invertedRange_returnsEmptyPage() {
        productInventoryService.createProduct(buildProductDto("Any Product", "20.00", "Range Category", "Range description"));
        flushAndClear();

        Page<Product> page = productInventoryService.findProductsBetweenMinAndMaxPrice(
                new BigDecimal("100.00"), new BigDecimal("10.00"), PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
    }

    // ---------- loadAllProducts ----------

    @Test
    void loadAllProducts_placeholder_returnsZero() {
        assertEquals(0, productInventoryService.loadAllProducts());
    }

    // ---------- helpers ----------

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static ProductDto buildProductDto(String name, String price, String categoryName, String categoryDescription) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(categoryName);
        categoryDto.setDescription(categoryDescription);

        ProductDto productDto = new ProductDto();
        productDto.setName(name);
        productDto.setPrice(new BigDecimal(price));
        productDto.setDescription(name + " description");
        productDto.setCategoryDto(categoryDto);
        return productDto;
    }
}
