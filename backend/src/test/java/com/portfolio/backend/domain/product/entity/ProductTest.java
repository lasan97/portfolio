package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Product 엔티티에 대한 단위 테스트
 */
@DisplayName("Product 엔티티 테스트")
class ProductTest {

    @Nested
    @DisplayName("상품 생성")
    class CreateProduct {

        @Test
        @DisplayName("유효한 파라미터로 상품 생성 시 성공해야 한다")
        void shouldCreateProductWithValidParameters() {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // When
            Product product = buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock);

            // Then
            assertThat(product)
                .satisfies(p -> {
                    assertThat(p.getName()).isEqualTo(name);
                    assertThat(p.getOriginalPrice()).isEqualTo(originalPrice);
                    assertThat(p.getPrice()).isEqualTo(price);
                    assertThat(p.getDescription()).isEqualTo(description);
                    assertThat(p.getThumbnailImageUrl()).isEqualTo(thumbnailImageUrl);
                    assertThat(p.getCategory()).isEqualTo(category);
                    assertThat(p.getStock().getQuantity()).isEqualTo(stock);
                    assertThat(p.getStatus()).isEqualTo(ProductStatus.ACTIVE);
                });
        }

        @Test
        @DisplayName("재고가 0인 상품 생성 시 SOLD_OUT 상태여야 한다")
        void shouldBeInSoldOutStatusWhenStockIsZero() {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 0;

            // When
            Product product = buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock);

