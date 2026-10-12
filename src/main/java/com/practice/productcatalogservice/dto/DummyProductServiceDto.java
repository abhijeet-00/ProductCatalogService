package com.practice.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DummyProductServiceDto {
    private long id;
    private String title;
    private double price;
    private String description;
    private String category;
}
