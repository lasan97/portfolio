package com.portfolio.backend.domain.product.service;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.entity.ProductStock;
import com.portfolio.backend.domain.product.event.ProductStockChangedEvent;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockRepository;
import com.portfolio.backend.service.ServiceTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@DisplayName("ProductStockManager 테스트")
class ProductStockManagerTest extends ServiceTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductStockRepository productStockRepository;

    @Autowired
    private ProductStockManager productStockManager;

    @MockBean
    private DomainEventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("재고 반품 처리")
    class Refund {

        @Test
        @DisplayName("상품 반품 시 재고가 증가해야 한다")
        void shouldIncreaseStockWhenProductIsRefunded() {
            // Given
            int initQuantity = 10;
            int refundQuantity = 5;
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(initQuantity));

            // When
            productStockManager.refund(product, refundQuantity);

            // Then
            ProductStock response = productStockRepository.findByProductId(product.getId()).get();

            assertThat(response.getQuantity())
                    .isEqualTo(initQuantity + refundQuantity);
        }

        @Test
        @DisplayName("품절 상품 반품 시 상품 상태가 활성화로 변경되어야 한다")
        void shouldActivateProductWhenSoldOutProductIsRefunded() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(0));
            int refundQuantity = 5;

            // When
            productStockManager.refund(product, refundQuantity);

            // Then
            Product response = productRepository.findById(product.getId()).get();

            assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("재고가 존재하지 않을 경우 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductStockNotFound() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));
            productStockRepository.deleteAll();

            // When & Then
            assertThatThrownBy(() -> productStockManager.refund(product, 5))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("재고가 존재하지 않습니다");
        }

        @Test
        @DisplayName("상품 반품시 ProductStockChangedEvent가 발생한다.")
        void shouldPublishProductStockChangedEventWhenRefunded() {

            // Given
            int initQuantity = 10;
            int refundQuantity = 5;
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(initQuantity));

            // When
            productStockManager.refund(product, refundQuantity);

            // Then
            ArgumentCaptor<ProductStock> productStockCaptor = ArgumentCaptor.forClass(ProductStock.class);
            verify(eventPublisher).publishEventsFrom(productStockCaptor.capture());
            ProductStock capturedProductStock = productStockCaptor.getValue();

            boolean hasExpectedEvent = capturedProductStock.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof ProductStockChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 ProductStockChangedEvent가 포함되어 있어야 합니다");
        }
    }

    @Nested
    @DisplayName("재고 판매 처리")
    class Sale {

        @Test
        @DisplayName("상품 판매 시 재고가 감소해야 한다")
        void shouldDecreaseStockWhenProductIsSold() {
            // Given
            int initialQuantity = 10;
            int saleQuantity = 5;
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(initialQuantity));

            // When
            productStockManager.sale(product, saleQuantity);

            // Then
            ProductStock response = productStockRepository.findByProductId(product.getId()).get();
            assertThat(response.getQuantity()).isEqualTo(initialQuantity - saleQuantity);
        }

        @Test
        @DisplayName("활성 상품 판매 후 재고가 0이 되면 상품 상태가 품절로 변경되어야 한다")
        void shouldMarkProductAsSoldOutWhenStockBecomesZero() {
            // Given
            int initialQuantity = 10;
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(initialQuantity));

            // When
            productStockManager.sale(product, initialQuantity);

            // Then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("삭제된 상품 판매 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenSellingDeletedProduct() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            productRepository.save(product);

            // When & Then
            assertThatThrownBy(() -> productStockManager.sale(product, 5))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("삭제된 상품은 재고를 변경할 수 없습니다");
        }

        @Test
        @DisplayName("재고보다 많은 수량 판매시 에러가 발생한다.")
        void shouldThrowExceptionWhenInsufficientStock() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(1));

            // When & Then
            assertThatThrownBy(() -> productStockManager.sale(product, 100))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("재고가 부족합니다.");
        }

        @Test
        @DisplayName("상품 판매시 ProductStockChangedEvent가 발생한다.")
        void shouldPublishProductStockChangedEventWhenSale() {
            // Given
            int initQuantity = 10;
            int saleQuantity = 5;
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(initQuantity));

            // When
            productStockManager.sale(product, saleQuantity);

            // Then
            ArgumentCaptor<ProductStock> productStockCaptor = ArgumentCaptor.forClass(ProductStock.class);
            verify(eventPublisher).publishEventsFrom(productStockCaptor.capture());
            ProductStock capturedProductStock = productStockCaptor.getValue();

            boolean hasExpectedEvent = capturedProductStock.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof ProductStockChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 ProductStockChangedEvent가 포함되어 있어야 합니다");
        }
    }

    @Nested
    @DisplayName("재고 조정")
    class Adjust {

        @Test
        @DisplayName("재고 조정 시 수량이 변경되어야 한다")
        void shouldAdjustStockQuantity() {
            // Given
            int initialQuantity = 10;
            int adjustQuantity = 15;
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(initialQuantity));
            String memo = "재고 조정 테스트";

            // When
            productStockManager.adjust(product, adjustQuantity, memo);

            // Then
            ProductStock response = productStockRepository.findByProductId(product.getId()).get();
            assertThat(response.getQuantity()).isEqualTo(adjustQuantity);
        }

        @Test
        @DisplayName("재고 조정 후 재고가 0보다 크면 상품 상태가 활성화로 변경되어야 한다")
        void shouldActivateProductWhenStockBecomesAvailable() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(0));
            int adjustQuantity = 5;
            String memo = "재고 조정 테스트";

            // When
            productStockManager.adjust(product, adjustQuantity, memo);

            // Then
            Product response = productRepository.findById(product.getId()).get();
            assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("재고 조정 후 재고가 0이면 상품 상태가 품절로 변경되어야 한다")
        void shouldMarkProductAsSoldOutWhenStockBecomesZero() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));
            int adjustQuantity = 0;
            String memo = "재고 조정 테스트";

            // When
            productStockManager.adjust(product, adjustQuantity, memo);

            // Then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("삭제된 상품 재고 조정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenAdjustingDeletedProduct() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            productRepository.save(product);

            // When & Then
            assertThatThrownBy(() -> productStockManager.adjust(product, 5, "재고 조정 테스트"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("삭제된 상품은 재고를 변경할 수 없습니다");
        }

        @Test
        @DisplayName("재고 조정시 ProductStockChangedEvent가 발생한다.")
        void shouldPublishProductStockChangedEventWhenAdjusting() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));
            int adjustQuantity = 5;

            // When
            productStockManager.adjust(product, adjustQuantity, "재고 조정");

            // Then
            ArgumentCaptor<ProductStock> productStockCaptor = ArgumentCaptor.forClass(ProductStock.class);
            verify(eventPublisher).publishEventsFrom(productStockCaptor.capture());
            ProductStock capturedProductStock = productStockCaptor.getValue();

            boolean hasExpectedEvent = capturedProductStock.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof ProductStockChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 ProductStockChangedEvent가 포함되어 있어야 합니다");
        }
    }

    @Nested
    @DisplayName("삭제된 상품 처리")
    class Deleted {

        @Test
        @DisplayName("삭제된 상품의 재고를 0으로 조정해야 한다")
        void shouldAdjustStockToZeroForDeletedProduct() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            productRepository.save(product);

            // When
            productStockManager.deleted(product);

            // Then
            ProductStock response = productStockRepository.findByProductId(product.getId()).get();
            assertThat(response.getQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("삭제되지 않은 상품 처리 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductIsNotDeleted() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            // When & Then
            assertThatThrownBy(() -> productStockManager.deleted(product))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("삭제된 상품이 아닙니다");
        }

        @Test
        @DisplayName("삭제 상품 재고 조정시 ProductStockChangedEvent가 발생한다.")
        void shouldPublishProductStockChangedEventWhenAdjusting() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            productRepository.save(product);

            // When
            productStockManager.deleted(product);

            // Then
            ArgumentCaptor<ProductStock> productStockCaptor = ArgumentCaptor.forClass(ProductStock.class);
            verify(eventPublisher).publishEventsFrom(productStockCaptor.capture());
            ProductStock capturedProductStock = productStockCaptor.getValue();

            boolean hasExpectedEvent = capturedProductStock.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof ProductStockChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 ProductStockChangedEvent가 포함되어 있어야 합니다");
        }
    }
}
