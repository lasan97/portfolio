package com.portfolio.backend.domain.product.repository;

import com.portfolio.backend.domain.product.entity.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductStock> findLockedByProductId(Long productId);

    Optional<ProductStock> findByProductId(Long productId);
}