            // Then
            assertThat(product.getStock().getQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("상품명이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenNameIsNullOrEmpty(String name) {
            // Given - 필요한 모든 파라미터 준비
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // When & Then - 예외가 발생하는지 확인
            assertThatThrownBy(() -> buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("상품명은 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("원가가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenOriginalPriceIsNull() {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = null;
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // When & Then
            assertThatThrownBy(() -> buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("원가는 null일 수 없습니다");
        }

        @Test
        @DisplayName("판매가가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenPriceIsNull() {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = null;
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // When & Then
            assertThatThrownBy(() -> buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("판매가는 null일 수 없습니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("상품 설명이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenDescriptionIsNullOrEmpty(String description) {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // When & Then
            assertThatThrownBy(() -> buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("상품설명은 비어있을 수 없습니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("썸네일 이미지 URL이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenThumbnailImageUrlIsNullOrEmpty(String thumbnailImageUrl) {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // When & Then
            assertThatThrownBy(() -> buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("썸네일이미지주소는 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("카테고리가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenCategoryIsNull() {
            // Given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = null;
            Integer stock = 100;

            // When & Then
            assertThatThrownBy(() -> buildProduct(name, originalPrice, price, description, thumbnailImageUrl, category, stock))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("카테고리는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("재고 관리")
    class StockManagement {

        @Test
        @DisplayName("재고를 증가시키면 수량이 증가해야 한다")
        void shouldIncreaseStockQuantityWhenIncreaseCalled() {
            // Given
            Product product = TestFixtures.createDefaultProduct();
            int initialStock = product.getStock().getQuantity();
            int increaseAmount = 50;

            // When
            product.increaseStock(increaseAmount);

            // Then
            assertThat(product.getStock().getQuantity()).isEqualTo(initialStock + increaseAmount);
        }

        @Test
        @DisplayName("재고를 감소시키면 수량이 감소해야 한다")
        void shouldDecreaseStockQuantityWhenDecreaseCalled() {
            // Given
            Product product = TestFixtures.createDefaultProduct();
            int initialStock = product.getStock().getQuantity();
            int decreaseAmount = 50;

            // When
            product.decreaseStock(decreaseAmount);

            // Then
            assertThat(product.getStock().getQuantity()).isEqualTo(initialStock - decreaseAmount);
        }

        @Test
        @DisplayName("품절 상태에서 재고 증가 시 ACTIVE 상태로 변경되어야 한다")
        void shouldChangeToActiveStatusWhenIncreaseStockFromSoldOut() {
            // Given
            Product product = TestFixtures.createProductWithZeroStock();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            // When
            product.increaseStock(10);

            // Then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.getStock().getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("재고가 0이 되면 SOLD_OUT 상태로 변경되어야 한다")
        void shouldChangeToSoldOutStatusWhenStockBecomesZero() {
            // Given
            Product product = TestFixtures.createDefaultProduct();
            int currentStock = product.getStock().getQuantity();
            
            // When
            product.decreaseStock(currentStock); // 재고를 0으로 만듦

            // Then
            assertThat(product.getStock().getQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }
    }

    @Nested
    @DisplayName("상품 상태")
    class ProductStatusTest {
        
        @ParameterizedTest
        @CsvSource({
            "ACTIVE, true",
            "SOLD_OUT, false",
            "DELETED, false"
        })
        @DisplayName("상품 상태에 따라 isAvailable이 올바른 값을 반환해야 한다")
        void shouldReturnCorrectAvailabilityBasedOnStatus(ProductStatus status, boolean expected) {
            // Given
            Product product = TestFixtures.createProductWithStatus(status);
            
            // When & Then
            assertThat(product.isAvailable()).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("상태가 ACTIVE이고 재고가 있을 때만 isAvailable이 true를 반환해야 한다")
        void shouldReturnTrueForIsAvailableOnlyWhenActiveAndInStock() {
            // Given
            Product product = TestFixtures.createDefaultProduct();
            
            // 먼저 ACTIVE 상태와 재고가 있는 상태 확인
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.getStock().getQuantity()).isPositive();
            
            // When & Then - ACTIVE 상태에서는 true
            assertThat(product.isAvailable()).isTrue();
            
            // 재고를 0으로 만들어 SOLD_OUT 상태로 변경
            product.decreaseStock(product.getStock().getQuantity());
            
            // When & Then - SOLD_OUT 상태에서는 false
            assertThat(product.isAvailable()).isFalse();
            
            // 재고를 늘려 ACTIVE 상태로 변경 후 삭제
            product.increaseStock(10);
            product.delete();
            
            // When & Then - DELETED 상태에서는 false
            assertThat(product.isAvailable()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("할인율 계산")
    class DiscountRateTest {
        
        @ParameterizedTest
        @CsvSource({
            "2000000, 1800000, 10",  // 10% 할인
            "1000000, 900000, 10",   // 10% 할인
            "1000000, 800000, 20",   // 20% 할인
            "1000000, 750000, 25",   // 25% 할인
            "1000000, 1000000, 0",   // 할인 없음
            "1000000, 500000, 50",   // 50% 할인
            "1000000, 0, 100"        // 100% 할인
        })
        @DisplayName("원가와 판매가에 따라 할인율이 올바르게 계산되어야 한다")
        void shouldCalculateCorrectDiscountRate(String originalPrice, String price, int expectedDiscountRate) {
            // Given
            Product product = buildProduct(
                    "상품명",
                    new Money(new BigDecimal(originalPrice)),
                    new Money(new BigDecimal(price)),
                    "상품 설명",
                    "https://example.com/image.jpg",
                    ProductCategory.ELECTRONICS,
                    100
            );
            
            // When
            int discountRate = product.getDiscountRate();
            
            // Then
            assertThat(discountRate).isEqualTo(expectedDiscountRate);
        }
        
        @Test
        @DisplayName("원가가 0일 때 할인율은 0이어야 한다")
        void shouldReturnZeroDiscountRateWhenOriginalPriceIsZero() {
            // Given
            Product product = buildProduct(
                    "상품명",
                    new Money(BigDecimal.ZERO),
                    new Money(BigDecimal.ZERO),
                    "상품 설명",
                    "https://example.com/image.jpg",
                    ProductCategory.ELECTRONICS,
                    100
            );
            
            // When
            int discountRate = product.getDiscountRate();
            
            // Then
            assertThat(discountRate).isZero();
        }
    }

    // 테스트 헬퍼 메서드
    private Product buildProduct(String name, Money originalPrice, Money price, 
                                String description, String thumbnailImageUrl, 
                                ProductCategory category, Integer stock) {
        return Product.builder()
                .name(name)
                .originalPrice(originalPrice)
                .price(price)
                .description(description)
                .thumbnailImageUrl(thumbnailImageUrl)
                .category(category)
                .stock(stock)
                .build();
    }
}