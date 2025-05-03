package com.portfolio.backend.controller.product;

import com.portfolio.backend.controller.ControllerTest;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.fixture.ProductServiceRequestTestFixtures;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("ProductController 테스트")
class ProductControllerTest extends ControllerTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productRepository.save(ProductTestFixtures.createDefaultProduct(10));
        productRepository.save(ProductTestFixtures.createDefaultProduct(10));
    }

    @Nested
    @DisplayName("상품 목록 조회 API")
    class GetProducts {

        @SneakyThrows
        ResultActions getProducts() {
            return mockMvc.perform(get("/api/products"))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증 없이 상품 목록을 성공적으로 조회할 수 있다")
        @WithAnonymousUser
        void shouldReturnProductListWithoutAuthentication() throws Exception {
            // When & Then
            getProducts()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[1].name").exists());
        }
    }

    @Nested
    @DisplayName("상품 상세 조회 API")
    class GetProduct {

        @SneakyThrows
        ResultActions getProduct(Long productId) {
            return mockMvc.perform(get("/api/products/{productId}", productId))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증 없이 상품 상세 정보를 성공적으로 조회할 수 있다")
        @WithAnonymousUser
        void shouldReturnProductDetailWithoutAuthentication() throws Exception {
            // Given
            List<Product> products = productRepository.findAll();
            Product product = products.get(0);

            // When & Then
            getProduct(product.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(product.getId()))
                    .andExpect(jsonPath("$.name").value(product.getName()));
        }
    }

    @Nested
    @DisplayName("상품 생성 API")
    class CreateProduct {

        @SneakyThrows
        ResultActions createProduct(ProductServiceRequest.Create request) {
            return mockMvc.perform(post("/api/products")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자는 상품을 생성할 수 있다")
        @WithUserDetails("admin")
        void shouldCreateProductWhenAdmin() throws Exception {
            // Given
            ProductServiceRequest.Create request = ProductServiceRequestTestFixtures.createProductCreateRequest();

            // When
            ResultActions resultActions = createProduct(request);

            // Then
            resultActions
                    .andExpect(status().isOk());

            Long productId = resultActionsTo(resultActions, Long.class);
            Optional<Product> response = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED);

            assertTrue(response.isPresent(), "Product should be created in the database");
        }

        @Test
        @DisplayName("일반 사용자는 상품을 생성할 수 없다")
        @WithUserDetails
        void shouldNotCreateProductWhenRegularUser() throws Exception {
            // Given
            ProductServiceRequest.Create request = ProductServiceRequestTestFixtures.createProductCreateRequest();

            // When & Then
            createProduct(request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("상품 수정 API")
    class UpdateProduct {

        @SneakyThrows
        ResultActions updateProduct(Long productId, ProductServiceRequest.Update request) {
            return mockMvc.perform(put("/api/products/{productId}", productId)
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자는 상품을 수정할 수 있다")
        @WithUserDetails("admin")
        void shouldUpdateProductWhenAdmin() throws Exception {
            // Given
            List<Product> products = productRepository.findAll();
            Product product = products.get(0);

            ProductServiceRequest.Update request = ProductServiceRequestTestFixtures.createProductUpdateRequest();

            // When
            ResultActions resultActions = updateProduct(product.getId(), request);

            // Then
            resultActions.andExpect(status().isOk());
            Long productId = resultActionsTo(resultActions, Long.class);

            assertEquals(productId, product.getId(), "Product id should be updated");
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertEquals(request.name(), updatedProduct.getName(), "Product name should be updated");
        }

        @Test
        @DisplayName("일반 사용자는 상품을 수정할 수 없다")
        @WithUserDetails
        void shouldNotUpdateProductWhenRegularUser() throws Exception {
            // Given
            List<Product> products = productRepository.findAll();
            Product product = products.get(0);

            ProductServiceRequest.Update request = ProductServiceRequestTestFixtures.createProductUpdateRequest();

            // When & Then
            updateProduct(product.getId(), request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("상품 삭제 API")
    class DeleteProduct {

        @SneakyThrows
        ResultActions deleteProduct(Long productId) {
            return mockMvc.perform(delete("/api/products/{productId}", productId));
        }

        @Test
        @DisplayName("관리자는 상품을 삭제할 수 있다")
        @WithUserDetails("admin")
        void shouldDeleteProductWhenAdmin() throws Exception {
            // Given
            List<Product> products = productRepository.findAll();
            Product product = products.get(0);

            // When & Then
            deleteProduct(product.getId())
                    .andExpect(status().isOk());

            Optional<Product> response = productRepository.findByIdAndStatusNot(product.getId(), ProductStatus.DELETED);
            assertFalse(response.isPresent(), "Product should be deleted from the database");
        }

        @Test
        @DisplayName("일반 사용자는 상품을 삭제할 수 없다")
        @WithUserDetails
        void shouldNotDeleteProductWhenRegularUser() throws Exception {
            // Given
            List<Product> products = productRepository.findAll();
            Product product = products.get(0);

            // When & Then
            deleteProduct(product.getId())
                    .andExpect(status().isForbidden());
        }
    }
}