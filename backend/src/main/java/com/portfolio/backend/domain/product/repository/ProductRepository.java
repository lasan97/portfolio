package com.portfolio.backend.domain.product.repository;

import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByStatusNot(ProductStatus status);
    Optional<Product> findByIdAndStatusNot(Long id, ProductStatus status);
}
