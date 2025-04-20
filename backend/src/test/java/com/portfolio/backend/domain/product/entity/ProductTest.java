package com.portfolio.backend.domain.product.entity;

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

class ProductTest {

    @Nested
    @DisplayName("상품 생성")
    class CreateProduct {

        @Test
        @DisplayName("유효한 파라미터로 상품 생성 시 성공해야 한다")
        void createProductWithValidParameters() {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // when
            Product product = Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build();

            // then
            assertThat(product.getName()).isEqualTo(name);
            assertThat(product.getOriginalPrice()).isEqualTo(originalPrice);
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getDescription()).isEqualTo(description);
            assertThat(product.getThumbnailImageUrl()).isEqualTo(thumbnailImageUrl);
            assertThat(product.getCategory()).isEqualTo(category);
            assertThat(product.getStock().getQuantity()).isEqualTo(stock);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("재고가 0인 상품 생성 시 SOLD_OUT 상태여야 한다")
        void createProductWithZeroStock() {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 0;

            // when
            Product product = Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build();

            // then
            assertThat(product.getStock().getQuantity()).isEqualTo(0);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("상품명이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenNameIsNullOrEmpty(String name) {
            // given
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build()
            ).isInstanceOf(DomainException.class)
                    .hasMessageContaining("상품명은 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("원가가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenOriginalPriceIsNull() {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = null;
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build()
            ).isInstanceOf(DomainException.class)
                    .hasMessageContaining("원가는 null일 수 없습니다");
        }

        @Test
        @DisplayName("판매가가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenPriceIsNull() {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = null;
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build()
            ).isInstanceOf(DomainException.class)
                    .hasMessageContaining("판매가는 null일 수 없습니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("상품 설명이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenDescriptionIsNullOrEmpty(String description) {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build()
            ).isInstanceOf(DomainException.class)
                    .hasMessageContaining("상품설명은 비어있을 수 없습니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("썸네일 이미지 URL이 null이거나 빈 문자열이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenThumbnailImageUrlIsNullOrEmpty(String thumbnailImageUrl) {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            ProductCategory category = ProductCategory.ELECTRONICS;
            Integer stock = 100;

            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build()
            ).isInstanceOf(DomainException.class)
                    .hasMessageContaining("썸네일이미지주소는 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("카테고리가 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenCategoryIsNull() {
            // given
            String name = "맥북 프로 M2";
            Money originalPrice = new Money(new BigDecimal("2000000"));
            Money price = new Money(new BigDecimal("1800000"));
            String description = "2023년형 맥북 프로 M2 모델";
            String thumbnailImageUrl = "https://example.com/macbook.jpg";
            ProductCategory category = null;
            Integer stock = 100;

            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(name)
                    .originalPrice(originalPrice)
                    .price(price)
                    .description(description)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .category(category)
                    .stock(stock)
                    .build()
            ).isInstanceOf(DomainException.class)
                    .hasMessageContaining("카테고리는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("재고 관리")
    class StockManagement {

        @Test
        @DisplayName("재고를 증가시키면 수량이 증가해야 한다")
        void increaseStock() {
            // given
            Product product = createValidProduct();
            int initialStock = product.getStock().getQuantity();
            int increaseAmount = 50;

            // when
            product.increaseStock(increaseAmount);

            // then
            assertThat(product.getStock().getQuantity()).isEqualTo(initialStock + increaseAmount);
        }

        @Test
        @DisplayName("재고를 감소시키면 수량이 감소해야 한다")
        void decreaseStock() {
            // given
            Product product = createValidProduct();
            int initialStock = product.getStock().getQuantity();
            int decreaseAmount = 50;

            // when
            product.decreaseStock(decreaseAmount);

            // then
            assertThat(product.getStock().getQuantity()).isEqualTo(initialStock - decreaseAmount);
        }

        @Test
        @DisplayName("품절 상태에서 재고 증가 시 ACTIVE 상태로 변경되어야 한다")
        void changeStatusToActiveWhenIncreaseStockFromSoldOut() {
            // given
            Product product = createProductWithZeroStock();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            // when
            product.increaseStock(10);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.getStock().getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("재고가 0이 되면 SOLD_OUT 상태로 변경되어야 한다")
        void changeStatusToSoldOutWhenStockIsZero() {
            // given
            Product product = createValidProduct();
            int currentStock = product.getStock().getQuantity();
            
            // when
            product.decreaseStock(currentStock); // 재고를 0으로 만듦

            // then
            assertThat(product.getStock().getQuantity()).isEqualTo(0);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }
    }

    @Nested
    @DisplayName("상품 상태")
    class ProductStatusTest {
        
        @Test
        @DisplayName("상품 상태가 ACTIVE 일 때만 isAvailable이 true를 반환해야 한다")
        void isAvailableReturnsTrueOnlyWhenStatusIsActive() {
            // given
            Product product = createValidProduct();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            
            // when & then
            assertThat(product.isAvailable()).isTrue();
            
            // when
            product.decreaseStock(product.getStock().getQuantity());
            
            // then
            assertThat(product.isAvailable()).isFalse();
            
            // when
            product.increaseStock(10);
            product.delete();
            
            // then
            assertThat(product.isAvailable()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("할인율 계산")
    class DiscountRate {
        
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
        @DisplayName("할인율이 올바르게 계산되어야 한다")
        void calculateDiscountRate(String originalPrice, String price, int expectedDiscountRate) {
            // given
            Product product = Product.builder()
                    .name("상품명")
                    .originalPrice(new Money(new BigDecimal(originalPrice)))
                    .price(new Money(new BigDecimal(price)))
                    .description("상품 설명")
                    .thumbnailImageUrl("https://example.com/image.jpg")
                    .category(ProductCategory.ELECTRONICS)
                    .stock(100)
                    .build();
            
            // when
            int discountRate = product.getDiscountRate();
            
            // then
            assertThat(discountRate).isEqualTo(expectedDiscountRate);
        }
        
        @Test
        @DisplayName("원가가 0일 때 할인율은 0이어야 한다")
        void discountRateIsZeroWhenOriginalPriceIsZero() {
            // given
            Product product = Product.builder()
                    .name("상품명")
                    .originalPrice(new Money(BigDecimal.ZERO))
                    .price(new Money(BigDecimal.ZERO))
                    .description("상품 설명")
                    .thumbnailImageUrl("https://example.com/image.jpg")
                    .category(ProductCategory.ELECTRONICS)
                    .stock(100)
                    .build();
            
            // when
            int discountRate = product.getDiscountRate();
            
            // then
            assertThat(discountRate).isEqualTo(0);
        }
    }

    // 테스트 헬퍼 메서드
    private Product createValidProduct() {
        return Product.builder()
                .name("맥북 프로 M2")
                .originalPrice(new Money(new BigDecimal("2000000")))
                .price(new Money(new BigDecimal("1800000")))
                .description("2023년형 맥북 프로 M2 모델")
                .thumbnailImageUrl("https://example.com/macbook.jpg")
                .category(ProductCategory.ELECTRONICS)
                .stock(100)
                .build();
    }

    private Product createProductWithZeroStock() {
        return Product.builder()
                .name("맥북 프로 M2")
                .originalPrice(new Money(new BigDecimal("2000000")))
                .price(new Money(new BigDecimal("1800000")))
                .description("2023년형 맥북 프로 M2 모델")
                .thumbnailImageUrl("https://example.com/macbook.jpg")
                .category(ProductCategory.ELECTRONICS)
                .stock(0)
                .build();
    }
}
