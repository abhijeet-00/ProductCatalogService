package com.practice.productcatalogservice.dto;

import com.practice.productcatalogservice.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
}
