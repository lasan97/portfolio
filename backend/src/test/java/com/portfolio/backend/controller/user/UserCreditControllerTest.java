package com.portfolio.backend.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.controller.product.ProductController;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.service.user.UserCreditService;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserCreditController.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenProvider.class
        )
)
@Import(com.portfolio.backend.common.security.TestSecurityConfig.class)
@DisplayName("UserCreditController 테스트")
class UserCreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserCreditService userCreditService;

    @Test
    @DisplayName("인증된 사용자는 크레딧을 충전할 수 있다")
    @WithMockUser(username = "test@example.com")
    void shouldChargeCreditsForAuthenticatedUser() throws Exception {
        // Given
        Long userId = 1L;
        UserImpl userImpl = new UserImpl(userId, "test@example.com", RoleType.USER);
        UserDetailsImpl userDetails = new UserDetailsImpl(userImpl);
        
        UserCreditServiceRequest.Increase request = new UserCreditServiceRequest.Increase(
                new Money(BigDecimal.valueOf(10000))
        );
        
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/users/credits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk());

        verify(userCreditService).increase(eq(userId), any(UserCreditServiceRequest.Increase.class));
    }

    @Test
    @DisplayName("유효하지 않은 요청(금액이 null)은 Bad Request를 반환한다")
    @WithMockUser(username = "test@example.com")
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        // Given
        String invalidRequestJson = "{\"amount\": null}";

        // When & Then
        mockMvc.perform(post("/api/users/credits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson)
                .with(SecurityMockMvcRequestPostProcessors.user(
                        new UserDetailsImpl(new UserImpl(1L, "test@example.com", RoleType.USER)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("금액이 빠진 요청은 Bad Request를 반환한다")
    @WithMockUser(username = "test@example.com")
    void shouldReturnBadRequestForMissingAmount() throws Exception {
        // Given
        String invalidRequestJson = "{}";

        // When & Then
        mockMvc.perform(post("/api/users/credits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson)
                .with(SecurityMockMvcRequestPostProcessors.user(
                        new UserDetailsImpl(new UserImpl(1L, "test@example.com", RoleType.USER)))))
                .andExpect(status().isBadRequest());
    }
}
