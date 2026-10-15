package com.practice.productcatalogservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Product extends BaseEntity {
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    @ManyToOne(cascade = CascadeType.ALL)
    private Category category;
}
