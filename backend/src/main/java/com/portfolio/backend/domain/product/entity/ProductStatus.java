package com.portfolio.backend.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
	ACTIVE("판매중"),
	SOLD_OUT("품절"),
	DELETED("삭제됨");

	private final String description;
}
