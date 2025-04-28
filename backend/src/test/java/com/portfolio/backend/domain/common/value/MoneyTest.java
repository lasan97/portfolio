package com.portfolio.backend.domain.common.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    @DisplayName("Money 생성 시")
    class CreateMoneyTest {

        @Test
        @DisplayName("양수 금액으로 Money 객체를 생성할 수 있다")
        void shouldCreateMoneyWithPositiveAmount() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(10000);

            // When
            Money money = new Money(amount);

            // Then
            assertThat(money.getAmount()).isEqualTo(amount.setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("0 금액으로 Money 객체를 생성할 수 있다")
        void shouldCreateMoneyWithZeroAmount() {
            // Given
            BigDecimal amount = BigDecimal.ZERO;

            // When
            Money money = new Money(amount);

            // Then
            assertThat(money.getAmount()).isEqualTo(amount.setScale(2, RoundingMode.HALF_UP));
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수 금액으로 Money 객체를 생성하면 예외가 발생한다")
        void shouldThrowExceptionWhenCreatingMoneyWithNegativeAmount() {
            // Given
            BigDecimal negativeAmount = BigDecimal.valueOf(-10000);

            // When, Then
            assertThatThrownBy(() -> new Money(negativeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("금액은 음수가 될 수 없습니다");
        }

        @Test
        @DisplayName("null 금액으로 Money 객체를 생성하면 예외가 발생한다")
        void shouldThrowExceptionWhenCreatingMoneyWithNullAmount() {
            // When, Then
            assertThatThrownBy(() -> new Money(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Money 연산 시")
    class MoneyOperationTest {

        @Test
        @DisplayName("Money 객체를 더할 수 있다")
        void shouldAddMoney() {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(10000));
            Money money2 = new Money(BigDecimal.valueOf(5000));

            // When
            Money result = money1.add(money2);

            // Then
            assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(15000).setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("Money 객체를 뺄 수 있다")
        void shouldSubtractMoney() {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(10000));
            Money money2 = new Money(BigDecimal.valueOf(5000));

            // When
            Money result = money1.subtract(money2);

            // Then
            assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(5000).setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("큰 금액에서 작은 금액을 뺄 수 있다")
        void shouldSubtractSmallerMoneyFromLargerMoney() {
            // Given
            Money larger = new Money(BigDecimal.valueOf(10000));
            Money smaller = new Money(BigDecimal.valueOf(5000));

            // When
            Money result = larger.subtract(smaller);

            // Then
            assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(5000).setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("Money 객체를 곱할 수 있다")
        void shouldMultiplyMoney() {
            // Given
            Money money = new Money(BigDecimal.valueOf(10000));
            BigDecimal multiplier = BigDecimal.valueOf(2);

            // When
            Money result = money.multiply(multiplier);

            // Then
            assertThat(result.getAmount().longValue()).isEqualTo(20000L);
        }
    }

    @Nested
    @DisplayName("Money 비교 시")
    class MoneyComparisonTest {

        @ParameterizedTest
        @CsvSource({
                "1000, 1000, true",
                "1000, 2000, true",
                "2000, 1000, false"
        })
        @DisplayName("금액이 같거나 작을 때 isLessThanOrEqual 메서드는 true를 반환한다")
        void shouldReturnTrueWhenMoneyIsLessThanOrEqual(int amount1, int amount2, boolean expected) {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(amount1));
            Money money2 = new Money(BigDecimal.valueOf(amount2));

            // When
            boolean result = money1.isLessThanOrEqual(money2);

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "1000, 2000, true",
                "1000, 1000, false",
                "2000, 1000, false"
        })
        @DisplayName("금액이 작을 때만 isLessThan 메서드는 true를 반환한다")
        void shouldReturnTrueWhenMoneyIsLessThan(int amount1, int amount2, boolean expected) {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(amount1));
            Money money2 = new Money(BigDecimal.valueOf(amount2));

            // When
            boolean result = money1.isLessThan(money2);

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Money 유틸리티 메서드 사용 시")
    class MoneyUtilityTest {

        @Test
        @DisplayName("zero() 메서드는 0 금액의 Money 객체를 반환한다")
        void shouldReturnZeroMoney() {
            // When
            Money zeroMoney = Money.zero();

            // Then
            assertThat(zeroMoney.getAmount()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            assertThat(zeroMoney.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("Money 객체 equals/hashCode 메서드 테스트")
    class MoneyEqualsHashCodeTest {

        @Test
        @DisplayName("같은 금액의 Money 객체는 equals 메서드로 비교하면 true를 반환한다")
        void shouldReturnTrueWhenComparingEqualMoney() {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(10000));
            Money money2 = new Money(BigDecimal.valueOf(10000));

            // When, Then
            assertThat(money1).isEqualTo(money2);
        }

        @Test
        @DisplayName("다른 금액의 Money 객체는 equals 메서드로 비교하면 false를 반환한다")
        void shouldReturnFalseWhenComparingDifferentMoney() {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(10000));
            Money money2 = new Money(BigDecimal.valueOf(20000));

            // When, Then
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("null과 Money 객체는 equals 메서드로 비교하면 false를 반환한다")
        void shouldReturnFalseWhenComparingMoneyWithNull() {
            // Given
            Money money = new Money(BigDecimal.valueOf(10000));

            // When, Then
            assertThat(money).isNotEqualTo(null);
        }

        @Test
        @DisplayName("같은 금액의 Money 객체는 같은 hashCode를 가진다")
        void shouldHaveSameHashCodeForEqualMoney() {
            // Given
            Money money1 = new Money(BigDecimal.valueOf(10000));
            Money money2 = new Money(BigDecimal.valueOf(10000));

            // When, Then
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }
    }
}
