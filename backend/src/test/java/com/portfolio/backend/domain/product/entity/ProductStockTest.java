package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductStockTest {

    @Nested
    @DisplayName("재고 생성")
    class CreateProductStock {

        @Test
        @DisplayName("유효한 파라미터로 재고 생성 시 성공해야 한다")
        void createProductStockWithValidParameters() {
            // given
            int quantity = 100;
            Product product = ProductTestFixtures.createDefaultProduct(quantity);

            // when
            ProductStock stock = new ProductStock(product, quantity);

            // then
            assertThat(stock.getProduct()).isEqualTo(product);
            assertThat(stock.getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("상품이 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductIsNull() {
            // given
            Product product = null;
            int quantity = 100;

            // when & then
            assertThatThrownBy(() -> new ProductStock(product, quantity))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("상품은 null일 수 없습니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("수량이 0보다 작으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenQuantityIsNegative(int quantity) {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);

            // when & then
            assertThatThrownBy(() -> new ProductStock(product, quantity))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("quantity는 0보다 작을 수 없습니다");
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenQuantityIsNull() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            Integer quantity = null;

            // when & then
            assertThatThrownBy(() -> new ProductStock(product, quantity))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("quantity는 0보다 작을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("재고 가용성")
    class StockAvailability {

        @Test
        @DisplayName("재고가 0보다 크면 isAvailable이 true를 반환해야 한다")
        void isAvailableReturnsTrueWhenQuantityIsGreaterThanZero() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 10);

            // when & then
            assertThat(stock.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("재고가 0이면 isAvailable이 false를 반환해야 한다")
        void isAvailableReturnsFalseWhenQuantityIsZero() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 0);

            // when & then
            assertThat(stock.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("재고 증가")
    class IncreaseStock {

        @Test
        @DisplayName("재고를 증가시키면 수량이 증가해야 한다")
        void increaseStock() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 10);
            int initialQuantity = stock.getQuantity();
            int increaseAmount = 5;

            // when
            stock.increaseStock(increaseAmount);

            // then
            assertThat(stock.getQuantity()).isEqualTo(initialQuantity + increaseAmount);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -5, -10})
        @DisplayName("증가량이 음수이면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenIncreaseAmountIsNegative(int increaseAmount) {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 10);

            // when & then
            assertThatThrownBy(() -> stock.increaseStock(increaseAmount))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("증가시킬 수량은 음수일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("재고 감소")
    class DecreaseStock {

        @Test
        @DisplayName("재고를 감소시키면 수량이 감소해야 한다")
        void decreaseStock() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 10);
            int initialQuantity = stock.getQuantity();
            int decreaseAmount = 5;

            // when
            stock.decreaseStock(decreaseAmount);

            // then
            assertThat(stock.getQuantity()).isEqualTo(initialQuantity - decreaseAmount);
        }

        @Test
        @DisplayName("재고보다 많은 수량을 감소시키려 하면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenDecreaseAmountIsGreaterThanQuantity() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 10);
            int decreaseAmount = 15; // 현재 재고보다 많은 양

            // when & then
            assertThatThrownBy(() -> stock.decreaseStock(decreaseAmount))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("재고가 부족합니다");
        }

        @Test
        @DisplayName("재고와 같은 수량을 감소시키면 수량이 0이 되어야 한다")
        void decreaseStockToZero() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            ProductStock stock = new ProductStock(product, 10);
            int decreaseAmount = 10; // 현재 재고와 같은 양

            // when
            stock.decreaseStock(decreaseAmount);

            // then
            assertThat(stock.getQuantity()).isEqualTo(0);
        }
    }
}