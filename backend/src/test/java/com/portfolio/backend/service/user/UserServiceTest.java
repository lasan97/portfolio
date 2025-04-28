package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.user.dto.UserServiceResponse.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserService 테스트")
class UserServiceTest extends ServiceTest {

    @Autowired
    private UserService userService;

    @Nested
    @DisplayName("사용자 프로필 조회 시")
    class GetUserProfileTest {

        @Test
        @DisplayName("존재하는 사용자 ID로 프로필을 조회할 수 있다")
        void shouldReturnProfileWhenUserExists() {
            // When
            Profile profile = userService.getUserProfile(user.getId());

            // Then
            assertThat(profile)
                    .extracting(
                            Profile::id,
                            Profile::email,
                            Profile::nickname,
                            Profile::profileImageUrl,
                            Profile::role)
                    .containsExactly(
                            user.getId(),
                            user.getEmail(),
                            user.getNickname(),
                            user.getProfileImageUrl(),
                            user.getRole());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 프로필을 조회하면 예외가 발생한다")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // When, Then
            assertThatThrownBy(() -> userService.getUserProfile(0L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("해당 사용자가 존재하지 않습니다");
        }
    }
}
