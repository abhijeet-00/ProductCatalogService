package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.dto.CategoryDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.service.IPitchService;
import com.practice.productcatalogservice.service.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final IProductService iProductService;
    private final IPitchService iPitchService;
    public ProductController(IProductService iProductService, IPitchService iPitchService) {
        this.iProductService = iProductService;
        this.iPitchService = iPitchService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product product = iProductService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(from(product));
    }
    @GetMapping("{id}/pitch")
    public ResponseEntity<String> getProductPitch(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product product = iProductService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        String pitch = iPitchService.getPitch(product);
        return ResponseEntity.ok(pitch);
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        if (productDto.getName() == null || productDto.getPrice() == null || productDto.getDescription() == null || productDto.getCategoryDto() == null) {
            return ResponseEntity.badRequest().build();
        }
        Product product = iProductService.createProduct(productDto);
        return new ResponseEntity<>(from(product), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        boolean deleted = iProductService.deleteProduct(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private ProductDto from(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPrice(product.getPrice());
        productDto.setDescription(product.getDescription());
        productDto.setCategoryDto(fromCategory(product.getCategory()));
        return productDto;
    }
    private CategoryDto fromCategory(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        categoryDto.setDescription(category.getDescription());
        return categoryDto;
    }
}
