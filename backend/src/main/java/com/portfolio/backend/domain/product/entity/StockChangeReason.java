package com.portfolio.backend.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockChangeReason {
	SALE("판매"),
	RETURN("반품"),
	ADJUSTMENT("재고조정"),
	LOSS("손실"),
	STOP_SALE("판매 중지");

	private final String description;
}
