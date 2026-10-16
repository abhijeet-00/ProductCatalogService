package com.practice.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DummyAllProductsDto {
    List<DummyProductServiceDto> products;
}
