package com.portfolio.backend.domain.common.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    @DisplayName("Money 생성")
    class MoneyCreate {

        @Test
        @DisplayName("BigDecimal로 Money를 생성할 수 있다")
        void createMoneyWithPositiveBigDecimal() {
            // given
            BigDecimal amount = new BigDecimal("1000.50");

            // when
            Money money = new Money(amount);

            // then
            assertThat(money.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("0으로 Money를 생성할 수 있다")
        void createMoneyWithZeroBigDecimal() {
            // given
            BigDecimal amount = BigDecimal.ZERO;

            // when
            Money money = new Money(amount);

            // then
            assertThat(money.getAmount()).isEqualTo(amount);
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수로 Money를 생성할 수 없다")
        void shouldThrowExceptionWhenBigDecimalIsNegative() {
            // given
            BigDecimal amount = new BigDecimal("-100");

            // when // then
            assertThatThrownBy(() -> new Money(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("금액은 음수가 될 수 없습니다");
        }

        @Test
        @DisplayName("null로 Money를 생성할 수 없다")
        void shouldThrowExceptionWhenBigDecimalIsNull() {
            // given
            BigDecimal amount = null;

            // when // then
            assertThatThrownBy(() -> new Money(amount))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Money 비교")
    class MoneyComparison {

        @Test
        @DisplayName("같은 금액의 Money 들은 동등해야 한다")
        void equalMoney() {
            // given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("1000.00"));

            // when // then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 금액의 Money 들은 동등하지 않아야 한다")
        void unequalMoneyObjects() {
            // given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("2000.00"));

            // when // then
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("작거나 같은 금액 비교 메서드가 올바르게 동작해야 한다")
        void isLessThanOrEqualTest() {
            // given
            Money smaller = new Money(new BigDecimal("1000.00"));
            Money equal = new Money(new BigDecimal("1000.00"));
            Money bigger = new Money(new BigDecimal("2000.00"));

            // when & then
            assertThat(smaller.isLessThanOrEqual(bigger)).isTrue();
            assertThat(smaller.isLessThanOrEqual(equal)).isTrue();
            assertThat(bigger.isLessThanOrEqual(smaller)).isFalse();
        }
    }

    @Nested
    @DisplayName("Money 산술 연산 테스트")
    class MoneyArithmetic {

        @Test
        @DisplayName("Money 를 더할 수 있어야 한다")
        void addMoney() {
            // given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("500.50"));

            // when
            Money result = money1.add(money2);

            // then
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("1500.50"));
        }

        @Test
        @DisplayName("Money 를 뺄 수 있어야 한다")
        void subtractMoney() {
            // given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("300.00"));

            // when
            Money result = money1.subtract(money2);

            // then
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Money 에 BigDecimal을 곱할 수 있어야 한다")
        void multiplyMoneyWithBigDecimal() {
            // given
            Money money = new Money(new BigDecimal("100.00"));
            BigDecimal multiplier = new BigDecimal("2.5");

            // when
            Money result = money.multiply(multiplier);

            // then
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("Money 에 정수를 곱할 수 있어야 한다")
        void multiplyMoneyWithInteger() {
            // given
            Money money = new Money(new BigDecimal("100.00"));
            int multiplier = 3;

            // when
            Money result = money.multiply(multiplier);

            // then
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("300.00"));
        }

        @ParameterizedTest
        @CsvSource({
                "1000.00, 0, 0.00",
                "500.50, 2, 1001.00",
                "100.00, 10, 1000.00"
        })
        @DisplayName("Money 에 다양한 정수를 곱할 수 있어야 한다")
        void multiplyMoneyWithVariousIntegers(String amount, int multiplier, String expectedResult) {
            // given
            Money money = new Money(new BigDecimal(amount));

            // when
            Money result = money.multiply(multiplier);

            // then
            assertThat(result.getAmount()).isEqualTo(new BigDecimal(expectedResult));
        }
    }

    @Nested
    @DisplayName("Money 기타 기능 테스트")
    class MoneyMiscTest {

        @Test
        @DisplayName("Money의 toString은 금액을 문자열로 변환해야 한다")
        void toStringTest() {
            // given
            Money money = new Money(new BigDecimal("1234.56"));

            // when
            String result = money.toString();

            // then
            assertThat(result).isEqualTo("1234.56");
        }

        @Test
        @DisplayName("0원 Money에 대해 isZero는 true를 반환해야 한다")
        void isZeroReturnsTrueForZeroAmount() {
            // given
            Money money = new Money(BigDecimal.ZERO);

            // when & then
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("0원이 아닌 Money에 대해 isZero는 false를 반환해야 한다")
        void isZeroReturnsFalseForNonZeroAmount() {
            // given
            Money money = new Money(new BigDecimal("1.00"));

            // when & then
            assertThat(money.isZero()).isFalse();
        }
    }
}