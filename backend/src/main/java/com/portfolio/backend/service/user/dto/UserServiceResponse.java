package com.portfolio.backend.service.user.dto;

import com.portfolio.backend.domain.user.entity.RoleType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserServiceResponse {

    public record Profile(
            Long id,
            String email,
            String nickname,
            String profileImageUrl,
            RoleType role
    ) {}
}
