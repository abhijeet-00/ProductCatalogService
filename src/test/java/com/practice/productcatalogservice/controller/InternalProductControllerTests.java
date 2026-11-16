package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.clients.UserAuthClient;
import com.practice.productcatalogservice.dto.UserDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.service.IDummyProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalProductController.class)
public class InternalProductControllerTests {
    private static final String CATEGORY_NAME = "Beauty";
    private static final String BEARER_TOKEN = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    // The controller injects this by @Qualifier("dummyProductService"), so the bean name must match.
    @MockitoBean(name = "dummyProductService")
    private IDummyProductService dummyProductService;
    // UserTokenFilter is a Filter bean, so the MVC slice picks it up and needs this collaborator.
    @MockitoBean
    private UserAuthClient userAuthClient;

    @BeforeEach
    void stubAuthenticatedUser() {
        UserDto admin = new UserDto();
        admin.setRole("ADMIN");
        when(userAuthClient.getUser("test-token")).thenReturn(admin);
    }

    // ---------- GET /internal/products/{id} ----------

    @Test
    void getDummyProduct_productExists_returnsDto() throws Exception {
        when(dummyProductService.getProductById(1L)).thenReturn(buildProduct());

        mockMvc.perform(get("/internal/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Essence Mascara"))
                .andExpect(jsonPath("$.categoryDto.name").value(CATEGORY_NAME));
    }

    @Test
    void getDummyProduct_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/internal/products/{id}", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dummyProductService);
    }

    @Test
    void getDummyProduct_serviceReturnsNull_throwsNullPointerException() {
        when(dummyProductService.getProductById(1L)).thenReturn(null);

        // Known bug: ProductDto.from(null) is unguarded, so a missing product is a 500, not a 404.
        Throwable thrown = assertThrows(Throwable.class, () -> mockMvc.perform(get("/internal/products/{id}", 1L)));
        assertInstanceOf(NullPointerException.class, rootCause(thrown));
    }

    // ---------- POST /internal/products/load ----------

    @Test
    void loadAllProducts_productsLoaded_returnsCount() throws Exception {
        when(dummyProductService.loadAllProducts()).thenReturn(2);

        mockMvc.perform(post("/internal/products/load").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Total 2 products loaded from external service."));
    }

    @Test
    void loadAllProducts_serviceThrows_propagatesException() {
        when(dummyProductService.loadAllProducts()).thenThrow(new RuntimeException("dummy service is down"));

        // No @ControllerAdvice exists, so this surfaces as an unhandled 500 in production.
        Throwable thrown = assertThrows(Throwable.class, () -> mockMvc.perform(
                post("/internal/products/load").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)));
        assertInstanceOf(RuntimeException.class, rootCause(thrown));
    }

    // ---------- fixtures ----------

    private static Throwable rootCause(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }

    private static Product buildProduct() {
        Category category = new Category();
        category.setName(CATEGORY_NAME);
        category.setDescription("Beauty products");

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Essence Mascara");
        product.setDescription("A volumizing mascara");
        product.setPrice(new BigDecimal("9.99"));
        // ProductDto.from(product) NPEs on a null category, so it must always be set.
        product.setCategory(category);
        return product;
    }
}
