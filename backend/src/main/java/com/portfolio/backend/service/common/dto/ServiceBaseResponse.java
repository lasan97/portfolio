package com.portfolio.backend.service.common.dto;

import jakarta.validation.constraints.NotBlank;

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
}
