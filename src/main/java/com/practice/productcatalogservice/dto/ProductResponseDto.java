package com.practice.productcatalogservice.dto;

import com.practice.productcatalogservice.model.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {
    private String name;
    private double price;
    private String description;
    private Category category;
}
