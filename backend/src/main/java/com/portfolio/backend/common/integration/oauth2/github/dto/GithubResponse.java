package com.portfolio.backend.common.integration.oauth2.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class GithubResponse {

	public record TokenDto(
			@JsonProperty("access_token") String accessToken,
			@JsonProperty("token_type") String tokenType,
			String scope
	) {}

	public record EmailDto(
			String email,
			boolean primary,
			boolean verified,
			String visibility
	) {}

	public record UserDto(
			String name,                                                // 선택적: nullable
			String email,                                               // 선택적: nullable
			String login,                                               // 필수
			long id,                                                    // 필수
			@JsonProperty("node_id") String nodeId,                     // 필수
			@JsonProperty("avatar_url") URI avatarUrl,                  // 필수
			@JsonProperty("gravatar_id") String gravatarId,             // 선택적: nullable
			URI url,                                                    // 필수
			@JsonProperty("html_url") URI htmlUrl,                      // 필수
			@JsonProperty("followers_url") URI followersUrl,            // 필수
			@JsonProperty("following_url") String followingUrl,         // 필수
			@JsonProperty("gists_url") String gistsUrl,                 // 필수
			@JsonProperty("starred_url") String starredUrl,             // 필수
			@JsonProperty("subscriptions_url") URI subscriptionsUrl,    // 필수
			@JsonProperty("organizations_url") URI organizationsUrl,    // 필수
			@JsonProperty("repos_url") URI reposUrl,                    // 필수
			@JsonProperty("events_url") String eventsUrl,               // 필수
			@JsonProperty("received_events_url") URI receivedEventsUrl, // 필수
			String type,                                                // 필수
			@JsonProperty("site_admi n") boolean siteAdmin,             // 필수
			@JsonProperty("starred_a t") String starredAt,              // 선택적: nullable
			@JsonProperty("user_view_typ e") String userViewType        // 선택적: nullable
	) {}
}
