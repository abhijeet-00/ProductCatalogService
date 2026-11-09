package com.practice.productcatalogservice.repository;

import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndState(UUID id, State state);
    Page<Product> findByState(State state, Pageable pageable);
    @Query(value = "Select p from Product p LEFT JOIN FETCH p.category c")
    List<Product> findAllProducts();
    @EntityGraph(attributePaths = "category")
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
