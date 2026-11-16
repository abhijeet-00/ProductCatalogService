package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.clients.UserAuthClient;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.dto.UserDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.service.ICategoryService;
import com.practice.productcatalogservice.service.IPitchService;
import com.practice.productcatalogservice.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.autoconfigure.web.DataWebAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
// WebMvcTest does not enable Spring Data's web support, so Pageable arguments stay unresolved without this.
@ImportAutoConfiguration(DataWebAutoConfiguration.class)
public class ProductControllerTests {
    private static final String CATEGORY_NAME = "Beauty";
    private static final String BEARER_TOKEN = "Bearer test-token";
    private static final String VALID_PAYLOAD = """
            {"name":"Mascara","price":23.99,"description":"Desc","categoryDto":{"name":"Beauty"}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProductService productService;
    @MockitoBean
    private IPitchService pitchService;
    @MockitoBean
    private ICategoryService categoryService;
    // UserTokenFilter is a Filter bean, so the MVC slice picks it up and needs this collaborator.
    @MockitoBean
    private UserAuthClient userAuthClient;

    @BeforeEach
    void stubAuthenticatedUser() {
        UserDto admin = new UserDto();
        admin.setRole("ADMIN");
        when(userAuthClient.getUser("test-token")).thenReturn(admin);
    }

    // ---------- GET /products/{id} ----------

    @Test
    void getProductById_productExists_returnsDto() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id)).thenReturn(buildProduct(id));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Mascara"))
                .andExpect(jsonPath("$.price").value(15.5))
                .andExpect(jsonPath("$.categoryDto.name").value(CATEGORY_NAME));
    }

    @Test
    void getProductById_productMissing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id)).thenReturn(null);

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /products/{id}/pitch ----------

    @Test
    void getPitch_productExists_returnsPitch() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id)).thenReturn(buildProduct(id));
        when(pitchService.getPitch(any(Product.class))).thenReturn("Big pitch");

        mockMvc.perform(get("/products/{id}/pitch", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Big pitch"));
    }

    @Test
    void getPitch_productMissing_returns404AndNeverCallsPitchService() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id)).thenReturn(null);

        mockMvc.perform(get("/products/{id}/pitch", id))
                .andExpect(status().isNotFound());

        verifyNoInteractions(pitchService);
    }

    // ---------- POST /products ----------

    @Test
    void createProduct_validPayload_returns201() throws Exception {
        when(productService.createProduct(any(ProductDto.class))).thenReturn(buildProduct(UUID.randomUUID()));

        mockMvc.perform(post("/products")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mascara"));
    }

    @Test
    void createProduct_missingPrice_returns400() throws Exception {
        String payloadWithoutPrice = """
                {"name":"Mascara","description":"Desc","categoryDto":{"name":"Beauty"}}
                """;

        mockMvc.perform(post("/products")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithoutPrice))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    // ---------- DELETE /products/{id} ----------

    @Test
    void deleteProduct_deleted_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.deleteProduct(id)).thenReturn(true);

        mockMvc.perform(delete("/products/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notDeleted_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.deleteProduct(id)).thenReturn(false);

        mockMvc.perform(delete("/products/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /products ----------

    @Test
    void replaceProduct_replaced_returns200() throws Exception {
        when(productService.replaceProduct(any(ProductDto.class))).thenReturn(buildProduct(UUID.randomUUID()));

        mockMvc.perform(put("/products")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mascara"));
    }

    @Test
    void replaceProduct_productMissing_returns404() throws Exception {
        when(productService.replaceProduct(any(ProductDto.class))).thenReturn(null);

        mockMvc.perform(put("/products")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /products ----------

    @Test
    void updateProduct_updated_returns200() throws Exception {
        Product updatedProduct = buildProduct(UUID.randomUUID());
        updatedProduct.setName("Updated Mascara");
        when(productService.updateProduct(any(ProductDto.class))).thenReturn(updatedProduct);

        mockMvc.perform(patch("/products")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Mascara"));
    }

    @Test
    void updateProduct_productMissing_returns404() throws Exception {
        when(productService.updateProduct(any(ProductDto.class))).thenReturn(null);

        mockMvc.perform(patch("/products")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /products/categories/{categoryName} ----------

    @Test
    void getAvgPriceByCategory_categoryExists_returnsAverage() throws Exception {
        when(categoryService.getAvgPriceByCategory(CATEGORY_NAME)).thenReturn(new BigDecimal("24.75"));

        mockMvc.perform(get("/products/categories/{categoryName}", CATEGORY_NAME))
                .andExpect(status().isOk())
                .andExpect(content().string("Average price for category Beauty is: 24.75"));
    }

    @Test
    void getAvgPriceByCategory_noProducts_returnsZero() throws Exception {
        when(categoryService.getAvgPriceByCategory("Empty")).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/products/categories/{categoryName}", "Empty"))
                .andExpect(status().isOk())
                .andExpect(content().string("Average price for category Empty is: 0"));
    }

    // ---------- GET /products/paged/all ----------

    @Test
    void getAllProductsPagedData_pageOfProducts_returnsPagedModel() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(buildProduct(UUID.randomUUID())), pageable, 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/products/paged/all").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Mascara"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getAllProductsPagedData_noProducts_returnsEmptyContent() throws Exception {
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/products/paged/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ---------- GET /products/all ----------

    @Test
    void findAllProducts_productsExist_returnsList() throws Exception {
        when(productService.findAllProducts()).thenReturn(List.of(buildProduct(UUID.randomUUID()), buildProduct(UUID.randomUUID())));

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mascara"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAllProducts_noProducts_returnsEmptyList() throws Exception {
        when(productService.findAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /products/search ----------

    @Test
    void searchByPriceRange_validRange_returnsPagedModel() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(buildProduct(UUID.randomUUID())), pageable, 1);
        when(productService.findProductsBetweenMinAndMaxPrice(
                eq(new BigDecimal("10.00")), eq(new BigDecimal("50.00")), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/products/search")
                        .param("minPrice", "10.00")
                        .param("maxPrice", "50.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Mascara"));

        verify(productService).findProductsBetweenMinAndMaxPrice(
                eq(new BigDecimal("10.00")), eq(new BigDecimal("50.00")), any(Pageable.class));
    }

    @Test
    void searchByPriceRange_nonNumericMinPrice_returns400() throws Exception {
        mockMvc.perform(get("/products/search")
                        .param("minPrice", "abc")
                        .param("maxPrice", "50.00"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    // ---------- GET /products/health ----------

    @Test
    void getHealth_returnsRunning() throws Exception {
        mockMvc.perform(get("/products/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is running"));
    }

    @Test
    void getHealth_doesNotTouchServices() throws Exception {
        mockMvc.perform(get("/products/health"))
                .andExpect(status().isOk());

        verifyNoInteractions(productService, pitchService, categoryService);
    }

    // ---------- fixtures ----------

    private static Product buildProduct(UUID id) {
        Category category = new Category();
        category.setName(CATEGORY_NAME);
        category.setDescription("Beauty products");

        Product product = new Product();
        product.setId(id);
        product.setName("Mascara");
        product.setDescription("A volumizing mascara");
        product.setPrice(new BigDecimal("15.50"));
        // ProductDto.from(product) NPEs on a null category, so it must always be set.
        product.setCategory(category);
        return product;
    }
}
