package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.dto.ProductResponseDto;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.service.IPitchService;
import com.practice.productcatalogservice.service.IProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product product = iProductService.getProductById(id);
        return ResponseEntity.ok(from(product));
    }
    @GetMapping("{id}/pitch")
    public ResponseEntity<String> getProductPitch(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product product = iProductService.getProductById(id);
        String pitch = iPitchService.getPitch(product);
        return ResponseEntity.ok(pitch);
    }
    private ProductResponseDto from(Product product) {
        ProductResponseDto productResponseDto = new ProductResponseDto();
        productResponseDto.setName(product.getName());
        productResponseDto.setPrice(product.getPrice());
        productResponseDto.setDescription(product.getDescription());
        productResponseDto.setCategory(product.getCategory());
        return productResponseDto;
    }
}
