package com.portfolio.backend.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.service.product.ProductService;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;
import com.portfolio.backend.service.product.dto.StockChangeReason;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
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

@WebMvcTest(
    controllers = ProductController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtTokenProvider.class
    )
)
@Import(com.portfolio.backend.common.security.TestSecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // 테스트 데이터
    private ProductServiceResponse.Get productGetResponse;
    private ProductServiceResponse.GetList productGetListResponse1;
    private ProductServiceResponse.GetList productGetListResponse2;
    private List<ProductServiceResponse.GetList> productGetListRespons;
    private ProductServiceRequest.Create createRequest;
    private ProductServiceRequest.Update updateRequest;
    private ProductServiceRequest.AdjustStock adjustStockRequest;

    @BeforeEach
    void setUp() {
        productGetResponse = new ProductServiceResponse.Get(
                1L, "맥북 프로", new Money(BigDecimal.valueOf(2000000)), new Money(BigDecimal.valueOf(1800000)),
                "Apple 맥북 프로 M3 칩", "macbook-pro.jpg", ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE, 10, 10, LocalDateTime.now(), LocalDateTime.now()
        );

        productGetListResponse1 = new ProductServiceResponse.GetList(
                1L, "맥북 프로", new Money(BigDecimal.valueOf(1800000)), new Money(BigDecimal.valueOf(1620000)),
                "macbook-pro.jpg", ProductCategory.ELECTRONICS, ProductStatus.ACTIVE, 10, 10
        );

        productGetListResponse2 = new ProductServiceResponse.GetList(
                2L, "아이폰 15", new Money(BigDecimal.valueOf(1500000)), new Money(BigDecimal.valueOf(1275000)),
                "iphone15.jpg", ProductCategory.ELECTRONICS, ProductStatus.ACTIVE, 10, 15
        );

        productGetListRespons = Arrays.asList(productGetListResponse1, productGetListResponse2);

        createRequest = new ProductServiceRequest.Create(
                "맥북 프로", new Money(BigDecimal.valueOf(2000000)), new Money(BigDecimal.valueOf(1800000)),
                "Apple 맥북 프로 M3 칩", "macbook-pro.jpg", ProductCategory.ELECTRONICS, 10
        );

        updateRequest = new ProductServiceRequest.Update(
                "맥북 프로 M3", new Money(BigDecimal.valueOf(2100000)), new Money(BigDecimal.valueOf(1900000)),
                "Apple 맥북 프로 M3 칩 업데이트", "macbook-pro-m3.jpg", ProductCategory.ELECTRONICS
        );

        adjustStockRequest = new ProductServiceRequest.AdjustStock(
                5, StockChangeReason.ADJUSTMENT, "신규 입고"
        );
    }

    @Nested
    @DisplayName("상품 목록 조회 테스트")
    class GetProductsTest {

        @Test
        @DisplayName("인증 없이 상품 목록을 조회할 수 있다")
        @WithAnonymousUser
        void shouldReturnProductListWithoutAuthentication() throws Exception {
            // Given
            when(productService.getProducts()).thenReturn(productGetListRespons);

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
    @DisplayName("상품 상세 조회 테스트")
    class GetProductTest {

        @Test
        @DisplayName("인증 없이 상품 상세를 조회할 수 있다")
        @WithAnonymousUser
        void shouldReturnProductDetailWithoutAuthentication() throws Exception {
            // Given
            Long productId = 1L;
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
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("ADMIN 권한으로 상품을 생성할 수 있다")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateProductWithAdminRole() throws Exception {
            // Given
            Long createdProductId = 1L;
            when(productService.createProduct(any(ProductServiceRequest.Create.class))).thenReturn(createdProductId);

            // When & Then
            mockMvc.perform(post("/api/products")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(createdProductId.toString()));

            verify(productService).createProduct(any(ProductServiceRequest.Create.class));
        }

        @Test
        @DisplayName("USER 권한으로 상품 생성 시 접근이 거부되어야 한다")
        @WithMockUser(roles = "USER")
        void shouldDenyCreateProductWithUserRole() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/products")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 상품 생성 시 401 Unauthorized 또는 403 Forbidden 에러가 발생해야 한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForCreateProduct() throws Exception {
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
        @DisplayName("잘못된 요청으로 상품 생성 시 검증 오류가 발생해야 한다")
        @WithMockUser(roles = "ADMIN")
        void shouldValidateCreateProductRequest() throws Exception {
            // Given
            ProductServiceRequest.Create invalidRequest = new ProductServiceRequest.Create(
                    "", // 비어있는 상품명
                    new Money(BigDecimal.valueOf(2000000)),
                    new Money(BigDecimal.valueOf(1800000)),
                    "Apple 맥북 프로 M3 칩",
                    "macbook-pro.jpg",
                    ProductCategory.ELECTRONICS,
                    10
            );

            // When & Then
            mockMvc.perform(post("/api/products")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class UpdateProductTest {

        @Test
        @DisplayName("ADMIN 권한으로 상품을 수정할 수 있다")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateProductWithAdminRole() throws Exception {
            // Given
            Long productId = 1L;
            when(productService.updateProduct(eq(productId), any(ProductServiceRequest.Update.class))).thenReturn(productId);

            // When & Then
            mockMvc.perform(put("/api/products/{id}", productId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(productId.toString()));

            verify(productService).updateProduct(eq(productId), any(ProductServiceRequest.Update.class));
        }

        @Test
        @DisplayName("USER 권한으로 상품 수정 시 접근이 거부되어야 한다")
        @WithMockUser(roles = "USER")
        void shouldDenyUpdateProductWithUserRole() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/products/{id}", 1L)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 상품 수정 시 401 Unauthorized 또는 403 Forbidden 에러가 발생해야 한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForUpdateProduct() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/products/{id}", 1L)
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
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

        @Test
        @DisplayName("ADMIN 권한으로 상품을 삭제할 수 있다")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteProductWithAdminRole() throws Exception {
            // Given
            Long productId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/products/{id}", productId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService).deleteProduct(productId);
        }

        @Test
        @DisplayName("USER 권한으로 상품 삭제 시 접근이 거부되어야 한다")
        @WithMockUser(roles = "USER")
        void shouldDenyDeleteProductWithUserRole() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/products/{id}", 1L)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 상품 삭제 시 401 Unauthorized 또는 403 Forbidden 에러가 발생해야 한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForDeleteProduct() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/products/{id}", 1L)
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
    @DisplayName("재고 조정 테스트")
    class AdjustStockTest {

        @Test
        @DisplayName("ADMIN 권한으로 재고를 조정할 수 있다")
        @WithMockUser(roles = "ADMIN")
        void shouldAdjustStockWithAdminRole() throws Exception {
            // Given
            Long productId = 1L;
            when(productService.adjustStock(eq(productId), any(ProductServiceRequest.AdjustStock.class))).thenReturn(productId);

            // When & Then
            mockMvc.perform(patch("/api/products/{id}/stock", productId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustStockRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(productId.toString()));

            verify(productService).adjustStock(eq(productId), any(ProductServiceRequest.AdjustStock.class));
        }

        @Test
        @DisplayName("USER 권한으로 재고 조정 시 접근이 거부되어야 한다")
        @WithMockUser(roles = "USER")
        void shouldDenyAdjustStockWithUserRole() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/products/{id}/stock", 1L)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustStockRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 재고 조정 시 401 Unauthorized 또는 403 Forbidden 에러가 발생해야 한다")
        @WithAnonymousUser
        void shouldRequireAuthenticationForAdjustStock() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/products/{id}/stock", 1L)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustStockRequest)))
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
    @DisplayName("실제 사용자 인증을 사용한 테스트")
    class WithActualUserDetailsTest {

        @Test
        @DisplayName("UserDetailsImpl을 사용하여 상품을 생성할 수 있다")
        void shouldCreateProductWithUserDetailsImpl() throws Exception {
            // Given
            Long createdProductId = 1L;
            UserImpl userImpl = new UserImpl(1L, "admin@example.com", RoleType.ADMIN);
            UserDetailsImpl userDetails = new UserDetailsImpl(userImpl);

            when(productService.createProduct(any(ProductServiceRequest.Create.class))).thenReturn(createdProductId);

            // When & Then
            mockMvc.perform(post("/api/products")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(createdProductId.toString()));

            verify(productService).createProduct(any(ProductServiceRequest.Create.class));
        }
    }
}
