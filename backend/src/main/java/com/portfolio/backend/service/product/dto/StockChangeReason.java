package com.portfolio.backend.service.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockChangeReason {
    ADJUSTMENT("재고조정"),
    LOSS("손실");

    private final String description;
}
