package com.practice.productcatalogservice.repository;

import com.practice.productcatalogservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    @Query(value = "Select AVG(p.price) from Product p where p.category.name = :name")
    BigDecimal getAvgPriceByCategory(String name);
}
