package com.portfolio.backend.service.product.dto;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;

import java.time.LocalDateTime;

public class ProductServiceResponse {

    public record Get(
            Long id,
            String name,
            Money originalPrice,
            Money price,
            String description,
            String thumbnailImageUrl,
            ProductCategory category,
            ProductStatus status,
            Integer stock,
            Integer discountRate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record GetList(
            Long id,
            String name,
            Money originalPrice,
            Money price,
            String thumbnailImageUrl,
            ProductCategory category,
            ProductStatus status,
            Integer stock,
            Integer discountRate
    ) {
    }
}
