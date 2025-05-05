package com.portfolio.backend.service.common.dto;

import com.portfolio.backend.domain.common.value.Address;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class ServiceBaseResponse {

	public record ExternalLink(
			String name,
			String url,
			String logoUrl
	) {
		public ExternalLink(com.portfolio.backend.domain.introduction.value.ExternalLink externalLink) {
			this(externalLink.getName(), externalLink.getUrl(), externalLink.getLogoUrl());
		}
	}

	public record SimpleProduct(
			Long id,
			String name,
			BigDecimal price,
			BigDecimal originalPrice,
			String thumbnailImageUrl,
			ProductStatus status
	) {
	}

	public record Address(
			String address,
			String detailAddress,
			String postCode
	) {
		public Address(com.portfolio.backend.domain.common.value.Address address) {
			this(address.getAddress(), address.getDetailAddress(), address.getPostCode());
		}
	}
}
