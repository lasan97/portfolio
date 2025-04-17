package com.portfolio.backend.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockChangeReason {
	PURCHASE("구매"),
	SALE("판매"),
	RETURN("반품"),
	ADJUSTMENT("재고조정"),
	LOSS("손실");

	private final String description;
}
