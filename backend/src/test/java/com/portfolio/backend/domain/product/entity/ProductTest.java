package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.event.ProductStockChangedEvent;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.user.event.UserCreatedEvent;
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
            Product product = Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build();

            // Then
            assertThat(product)
                .satisfies(p -> {
                    assertThat(p.getName()).isEqualTo(name);
                    assertThat(p.getOriginalPrice()).isEqualTo(originalPrice);
                    assertThat(p.getPrice()).isEqualTo(price);
                    assertThat(p.getDescription()).isEqualTo(description);
                    assertThat(p.getThumbnailImageUrl()).isEqualTo(thumbnailImageUrl);
                    assertThat(p.getCategory()).isEqualTo(category);
                    assertThat(p.getStockQuantity()).isEqualTo(stock);
                    assertThat(p.getStatus()).isEqualTo(ProductStatus.ACTIVE);
                });

            assertThat(product.getDomainEvents()).hasSize(1);
            assertThat(product.getDomainEvents().get(0)).isInstanceOf(ProductStockChangedEvent.class);
        }

        @Test
        @DisplayName("재고가 0인 상품 생성 시 SOLD_OUT 상태여야 한다")
        void shouldBeInSoldOutStatusWhenStockIsZero() {
            // When
            Product product = Product.builder()
                    .name("맥북 프로 M2")
                    .originalPrice(new Money(new BigDecimal("2000000")))
                    .price(new Money(new BigDecimal("1800000")))
                    .description("2023년형 맥북 프로 M2 모델")
                    .thumbnailImageUrl("https://example.com/macbook.jpg")
                    .category(ProductCategory.ELECTRONICS)
                    .stock(0)
                    .build();

            // Then
            assertThat(product.getStockQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("상품명이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenNameIsNullOrEmpty(String name) {
            // When & Then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(new Money(new BigDecimal("1800000")))
                    .price(new Money(new BigDecimal("1800000")))
                    .description("2023년형 맥북 프로 M2 모델")
                    .thumbnailImageUrl("https://example.com/macbook.jpg")
                    .category(ProductCategory.ELECTRONICS)
                    .stock(100)
                    .build())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("상품명은 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("originalPrice가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenOriginalPriceIsNull() {
            // When & Then
            assertThatThrownBy(() -> Product.builder()
                            .name("맥북 프로 M2")
                            .originalPrice(null)
                            .price(new Money(new BigDecimal("1800000")))
                            .description("2023년형 맥북 프로 M2 모델")
                            .thumbnailImageUrl("https://example.com/macbook.jpg")
                            .category(ProductCategory.ELECTRONICS)
                            .stock(100)
                            .build())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("원가는 null일 수 없습니다");
        }

        @Test
        @DisplayName("판매가가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenPriceIsNull() {
            // When & Then
            assertThatThrownBy(() -> Product.builder()
                    .name("맥북 프로 M2")
                    .originalPrice(new Money(new BigDecimal("2000000")))
                    .price(null)
                    .description("2023년형 맥북 프로 M2 모델")
                    .thumbnailImageUrl("https://example.com/macbook.jpg")
                    .category(ProductCategory.ELECTRONICS)
                    .stock(100)
                    .build())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("판매가는 null일 수 없습니다");
        }

        @Test
        @DisplayName("카테고리가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenCategoryIsNull() {
            // When & Then
            assertThatThrownBy(() -> Product.builder()
                    .name("맥북 프로 M2")
                    .originalPrice(new Money(new BigDecimal("2000000")))
                    .price(new Money(new BigDecimal("1800000")))
                    .description("2023년형 맥북 프로 M2 모델")
                    .thumbnailImageUrl("https://example.com/macbook.jpg")
                    .category(null)
                    .stock(100)
                    .build())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("카테고리는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("유효한 파라미터로 상품 수정 시 성공해야 한다")
        void shouldUpdateProductWithValidParameters() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct();
            String newName = "LG 그램";
            Money newOriginalPrice = new Money(new BigDecimal("2100000"));
            Money newPrice = new Money(new BigDecimal("1900000"));
            String newDescription = "2024년형 LG 그램 신모델";
            String newThumbnailImageUrl = "https://example.com/gram.jpg";
            ProductCategory newCategory = ProductCategory.ELECTRONICS;

            // When
            product.update(newName, newOriginalPrice, newPrice, newDescription, newThumbnailImageUrl, newCategory);

            // Then
            assertThat(product)
                    .satisfies(p -> {
                        assertThat(p.getName()).isEqualTo(newName);
                        assertThat(p.getOriginalPrice()).isEqualTo(newOriginalPrice);
                        assertThat(p.getPrice()).isEqualTo(newPrice);
                        assertThat(p.getDescription()).isEqualTo(newDescription);
                        assertThat(p.getThumbnailImageUrl()).isEqualTo(newThumbnailImageUrl);
                        assertThat(p.getCategory()).isEqualTo(newCategory);
                    });
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("상품명이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenNameIsNullOrEmpty(String name) {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct();

            // When & Then
            assertThatThrownBy(() -> product.update(
                    name,
                    new Money(new BigDecimal("2100000")),
                    new Money(new BigDecimal("1900000")),
                    "설명",
                    "이미지URL",
                    ProductCategory.ELECTRONICS
            ))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("상품명은 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("originalPrice가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenOriginalPriceIsNull() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct();

            // When & Then
            assertThatThrownBy(() -> product.update(
                    "상품명",
                    null,
                    new Money(new BigDecimal("1900000")),
                    "설명",
                    "이미지URL",
                    ProductCategory.ELECTRONICS
            ))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("원가는 null일 수 없습니다");
        }

        @Test
        @DisplayName("price가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenPriceIsNull() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct();

            // When & Then
            assertThatThrownBy(() -> product.update(
                    "상품명",
                    new Money(new BigDecimal("2100000")),
                    null,
                    "설명",
                    "이미지URL",
                    ProductCategory.ELECTRONICS
            ))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("판매가는 null일 수 없습니다");
        }

        @Test
        @DisplayName("category가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenCategoryIsNull() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct();

            // When & Then
            assertThatThrownBy(() -> product.update(
                    "상품명",
                    new Money(new BigDecimal("2100000")),
                    new Money(new BigDecimal("1900000")),
                    "설명",
                    "이미지URL",
                    null
            ))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("카테고리는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상품 상태")
    class IsAvailable {

        @ParameterizedTest
        @CsvSource({
            "ACTIVE, true",
            "SOLD_OUT, false",
            "DELETED, false"
        })
        @DisplayName("상품 상태에 따라 isAvailable이 올바른 값을 반환해야 한다")
        void shouldReturnCorrectAvailabilityBasedOnStatus(ProductStatus status, boolean expected) {
            // Given
            Product product = ProductTestFixtures.createProductWithStatus(status);

            // When & Then
            assertThat(product.isAvailable()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("상품 활성화")
    class Active {

        @Test
        @DisplayName("상품을 활성화하면 상태가 ACTIVE로 변경 되어야한다.")
        void shouldChangeStatusToActiveWhenActivated() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(0);

            // When
            product.active();

            // Then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("DELETED 상태의 경우 에러가 발생한다.")
        void shouldThrowExceptionWhenChangingStatusOfDeletedProduct() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(1);
            product.delete();

            // When & Then
            assertThatThrownBy(() -> product.active())
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("삭제된 상품의 상태를 변경할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("상품 활성화")
    class SoldOut {

        @Test
        @DisplayName("상품을 매진처리하면 상태가 SOLDOUT으로 변경 되어야한다.")
        void shouldChangeStatusToSoldOutWhenSoldOut() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(1);

            // When
            product.soldOut();

            // Then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("DELETED 상태의 경우 에러가 발생한다.")
        void shouldThrowExceptionWhenChangingStatusOfDeletedProduct() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(1);
            product.delete();

            // When & Then
            assertThatThrownBy(() -> product.soldOut())
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("삭제된 상품의 상태를 변경할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("삭제")
    class Delete {

        @Test
        @DisplayName("상품을 삭제하면 상태가 DELETED로 변경 되어야한다.")
        void shouldChangeStatusToDeletedWhenDeleted() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);

            // When
            product.delete();

            // Then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("할인율")
    class GetDiscountRate {

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
            Product product = ProductTestFixtures.createDefaultProduct(
                    new Money(new BigDecimal(originalPrice)),
                    new Money(new BigDecimal(price)));
            
            // When
            int discountRate = product.getDiscountRate();
            
            // Then
            assertThat(discountRate).isEqualTo(expectedDiscountRate);
        }
        
        @Test
        @DisplayName("원가가 0일 때 할인율은 0이어야 한다")
        void shouldReturnZeroDiscountRateWhenOriginalPriceIsZero() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(
                    new Money(BigDecimal.ZERO),
                    new Money(BigDecimal.ZERO));
            
            // When
            int discountRate = product.getDiscountRate();
            
            // Then
            assertThat(discountRate).isZero();
        }
    }
}