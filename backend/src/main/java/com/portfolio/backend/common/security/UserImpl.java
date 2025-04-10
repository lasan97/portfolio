package com.portfolio.backend.common.security;

import com.portfolio.backend.domain.user.entity.RoleType;

public record UserImpl(
		Long id,
		String email,
		RoleType role
) {}
