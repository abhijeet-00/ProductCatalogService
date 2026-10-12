package com.practice.productcatalogservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Category extends  BaseEntity {
    private String name;
    private String description;
    private List<Product> productList;
}
