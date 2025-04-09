package com.portfolio.backend.common.integration.oauth2.github.dto;

import java.net.URI;

public class GithubResponse {

	public record EmailDto(
			String email,
			boolean primary,
			boolean verified,
			String visibility
	) {}

	public record UserDto(
			String name,                // 선택적: nullable
			String email,               // 선택적: nullable
			String login,               // 필수
			long id,                    // 필수
			String node_id,             // 필수
			URI avatar_url,             // 필수
			String gravatar_id,         // 선택적: nullable
			URI url,                    // 필수
			URI html_url,               // 필수
			URI followers_url,          // 필수
			String following_url,       // 필수
			String gists_url,           // 필수
			String starred_url,         // 필수
			URI subscriptions_url,      // 필수
			URI organizations_url,      // 필수
			URI repos_url,              // 필수
			String events_url,          // 필수
			URI received_events_url,    // 필수
			String type,                // 필수
			boolean site_admin,         // 필수
			String starred_at,          // 선택적: nullable
			String user_view_type       // 선택적: nullable
	) {}


}
