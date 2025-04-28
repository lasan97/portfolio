package com.portfolio.backend.controller.user;

import com.portfolio.backend.controller.ControllerTest;
import com.portfolio.backend.service.user.dto.UserServiceResponse;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DisplayName("UserController 테스트")
class UserControllerTest extends ControllerTest {

    @Nested
    @DisplayName("내정보 조회 API")
    class GetCurrentUser {

        @SneakyThrows
        ResultActions getCurrentUser() {
            return mockMvc.perform(get("/api/users/me"))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 조회할 수 있다")
        @WithUserDetails
        void shouldGetUserProfileForAuthenticatedUser() throws Exception {
            // When
            ResultActions resultActions = getCurrentUser();

            // Then
            resultActions
                    .andExpect(status().isOk());

            UserServiceResponse.Profile response = resultActionsTo(resultActions, UserServiceResponse.Profile.class);

            assertThat(response)
                    .extracting(
                            UserServiceResponse.Profile::id,
                            UserServiceResponse.Profile::email
                    )
                    .containsExactly(
                            user.getId(),
                            user.getEmail()
                    );
        }

        @Test
        @DisplayName("인증되지않은 사용자는 조회할 수 없다")
        @WithAnonymousUser
        void shouldReturnForbiddenWhenUnauthorizedUserGetUserProfile() throws Exception {
            // When & Then
            getCurrentUser()
                    .andExpect(status().isForbidden());
        }
    }
}
