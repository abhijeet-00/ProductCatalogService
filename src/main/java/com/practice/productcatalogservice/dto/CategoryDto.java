package com.practice.productcatalogservice.dto;

import com.practice.productcatalogservice.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryDto {
    private String name;
    private String description;
}
