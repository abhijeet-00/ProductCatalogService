package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.dto.CategoryDto;
import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.service.ICategoryService;
import com.practice.productcatalogservice.service.IPitchService;
import com.practice.productcatalogservice.service.IProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final IProductService iProductService;
    private final IPitchService iPitchService;
    private final ICategoryService iCategoryService;

    public ProductController(IProductService iProductService, IPitchService iPitchService, ICategoryService iCategoryService) {
        this.iProductService = iProductService;
        this.iPitchService = iPitchService;
        this.iCategoryService = iCategoryService;
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

    @PutMapping
    public ResponseEntity<ProductDto> replaceProduct(@RequestBody ProductDto productDto) {
        Product product = iProductService.replaceProduct(productDto);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new  ResponseEntity<>(from(product), HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto productDto) {
        Product product = iProductService.updateProduct(productDto);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new  ResponseEntity<>(from(product), HttpStatus.OK);
    }

    @GetMapping("categories/{categoryName}")
    public ResponseEntity<String> getAvgPriceCategory(@PathVariable String categoryName) {
        BigDecimal avgPrice = iCategoryService.getAvgPriceByCategory(categoryName);
        return new ResponseEntity<>("Average price for category " + categoryName + " is: " + avgPrice, HttpStatus.OK);
    }

    @GetMapping("/paged/all")
    public ResponseEntity<PagedModel<ProductDto>> getAllProductsPagedData(Pageable pageable) {
        Page<ProductDto> products = iProductService.getAllProducts(pageable).map(this::from);
        return new ResponseEntity<>(new PagedModel<>(products), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> findAllProducts() {
        List<ProductDto> productDtos = iProductService.findAllProducts().stream().map(this::from).toList();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
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
