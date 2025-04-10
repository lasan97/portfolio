package com.portfolio.backend.service.dashboard.dto;

import com.portfolio.backend.service.common.dto.ServiceBaseResponse;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardServiceResponse {

	public record Get(
			Long id,
			String title,
			String content,
			List<ServiceBaseResponse.ExternalLink> externalLinks,
			LocalDateTime updatedAt
	) {}
}
