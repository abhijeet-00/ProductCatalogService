package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.DummyAllProductsDto;
import com.practice.productcatalogservice.dto.DummyProductServiceDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import com.practice.productcatalogservice.repository.CategoryRepository;
import com.practice.productcatalogservice.repository.ProductRepository;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class DummyProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private RestTemplate restTemplate;

    public DummyProductService(RestTemplateBuilder builder, ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.restTemplate = builder.build();
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product getProductById(Long id) {
        ResponseEntity<DummyProductServiceDto> response = restTemplate.getForEntity("https://dummyjson.com/products/{id}", DummyProductServiceDto.class, id);
        return convertToProduct(response.getBody());
    }


    @Override
    public Product createProduct(ProductDto productDto) {
        return null;
    }

    @Override
    public boolean deleteProduct(Long id) {
        return false;
    }

    @Override
    public Product replaceProduct(ProductDto productDto) {
        return null;
    }

    @Override
    public Product updateProduct(ProductDto productDto) {
        return null;
    }

    @Override
    public int loadAllProducts() {
        ResponseEntity<DummyAllProductsDto> response = restTemplate.getForEntity("https://dummyjson.com/products?limit=0", DummyAllProductsDto.class);
        int count = 0;
        if (response.getBody() != null) {
            for (DummyProductServiceDto dummyProductServiceDto : response.getBody().getProducts()) {
                productRepository.save(convertToProduct(dummyProductServiceDto));
                count++;
            }
        }
        return count;
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return null;
    }

    @Override
    public List<Product> findAllProducts() {
        return List.of();
    }

    @Override
    public List<Product> findProductsBetweenMinAndMaxPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        return List.of();
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
