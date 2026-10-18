package com.practice.productcatalogservice.controller;

import com.practice.productcatalogservice.service.IProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
