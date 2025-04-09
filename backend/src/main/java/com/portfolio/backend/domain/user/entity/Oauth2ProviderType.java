package com.portfolio.backend.domain.user.entity;

import lombok.Getter;

@Getter
public enum Oauth2ProviderType {

	GOOGLE("google"),
	FACEBOOK("facebook"),
	GITHUB("github");

	private final String registrationId;

	Oauth2ProviderType(String registrationId) {
		this.registrationId = registrationId;
	}

	public static Oauth2ProviderType fromRegistrationId(String registrationId) {
		for (Oauth2ProviderType provider : values()) {
			if (provider.getRegistrationId().equalsIgnoreCase(registrationId)) {
				return provider;
			}
		}
		throw new IllegalArgumentException("Unsupported registrationId: " + registrationId);
	}

}
