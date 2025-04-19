package com.portfolio.backend.domain.product.repository;

import com.portfolio.backend.domain.product.entity.ProductStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockHistoryRepository extends JpaRepository<ProductStockHistory, Long> {
}