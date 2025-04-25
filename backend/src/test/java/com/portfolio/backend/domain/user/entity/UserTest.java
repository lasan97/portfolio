package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.user.event.UserCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Nested
    @DisplayName("User 생성 시")
    class CreateUserTest {

        @Test
        @DisplayName("정상적인 파라미터로 User를 생성할 수 있다")
        void shouldCreateUserWithValidParameters() {
            // Given
            String email = "test@email.com";
            String nickname = "tester";
            Oauth2ProviderType provider = Oauth2ProviderType.GITHUB;
            String providerId = "12345";
            String profileImageUrl = "https://example.com/profile.jpg";
            RoleType role = RoleType.USER;

            // When
            User user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .provider(provider)
                    .providerId(providerId)
                    .profileImageUrl(profileImageUrl)
                    .role(role)
                    .build();

            // Then
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getNickname()).isEqualTo(nickname);
            assertThat(user.getProvider()).isEqualTo(provider);
            assertThat(user.getProviderId()).isEqualTo(providerId);
            assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
            assertThat(user.getRole()).isEqualTo(role);
            
            // 이벤트가 등록되었는지 확인
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserCreatedEvent.class);
        }

        @Test
        @DisplayName("provider가 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenProviderIsNull() {
            // Given & When & Then
            assertThatThrownBy(() -> User.builder()
                    .email("test@email.com")
                    .nickname("tester")
                    .provider(null)
                    .providerId("12345")
                    .profileImageUrl("https://example.com/profile.jpg")
                    .role(RoleType.USER)
                    .build())
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("연동 채널은 null일 수 없습니다");
        }

        @Test
        @DisplayName("providerId가 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenProviderIdIsNull() {
            // Given & When & Then
            assertThatThrownBy(() -> User.builder()
                    .email("test@email.com")
                    .nickname("tester")
                    .provider(Oauth2ProviderType.GITHUB)
                    .providerId(null)
                    .profileImageUrl("https://example.com/profile.jpg")
                    .role(RoleType.USER)
                    .build())
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("연동 아이디는 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("providerId가 빈 문자열이면 예외가 발생한다")
        void shouldThrowExceptionWhenProviderIdIsEmpty() {
            // Given & When & Then
            assertThatThrownBy(() -> User.builder()
                    .email("test@email.com")
                    .nickname("tester")
                    .provider(Oauth2ProviderType.GITHUB)
                    .providerId("")
                    .profileImageUrl("https://example.com/profile.jpg")
                    .role(RoleType.USER)
                    .build())
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("연동 아이디는 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("role이 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenRoleIsNull() {
            // Given & When & Then
            assertThatThrownBy(() -> User.builder()
                    .email("test@email.com")
                    .nickname("tester")
                    .provider(Oauth2ProviderType.GITHUB)
                    .providerId("12345")
                    .profileImageUrl("https://example.com/profile.jpg")
                    .role(null)
                    .build())
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("권한은 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("User 프로필 업데이트 시")
    class UpdateProfileTest {

        @Test
        @DisplayName("닉네임과 프로필 이미지를 업데이트할 수 있다")
        void shouldUpdateNicknameAndProfileImage() {
            // Given
            User user = TestFixtures.createRegularUser();
            String newNickname = "newTester";
            String newProfileImageUrl = "https://example.com/new-profile.jpg";

            // When
            user.updateProfile(newNickname, newProfileImageUrl);

            // Then
            assertThat(user.getNickname()).isEqualTo(newNickname);
            assertThat(user.getProfileImageUrl()).isEqualTo(newProfileImageUrl);
        }
    }
}
