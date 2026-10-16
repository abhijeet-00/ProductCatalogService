package com.practice.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DummyProductServiceDto {
    private long id;
    private String title;
    private BigDecimal price;
    private String description;
    private String category;
    private List<String> images;
}
