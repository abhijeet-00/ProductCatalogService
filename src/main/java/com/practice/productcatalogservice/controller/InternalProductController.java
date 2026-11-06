package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.dto.ProductDto;
import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.service.IProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {
    private final IProductService iProductServiceExternal;

    public InternalProductController(@Qualifier("dummyProductService") IProductService iProductServiceExternal) {
        this.iProductServiceExternal = iProductServiceExternal;
    }
    @PostMapping("/load")
    public ResponseEntity<String> loadAllProducts() {
        int count = iProductServiceExternal.loadAllProducts();
        return new ResponseEntity<>("Total " + count + " products loaded from external service.", HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getDummyProduct(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Product product = iProductServiceExternal.getProductById(id);
        return ResponseEntity.ok(ProductDto.from(product));
    }
}
