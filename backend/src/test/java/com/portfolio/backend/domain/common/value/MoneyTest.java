package com.portfolio.backend.domain.common.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Money 값 객체에 대한 단위 테스트
 */
@DisplayName("Money 값 객체 테스트")
class MoneyTest {

    @Nested
    @DisplayName("Money 생성")
    class MoneyCreate {

        @Test
        @DisplayName("BigDecimal로 Money를 생성할 수 있다")
        void shouldCreateMoneyWithPositiveBigDecimal() {
            // Given
            BigDecimal amount = new BigDecimal("1000.50");

            // When
            Money money = new Money(amount);

            // Then
            assertThat(money.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("0으로 Money를 생성할 수 있다")
        void shouldCreateMoneyWithZeroBigDecimal() {
            // Given
            BigDecimal amount = BigDecimal.ZERO;

            // When
            Money money = new Money(amount);

            // Then
            assertThat(money.getAmount()).isEqualTo(amount);
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수로 Money를 생성할 수 없다")
        void shouldThrowExceptionWhenBigDecimalIsNegative() {
            // Given
            BigDecimal amount = new BigDecimal("-100");

            // When & Then
            assertThatThrownBy(() -> new Money(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("금액은 음수가 될 수 없습니다");
        }

        @Test
        @DisplayName("null로 Money를 생성할 수 없다")
        void shouldThrowExceptionWhenBigDecimalIsNull() {
            // Given
            BigDecimal amount = null;

            // When & Then
            assertThatThrownBy(() -> new Money(amount))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Money 비교")
    class MoneyComparison {

        @Test
        @DisplayName("같은 금액의 Money 객체들은 동등해야 한다")
        void shouldBeEqualWhenAmountsAreEqual() {
            // Given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("1000.00"));

            // When & Then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 금액의 Money 객체들은 동등하지 않아야 한다")
        void shouldNotBeEqualWhenAmountsAreDifferent() {
            // Given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("2000.00"));

            // When & Then
            assertThat(money1).isNotEqualTo(money2);
        }

        @ParameterizedTest
        @CsvSource({
            "1000.00, 2000.00, true",
            "1000.00, 1000.00, true",
            "2000.00, 1000.00, false"
        })
        @DisplayName("isLessThanOrEqual 메서드는 금액 비교를 올바르게 수행해야 한다")
        void shouldCorrectlyCompareAmountsWithIsLessThanOrEqual(String amount1, String amount2, boolean expected) {
            // Given
            Money money1 = new Money(new BigDecimal(amount1));
            Money money2 = new Money(new BigDecimal(amount2));

            // When & Then
            assertThat(money1.isLessThanOrEqual(money2)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Money 산술 연산")
    class MoneyArithmetic {

        @Test
        @DisplayName("두 Money 객체를 더할 수 있어야 한다")
        void shouldAddTwoMoneyObjects() {
            // Given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("500.50"));
            BigDecimal expected = new BigDecimal("1500.50");

            // When
            Money result = money1.add(money2);

            // Then
            assertThat(result.getAmount()).isEqualTo(expected);
        }

        @Test
        @DisplayName("한 Money 객체에서 다른 Money 객체를 뺄 수 있어야 한다")
        void shouldSubtractOneMoneyfromAnother() {
            // Given
            Money money1 = new Money(new BigDecimal("1000.00"));
            Money money2 = new Money(new BigDecimal("300.00"));
            BigDecimal expected = new BigDecimal("700.00");

            // When
            Money result = money1.subtract(money2);

            // Then
            assertThat(result.getAmount()).isEqualTo(expected);
        }

        @Test
        @DisplayName("Money 객체에 BigDecimal을 곱할 수 있어야 한다")
        void shouldMultiplyMoneyWithBigDecimal() {
            // Given
            Money money = new Money(new BigDecimal("100.00"));
            BigDecimal multiplier = new BigDecimal("2.5");
            BigDecimal expected = new BigDecimal("250.00");

            // When
            Money result = money.multiply(multiplier);

            // Then
            assertThat(result.getAmount()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
            "1000.00, 0, 0.00",
            "500.50, 2, 1001.00",
            "100.00, 10, 1000.00",
            "33.33, 3, 99.99"
        })
        @DisplayName("Money 객체에 정수를 곱하면 올바른 결과를 반환해야 한다")
        void shouldMultiplyMoneyWithInteger(String amount, int multiplier, String expectedResult) {
            // Given
            Money money = new Money(new BigDecimal(amount));
            BigDecimal expected = new BigDecimal(expectedResult);

            // When
            Money result = money.multiply(multiplier);

            // Then
            assertThat(result.getAmount()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Money 기타 기능")
    class MoneyMiscTest {

        @Test
        @DisplayName("toString 메서드는 금액을 문자열로 변환해야 한다")
        void shouldConvertToStringCorrectly() {
            // Given
            Money money = new Money(new BigDecimal("1234.56"));
            String expected = "1234.56";

            // When
            String result = money.toString();

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.0", "0.00"})
        @DisplayName("0원 Money에 대해 isZero는 true를 반환해야 한다")
        void shouldReturnTrueForZeroAmount(String amount) {
            // Given
            Money money = new Money(new BigDecimal(amount));

            // When & Then
            assertThat(money.isZero()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.01", "1.00", "99999.99"})
        @DisplayName("0원이 아닌 Money에 대해 isZero는 false를 반환해야 한다")
        void shouldReturnFalseForNonZeroAmount(String amount) {
            // Given
            Money money = new Money(new BigDecimal(amount));

            // When & Then
            assertThat(money.isZero()).isFalse();
        }
    }
}