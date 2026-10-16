package com.practice.productcatalogservice.repository;

import com.practice.productcatalogservice.model.Product;
import com.practice.productcatalogservice.model.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndState(Long id, State state);
    Page<Product> findByState(State state, Pageable pageable);
}
