package com.portfolio.backend.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.service.cart.ProductCartService;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest;
import com.portfolio.backend.service.cart.dto.ProductCartServiceResponse;
import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductCartController에 대한 웹 계층 테스트
 */
@WebMvcTest(
    controllers = ProductCartController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtTokenProvider.class
    )
)
@Import(com.portfolio.backend.common.security.TestSecurityConfig.class)
@DisplayName("ProductCartController 테스트")
class ProductCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductCartService productCartService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // 테스트 데이터
    private ProductCartServiceResponse.Get cartItem1;
    private ProductCartServiceResponse.Get cartItem2;
    private List<ProductCartServiceResponse.Get> cartItems;
    private ProductCartServiceRequest.AddItem addItemRequest;
    private ProductCartServiceRequest.RemoveItem removeItemRequest;
    private UserDetailsImpl regularUserDetails;
    private UserDetailsImpl adminUserDetails;
    private UserImpl regularUserImpl;
    private UserImpl adminUserImpl;

    @BeforeEach
    void setUp() {
        // 사용자 설정
        regularUserImpl = new UserImpl(TestFixtures.USER_ID_1, "user@example.com", RoleType.USER);
        regularUserDetails = new UserDetailsImpl(regularUserImpl);
        
        adminUserImpl = new UserImpl(TestFixtures.USER_ID_2, "admin@example.com", RoleType.ADMIN);
        adminUserDetails = new UserDetailsImpl(adminUserImpl);
        
        // 장바구니 응답 항목 설정
        ServiceBaseResponse.SimpleProduct simpleProduct1 = new ServiceBaseResponse.SimpleProduct(
                TestFixtures.PRODUCT_ID_1, "맥북 프로", 
                BigDecimal.valueOf(1800000), BigDecimal.valueOf(2000000),
                null,
                ProductStatus.ACTIVE
        );
        
        ServiceBaseResponse.SimpleProduct simpleProduct2 = new ServiceBaseResponse.SimpleProduct(
                TestFixtures.PRODUCT_ID_2, "아이폰 15", 
                BigDecimal.valueOf(1500000), BigDecimal.valueOf(1700000),
                null,
                ProductStatus.ACTIVE
        );
        
        cartItem1 = new ProductCartServiceResponse.Get(simpleProduct1, 1);
        cartItem2 = new ProductCartServiceResponse.Get(simpleProduct2, 2);
        cartItems = List.of(cartItem1, cartItem2);
        
        // 요청 객체 설정
        addItemRequest = new ProductCartServiceRequest.AddItem(TestFixtures.PRODUCT_ID_1, 3);
        removeItemRequest = new ProductCartServiceRequest.RemoveItem(TestFixtures.PRODUCT_ID_1);
    }

    @Nested
    @DisplayName("장바구니 목록 조회 API")
    class GetCartItemsApiTest {

        @Test
        @DisplayName("인증된 사용자는 장바구니 목록을 성공적으로 조회할 수 있다")
        void shouldGetCartItemsWithAuthentication() throws Exception {
            // Given
            when(productCartService.getCartItems(any(UserImpl.class))).thenReturn(cartItems);

            // When & Then
            performWithUserRoleAndExpect(
                get("/api/cart"),
                status().isOk()
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].product.id", is(1)))
            .andExpect(jsonPath("$[0].product.name", is("맥북 프로")))
            .andExpect(jsonPath("$[0].quantity", is(1)))
            .andExpect(jsonPath("$[1].product.id", is(2)))
            .andExpect(jsonPath("$[1].product.name", is("아이폰 15")))
            .andExpect(jsonPath("$[1].quantity", is(2)));

            verify(productCartService).getCartItems(any(UserImpl.class));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 장바구니 목록 조회 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldDenyGetCartItemsWithoutAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 추가 API")
    class AddCartItemApiTest {

        @Test
        @DisplayName("인증된 사용자는 장바구니에 아이템을 성공적으로 추가할 수 있다")
        void shouldAddCartItemWithAuthentication() throws Exception {
            // When & Then
            performWithUserRoleAndExpect(
                post("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addItemRequest)),
                status().isOk()
            );

            verify(productCartService).addCartItem(any(ProductCartServiceRequest.AddItem.class), any(UserImpl.class));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 장바구니에 아이템 추가 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldDenyAddCartItemWithoutAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/cart")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addItemRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("유효하지 않은 요청으로 장바구니에 아이템 추가 시 검증 오류가 발생한다")
        void shouldValidateAddCartItemRequest() throws Exception {
            // Given
            ProductCartServiceRequest.AddItem invalidRequest = new ProductCartServiceRequest.AddItem(
                TestFixtures.PRODUCT_ID_1, -1  // 음수 수량은 유효하지 않음
            );

            // When & Then
            performWithUserRoleAndExpect(
                post("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
                status().isBadRequest()
            );
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 제거 API")
    class RemoveCartItemApiTest {

        @Test
        @DisplayName("인증된 사용자는 장바구니에서 아이템을 성공적으로 제거할 수 있다")
        void shouldRemoveCartItemWithAuthentication() throws Exception {
            // When & Then
            performWithUserRoleAndExpect(
                delete("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(removeItemRequest)),
                status().isOk()
            );

            verify(productCartService).removeCartItem(any(ProductCartServiceRequest.RemoveItem.class), any(UserImpl.class));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 장바구니에서 아이템 제거 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldDenyRemoveCartItemWithoutAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/cart")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(removeItemRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("관리자 권한 테스트")
    class AdminRoleApiTest {

        @Test
        @DisplayName("관리자 권한으로 장바구니 목록을 성공적으로 조회할 수 있다")
        void shouldGetCartItemsWithAdminRole() throws Exception {
            // Given
            when(productCartService.getCartItems(any(UserImpl.class))).thenReturn(cartItems);

            // When & Then
            performWithAdminRoleAndExpect(
                get("/api/cart"),
                status().isOk()
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)));

            verify(productCartService).getCartItems(any(UserImpl.class));
        }
    }
    
    // 헬퍼 메소드 - 일반 사용자 권한으로 요청 수행 및 결과 검증
    private ResultActions performWithUserRoleAndExpect(MockHttpServletRequestBuilder request, ResultMatcher expectedStatus) throws Exception {
        return mockMvc.perform(request
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(regularUserDetails)))
            .andDo(print())
            .andExpect(expectedStatus);
    }
    
    // 헬퍼 메소드 - 관리자 권한으로 요청 수행 및 결과 검증
    private ResultActions performWithAdminRoleAndExpect(MockHttpServletRequestBuilder request, ResultMatcher expectedStatus) throws Exception {
        return mockMvc.perform(request
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
            .andDo(print())
            .andExpect(expectedStatus);
    }
}