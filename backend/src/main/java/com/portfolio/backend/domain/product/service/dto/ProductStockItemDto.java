package com.portfolio.backend.domain.product.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductStockItemDto {

    private Long productId;
    private Integer quantity;
}
