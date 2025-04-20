package com.portfolio.backend.domain.cart.repository;

import com.portfolio.backend.domain.cart.entity.ProductCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCartRepository extends JpaRepository<ProductCart, Long> {

    Optional<ProductCart> findByUserId(Long userId);
}
