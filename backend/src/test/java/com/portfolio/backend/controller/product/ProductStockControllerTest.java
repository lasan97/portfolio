package com.portfolio.backend.controller.product;

import com.portfolio.backend.controller.ControllerTest;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductStockServiceRequest;
import com.portfolio.backend.service.product.fixture.ProductStockServiceRequestTestFixtures;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DisplayName("ProductStockController 테스트")
class ProductStockControllerTest extends ControllerTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("재고 조정 API")
    class AdjustStock {

        @SneakyThrows
        ResultActions adjustStock(Long productId, ProductStockServiceRequest.AdjustStock request) {
            return mockMvc.perform(patch("/api/products/{productId}/stocks", productId)
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON));
        }

        @ParameterizedTest
        @CsvSource({
                "15, ADJUSTMENT, '입고'",
                "5, LOSS, '재고 손실'"
        })
        @DisplayName("관리자는 재고를 조정할 수 있다")
        @WithUserDetails("admin")
        void shouldAdjustStockWithUserRole(int quantity, String reason, String description) throws Exception {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductStockServiceRequest.AdjustStock request = ProductStockServiceRequestTestFixtures.createStockAdjustRequest(
                    quantity,
                    com.portfolio.backend.service.product.dto.StockChangeReason.valueOf(reason),
                    description
            );

            // When
            ResultActions resultActions = adjustStock(product.getId(), request);

            // Then
            resultActions
                    .andExpect(status().isOk());

            product = productRepository.findByIdAndStatusNot(product.getId(), ProductStatus.DELETED).get();

            assertEquals(quantity, product.getStockQuantity(), "Product stock should be updated");
        }

        @Test
        @DisplayName("일반 사용자는 재고를 조정할 수 없다")
        void shouldAdjustStockWithAdminRole() throws Exception {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(1));
            ProductStockServiceRequest.AdjustStock request = ProductStockServiceRequestTestFixtures.createStockAdjustRequest();

            // When & Then
            adjustStock(product.getId(), request)
                    .andExpect(status().isForbidden());
        }
    }
}