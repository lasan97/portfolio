package com.portfolio.backend.controller.user;

import com.portfolio.backend.controller.ControllerTest;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.fixture.UserCreditTestFixtures;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import com.portfolio.backend.service.user.fixture.UserCreditServiceRequestFixtures;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DisplayName("UserCreditController 테스트")
class UserCreditControllerTest extends ControllerTest {

    @Autowired
    private UserCreditRepository userCreditRepository;

    @BeforeEach
    void setUp() {
        userCreditRepository.save(UserCreditTestFixtures.createUserCredit(user));
    }

    @Nested
    @DisplayName("크레딧 충전 API")
    class Increase {

        @SneakyThrows
        ResultActions increase(UserCreditServiceRequest.Increase increase) {
            return mockMvc.perform(post("/api/users/credits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(increase)))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 크레딧을 충전할 수 있다")
        @WithUserDetails
        void shouldIncreaseCreditsForAuthenticatedUser() throws Exception {
            // Given
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(BigDecimal.valueOf(10000));

            // When
            ResultActions resultActions = increase(request);

            // Then
            resultActions
                    .andExpect(status().isOk());

            UserCredit credit = userCreditRepository.findLockedByUserId(user.getId()).get();
            assertThat(credit.getAmount()).isEqualTo(new Money(BigDecimal.valueOf(10000)));
        }

        @Test
        @DisplayName("인증되지않은 사용자는 크레딧을 충전할 수 없다")
        @WithAnonymousUser
        void shouldReturnForbiddenWhenUnauthorizedUserIncreasesCredit() throws Exception {
            // Given
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(BigDecimal.valueOf(10000));

            // When & Then
            increase(request)
                    .andExpect(status().isForbidden());
        }
    }
}
