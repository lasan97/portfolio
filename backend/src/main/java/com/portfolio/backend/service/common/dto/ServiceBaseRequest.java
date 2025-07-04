package com.portfolio.backend.service.common.dto;

import jakarta.validation.constraints.NotBlank;

public class ServiceBaseRequest {

	public record Address(
			@NotBlank(message = "주소는 필수입니다.")
			String address,
			String detailAddress,
			@NotBlank(message = "우편번호는 필수입니다.")
			String postCode
	) { }

	public record ExternalLink(
			@NotBlank(message = "외부 링크 이름은 필수 입력 항목입니다")
			String name,
			@NotBlank(message = "외부 링크 주소는 필수 입력 항목입니다")
			String url,
			String logoUrl
	) {}
}
