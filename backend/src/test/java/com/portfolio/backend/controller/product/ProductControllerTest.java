package com.portfolio.backend.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.service.product.ProductService;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductController에 대한 웹 계층 테스트
 */
@WebMvcTest(
    controllers = ProductController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtTokenProvider.class
    )
)
@Import(com.portfolio.backend.common.security.TestSecurityConfig.class)
@DisplayName("ProductController 테스트")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // 테스트에 사용할 사용자 정보
    private UserDetailsImpl adminUserDetails;
    private UserDetailsImpl regularUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 설정
        UserImpl adminUserImpl = new UserImpl(TestFixtures.USER_ID_1, "admin@example.com", RoleType.ADMIN);
        adminUserDetails = new UserDetailsImpl(adminUserImpl);
        
        UserImpl userUserImpl = new UserImpl(TestFixtures.USER_ID_2, "user@example.com", RoleType.USER);
        regularUserDetails = new UserDetailsImpl(userUserImpl);
    }

    @Nested
    @DisplayName("상품 목록 조회 API")
    class GetProductsApiTest {

        @Test
        @DisplayName("인증 없이 상품 목록을 성공적으로 조회할 수 있다")
        @WithAnonymousUser
        void shouldReturnProductListWithoutAuthentication() throws Exception {
            // Given
            List<ProductServiceResponse.GetList> productList = TestFixtures.createProductGetListResponseList();
            when(productService.getProducts()).thenReturn(productList);

            // When & Then
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("맥북 프로")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("아이폰 15")));

            verify(productService).getProducts();
        }
    }

    @Nested
    @DisplayName("상품 상세 조회 API")
    class GetProductApiTest {

        @Test
        @DisplayName("인증 없이 상품 상세를 성공적으로 조회할 수 있다")
        @WithAnonymousUser
        void shouldReturnProductDetailWithoutAuthentication() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceResponse.Get productGetResponse = TestFixtures.createProductGetResponse();
            when(productService.getProduct(productId)).thenReturn(productGetResponse);

            // When & Then
            mockMvc.perform(get("/api/products/{id}", productId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("맥북 프로")))
                .andExpect(jsonPath("$.category", is("ELECTRONICS")))
                .andExpect(jsonPath("$.stock", is(10)));

            verify(productService).getProduct(productId);
        }
    }

    @Nested
    @DisplayName("상품 생성 API")
    class CreateProductApiTest {

        @Test
        @DisplayName("ADMIN 권한으로 상품을 성공적으로 생성할 수 있다")
        void shouldCreateProductWithAdminRole() throws Exception {
            // Given
            Long createdProductId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.Create createRequest = TestFixtures.createProductCreateRequest();
            when(productService.createProduct(any(ProductServiceRequest.Create.class))).thenReturn(createdProductId);

            // When & Then
            performWithAdminRoleAndExpect(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)),
                status().isOk()
            )
            .andExpect(content().string(createdProductId.toString()));

            verify(productService).createProduct(any(ProductServiceRequest.Create.class));
        }

        @Test
        @DisplayName("USER 권한으로 상품 생성 시 접근이 거부된다")
        void shouldDenyCreateProductWithUserRole() throws Exception {
            // Given
            ProductServiceRequest.Create createRequest = TestFixtures.createProductCreateRequest();

            // When & Then
            performWithUserRoleAndExpect(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)),
                status().isForbidden()
            );
        }

        @Test
        @DisplayName("인증 없이 상품 생성 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForCreateProduct() throws Exception {
            // Given
            ProductServiceRequest.Create createRequest = TestFixtures.createProductCreateRequest();

            // When & Then
            mockMvc.perform(post("/api/products")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                        status == 401 || status == 403,
                        "Expected status 401 or 403, but was " + status
                    );
                });
        }

        @Test
        @DisplayName("잘못된 요청으로 상품 생성 시 검증 오류가 발생한다")
        void shouldValidateCreateProductRequest() throws Exception {
            // Given
            ProductServiceRequest.Create invalidRequest = new ProductServiceRequest.Create(
                    "", // 비어있는 상품명
                    null,
                    null,
                    "",
                    "",
                    null,
                    0
            );

            // When & Then
            performWithAdminRoleAndExpect(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
                status().isBadRequest()
            );
        }
    }

    @Nested
    @DisplayName("상품 수정 API")
    class UpdateProductApiTest {

        @Test
        @DisplayName("ADMIN 권한으로 상품을 성공적으로 수정할 수 있다")
        void shouldUpdateProductWithAdminRole() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.Update updateRequest = TestFixtures.createProductUpdateRequest();
            when(productService.updateProduct(eq(productId), any(ProductServiceRequest.Update.class))).thenReturn(productId);

            // When & Then
            performWithAdminRoleAndExpect(
                put("/api/products/{id}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
                status().isOk()
            )
            .andExpect(content().string(productId.toString()));

            verify(productService).updateProduct(eq(productId), any(ProductServiceRequest.Update.class));
        }

        @Test
        @DisplayName("USER 권한으로 상품 수정 시 접근이 거부된다")
        void shouldDenyUpdateProductWithUserRole() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.Update updateRequest = TestFixtures.createProductUpdateRequest();

            // When & Then
            performWithUserRoleAndExpect(
                put("/api/products/{id}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
                status().isForbidden()
            );
        }

        @Test
        @DisplayName("인증 없이 상품 수정 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForUpdateProduct() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.Update updateRequest = TestFixtures.createProductUpdateRequest();

            // When & Then
            mockMvc.perform(put("/api/products/{id}", productId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                        status == 401 || status == 403,
                        "Expected status 401 or 403, but was " + status
                    );
                });
        }
    }

    @Nested
    @DisplayName("상품 삭제 API")
    class DeleteProductApiTest {

        @Test
        @DisplayName("ADMIN 권한으로 상품을 성공적으로 삭제할 수 있다")
        void shouldDeleteProductWithAdminRole() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;

            // When & Then
            performWithAdminRoleAndExpect(
                delete("/api/products/{id}", productId),
                status().isOk()
            );

            verify(productService).deleteProduct(productId);
        }

        @Test
        @DisplayName("USER 권한으로 상품 삭제 시 접근이 거부된다")
        void shouldDenyDeleteProductWithUserRole() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;

            // When & Then
            performWithUserRoleAndExpect(
                delete("/api/products/{id}", productId),
                status().isForbidden()
            );
        }

        @Test
        @DisplayName("인증 없이 상품 삭제 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForDeleteProduct() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;

            // When & Then
            mockMvc.perform(delete("/api/products/{id}", productId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                        status == 401 || status == 403,
                        "Expected status 401 or 403, but was " + status
                    );
                });
        }
    }

    @Nested
    @DisplayName("재고 조정 API")
    class AdjustStockApiTest {

        @ParameterizedTest
        @CsvSource({
            "5, ADJUSTMENT, '재고 추가'",
            "-3, LOSS, '재고 손실'"
        })
        @DisplayName("USER 권한으로 재고를 성공적으로 조정할 수 있다")
        void shouldAdjustStockWithUserRole(int quantity, String reason, String description) throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.AdjustStock request = TestFixtures.createStockAdjustRequest(
                quantity, 
                com.portfolio.backend.service.product.dto.StockChangeReason.valueOf(reason), 
                description
            );

            // When & Then
            performWithUserRoleAndExpect(
                patch("/api/products/{id}/stock", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
                status().isOk()
            );

            verify(productService).adjustStock(eq(productId), any(ProductServiceRequest.AdjustStock.class));
        }

        @Test
        @DisplayName("ADMIN 권한으로도 재고를 성공적으로 조정할 수 있다")
        void shouldAdjustStockWithAdminRole() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.AdjustStock request = TestFixtures.createStockAdjustRequest(
                5, 
                com.portfolio.backend.service.product.dto.StockChangeReason.ADJUSTMENT, 
                "재고 추가"
            );

            // When & Then
            performWithAdminRoleAndExpect(
                patch("/api/products/{id}/stock", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
                status().isOk()
            );

            verify(productService).adjustStock(eq(productId), any(ProductServiceRequest.AdjustStock.class));
        }

        @Test
        @DisplayName("인증 없이 재고 조정 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForAdjustStock() throws Exception {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            ProductServiceRequest.AdjustStock request = TestFixtures.createStockAdjustRequest(
                5, 
                com.portfolio.backend.service.product.dto.StockChangeReason.ADJUSTMENT, 
                "재고 추가"
            );

            // When & Then
            mockMvc.perform(patch("/api/products/{id}/stock", productId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                        status == 401 || status == 403,
                        "Expected status 401 or 403, but was " + status
                    );
                });
        }
    }
    
    // 헬퍼 메소드 - 관리자 권한으로 요청 수행 및 결과 검증
    private ResultActions performWithAdminRoleAndExpect(MockHttpServletRequestBuilder request, ResultMatcher expectedStatus) throws Exception {
        return mockMvc.perform(request
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
            .andDo(print())
            .andExpect(expectedStatus);
    }
    
    // 헬퍼 메소드 - 일반 사용자 권한으로 요청 수행 및 결과 검증
    private ResultActions performWithUserRoleAndExpect(MockHttpServletRequestBuilder request, ResultMatcher expectedStatus) throws Exception {
        return mockMvc.perform(request
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(regularUserDetails)))
            .andDo(print())
            .andExpect(expectedStatus);
    }
}