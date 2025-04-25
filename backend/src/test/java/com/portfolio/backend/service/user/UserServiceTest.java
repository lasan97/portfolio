package com.portfolio.backend.service.user;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.user.dto.UserServiceResponse.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("사용자 프로필 조회 시")
    class GetUserProfileTest {

        @Test
        @DisplayName("존재하는 사용자 ID로 프로필을 조회할 수 있다")
        void shouldReturnProfileWhenUserExists() {
            // Given
            Long userId = TestFixtures.USER_ID_1;
            User user = TestFixtures.createRegularUser();
            
            // User ID 설정을 위한 리플렉션
            try {
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, userId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set user ID", e);
            }
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            Profile profile = userService.getUserProfile(userId);

            // Then
            assertThat(profile.id()).isEqualTo(userId);
            assertThat(profile.email()).isEqualTo(user.getEmail());
            assertThat(profile.nickname()).isEqualTo(user.getNickname());
            assertThat(profile.profileImageUrl()).isEqualTo(user.getProfileImageUrl());
            assertThat(profile.role()).isEqualTo(user.getRole());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 프로필을 조회하면 예외가 발생한다")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(() -> userService.getUserProfile(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("해당 사용자가 존재하지 않습니다");
        }
    }
}
