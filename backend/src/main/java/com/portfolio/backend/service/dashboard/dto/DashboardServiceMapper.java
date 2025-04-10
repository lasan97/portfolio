package com.portfolio.backend.service.dashboard.dto;

import com.portfolio.backend.domain.dashboard.entity.Dashboard;
import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceResponse.Get;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardServiceMapper {

	public Get toGet(Dashboard dashboard) {
		return new Get(dashboard.getId(),
				dashboard.getTitle(),
				dashboard.getContent(),
				dashboard.getExternalLinks().stream().map(ServiceBaseResponse.ExternalLink::new).toList(),
				dashboard.getUpdatedAt());
	}
}
