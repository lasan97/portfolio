package com.portfolio.backend.common.integration.oauth2.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.integration.oauth2.github.dto.GithubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GithubIntegration {

	private final RestTemplate restTemplate;

	@Value("${spring.security.oauth2.client.registration.github.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.github.client-secret}")
	private String clientSecret;

	private final String accessTokenUrl = "https://github.com/login/oauth/access_token";
	private final String userInfoUrl = "https://api.github.com/user";
	private final String userEmailUrl = "https://api.github.com/user/emails";

	/**
	 * GitHub 인증 코드로 액세스 토큰 요청
	 */
	public String getAccessToken(String code) {

		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "application/json");

		// 요청 파라미터 설정
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("code", code);

		// HTTP 엔티티 생성
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

		// API 요청
		ResponseEntity<GithubResponse.TokenDto> responseEntity = restTemplate.exchange(
				accessTokenUrl,
				HttpMethod.POST,
				requestEntity,
				GithubResponse.TokenDto.class
		);

		// 응답에서 액세스 토큰 추출
		GithubResponse.TokenDto responseBody = responseEntity.getBody();
		if (responseBody != null && responseBody.accessToken() != null) {
			return responseBody.accessToken();
		}

		throw new RuntimeException("GitHub 액세스 토큰을 가져오는데 실패했습니다.");
	}

	/**
	 * GitHub 액세스 토큰으로 사용자 정보 요청
	 */
	public GithubResponse.UserDto getUserInfo(String accessToken) {

		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		// HTTP 엔티티 생성
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		// API 요청
		ResponseEntity<GithubResponse.UserDto> responseEntity = restTemplate.exchange(
				userInfoUrl,
				HttpMethod.GET,
				requestEntity,
				GithubResponse.UserDto.class
		);

		// 응답에서 사용자 정보 반환
		return responseEntity.getBody();
	}

	/**
	 * GitHub 액세스 토큰으로 사용자 이메일 정보 요청
	 */
	public String getUserPrimaryEmail(String accessToken) {

		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		// HTTP 엔티티 생성
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		// API 요청
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				userEmailUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
		);

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			List<GithubResponse.EmailDto> userEmails = objectMapper.readValue(
					responseEntity.getBody(),
					objectMapper.getTypeFactory().constructCollectionType(List.class, GithubResponse.EmailDto.class)
			);

			return getPrimaryEmail(userEmails);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse GitHub email API response.", e);
		}
	}

	private String getPrimaryEmail(List<GithubResponse.EmailDto> userEmails) {
		return userEmails.stream().filter(info -> info.primary())
				.findFirst().orElseThrow(() -> new RuntimeException("No primary email found in the provided user email list."))
				.email();
	}
}
