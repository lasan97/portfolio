package com.portfolio.backend.service.product;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStock;
import com.portfolio.backend.domain.product.event.ProductStockChangedEvent;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.product.dto.ProductStockServiceRequest;
import com.portfolio.backend.service.product.dto.StockChangeReason;
import com.portfolio.backend.service.product.fixture.ProductStockServiceRequestTestFixtures;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@DisplayName("ProductStockService 테스트")
class ProductStockServiceTest extends ServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockService productStockService;

    @MockBean
    protected EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("재고 조정")
    class AdjustStockTest {

        @ParameterizedTest
        @CsvSource({
                "15, '재고 추가'",
                "5, '재고 감소'"
        })
        @DisplayName("재고를 지정한 수량으로 조정해야 한다")
        void shouldAdjustStockBySpecifiedAmount(int adjustment, String description) {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductStockServiceRequest.AdjustStock request = ProductStockServiceRequestTestFixtures.createStockAdjustRequest(
                    adjustment, StockChangeReason.ADJUSTMENT, description
            );

            // When
            productStockService.adjustStock(product.getId(), request);

            // Then
            Product response = productRepository.findById(product.getId()).get();

            assertThat(response.getStockQuantity()).isEqualTo(adjustment);
        }

        @Test
        @DisplayName("상품 재고 조정시 재고변경이벤트가 발생한다.")
        void shouldPublishProductStockChangedEventWhenProductDeleted() {
            // Given
            int adjustment = 5;
            String description = "재고 조정";

            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductStockServiceRequest.AdjustStock request = ProductStockServiceRequestTestFixtures.createStockAdjustRequest(
                    adjustment, StockChangeReason.ADJUSTMENT, description
            );

            // When
            productStockService.adjustStock(product.getId(), request);

            // Then
            ArgumentCaptor<ProductStock> productStockCaptor = ArgumentCaptor.forClass(ProductStock.class);
            verify(eventPublisher).publishDomainEventsFrom(productStockCaptor.capture());
            ProductStock capturedProductStock = productStockCaptor.getValue();

            boolean hasExpectedEvent = capturedProductStock.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof ProductStockChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 ProductStockChangedEvent가 포함되어 있어야 합니다");
        }

        @Test
        @DisplayName("재고 수량은 음수로 조정할 수 없다")
        void shouldThrowExceptionWhenAdjustingStockToNegative() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductStockServiceRequest.AdjustStock request = ProductStockServiceRequestTestFixtures
                    .createStockAdjustRequest(-5);

            // When & Then
            assertThatThrownBy(() -> productStockService.adjustStock(product.getId(), request))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("재고는 0보다 작을 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 상품의 재고 조정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductForStockAdjustmentNotFound() {
            // Given
            Long productId = 0L;

            ProductStockServiceRequest.AdjustStock request = ProductStockServiceRequestTestFixtures
                    .createStockAdjustRequest(-5);

            // When & Then
            assertThatThrownBy(() -> productStockService.adjustStock(productId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}