package com.portfolio.backend.domain.dashboard.value;

import com.portfolio.backend.common.exception.DomainException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ExternalLink implements Serializable {

	private String name;
	private String url;
	private String logoUrl;

	public ExternalLink(String name, String url, String logoUrl) {
		if(name == null || name.isBlank()) {
			throw new DomainException("이름이 null일 수 없습니다.");
		}
		if(url == null || url.isBlank()) {
			throw new DomainException("주소가 null일 수 없습니다.");
		}

		this.name = name;
		this.url = url;
		this.logoUrl = logoUrl;
	}
}
