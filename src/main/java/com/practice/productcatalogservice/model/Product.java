package com.practice.productcatalogservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product extends BaseEntity {
    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private Category category;
}
