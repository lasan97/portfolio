package com.portfolio.backend.service.auth.dto;


public class OAuth2ServiceResponse {

	public record TokenResponse(
			String token
	) { }
}
