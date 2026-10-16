package com.practice.productcatalogservice.service;

import java.math.BigDecimal;

public interface ICategoryService {
    BigDecimal getAvgPriceByCategory(String categoryName);
}