package com.practice.productcatalogservice.dto;

import com.practice.productcatalogservice.model.Category;
import com.practice.productcatalogservice.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductDto {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String description;
    private CategoryDto categoryDto;
    public static ProductDto from (Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPrice(product.getPrice());
        productDto.setDescription(product.getDescription());
        productDto.setCategoryDto(fromCategory(product.getCategory()));
        return productDto;
    }
    private static CategoryDto fromCategory(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        categoryDto.setDescription(category.getDescription());
        return categoryDto;
    }
}
