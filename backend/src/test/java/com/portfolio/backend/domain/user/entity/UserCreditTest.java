package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.fixture.UserTestFixtures;
import com.portfolio.backend.domain.user.event.UserCreditAmountChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCreditTest {

    @Nested
    @DisplayName("UserCredit 생성 시")
    class CreateUserCreditTest {

        @Test
        @DisplayName("사용자 정보로 UserCredit을 생성할 수 있다")
        void shouldCreateUserCreditWithUser() {
            // Given
            User user = UserTestFixtures.createUser();

            // When
            UserCredit userCredit = new UserCredit(user);

            // Then
            assertThat(userCredit.getUser()).isEqualTo(user);
            assertThat(userCredit.getAmount()).isEqualTo(Money.zero());
        }
    }

    @Nested
    @DisplayName("UserCredit 금액 추가 시")
    class AddAmountTest {

        @Test
        @DisplayName("양수 금액을 추가할 수 있다")
        void shouldAddPositiveAmount() {
            // Given
            User user = UserTestFixtures.createUser();
            UserCredit userCredit = new UserCredit(user);
            Money amountToAdd = new Money(BigDecimal.valueOf(10000));

            // When
            userCredit.add(amountToAdd);

            // Then
            assertThat(userCredit.getAmount()).isEqualTo(amountToAdd);
            
            // 이벤트가 등록되었는지 확인
            assertThat(userCredit.getDomainEvents()).hasSize(1);
            assertThat(userCredit.getDomainEvents().get(0)).isInstanceOf(UserCreditAmountChangedEvent.class);
            
            UserCreditAmountChangedEvent event = (UserCreditAmountChangedEvent) userCredit.getDomainEvents().get(0);
            assertThat(event.getTransactionType()).isEqualTo(CreditTransactionType.INCREASE);
            assertThat(event.getAmount()).isEqualTo(amountToAdd);
            assertThat(event.getBalanceAfterTransaction()).isEqualTo(amountToAdd);
        }

        @Test
        @DisplayName("0 금액을 추가하면 예외가 발생한다")
        void shouldThrowExceptionWhenAddingZeroAmount() {
            // Given
            User user = UserTestFixtures.createUser();
            UserCredit userCredit = new UserCredit(user);
            Money zeroAmount = Money.zero();

            // When & Then
            assertThatThrownBy(() -> userCredit.add(zeroAmount))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("충전금액은 0과 같거나 작을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("UserCredit 금액 차감 시")
    class SubtractAmountTest {

        @Test
        @DisplayName("양수 금액을 차감할 수 있다")
        void shouldSubtractPositiveAmount() {
            // Given
            User user = UserTestFixtures.createUser();
            UserCredit userCredit = new UserCredit(user);
            Money initialAmount = new Money(BigDecimal.valueOf(20000));
            Money amountToSubtract = new Money(BigDecimal.valueOf(10000));
            
            userCredit.add(initialAmount);
            // 이벤트 초기화
            userCredit.clearDomainEvents();

            // When
            userCredit.subtract(amountToSubtract);

            // Then
            Money expectedAmount = new Money(BigDecimal.valueOf(10000));
            assertThat(userCredit.getAmount()).isEqualTo(expectedAmount);
            
            // 이벤트가 등록되었는지 확인
            assertThat(userCredit.getDomainEvents()).hasSize(1);
            assertThat(userCredit.getDomainEvents().get(0)).isInstanceOf(UserCreditAmountChangedEvent.class);
            
            UserCreditAmountChangedEvent event = (UserCreditAmountChangedEvent) userCredit.getDomainEvents().get(0);
            assertThat(event.getTransactionType()).isEqualTo(CreditTransactionType.DECREASE);
            assertThat(event.getAmount()).isEqualTo(amountToSubtract);
            assertThat(event.getBalanceAfterTransaction()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("0 금액을 차감하면 예외가 발생한다")
        void shouldThrowExceptionWhenSubtractingZeroAmount() {
            // Given
            User user = UserTestFixtures.createUser();
            UserCredit userCredit = new UserCredit(user);
            Money zeroAmount = Money.zero();

            // When & Then
            assertThatThrownBy(() -> userCredit.subtract(zeroAmount))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("차감금액은 0과 같거나 작을 수 없습니다");
        }

        @Test
        @DisplayName("잔액보다 큰 금액은 차감할 수 없다.")
        void shouldAllowNegativeBalanceWhenSubtractingMoreThanBalance() {
            // Given
            User user = UserTestFixtures.createUser();
            UserCredit userCredit = new UserCredit(user);
            Money initialAmount = new Money(BigDecimal.valueOf(5000));
            Money amountToSubtract = new Money(BigDecimal.valueOf(10000));
            
            userCredit.add(initialAmount);
            // 이벤트 초기화
            userCredit.clearDomainEvents();

            // When & then
            assertThatThrownBy(()  -> userCredit.subtract(amountToSubtract))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("잔액이 부족합니다.");

            // Then
            Money expectedAmount = new Money(BigDecimal.valueOf(5000));
            assertThat(userCredit.getAmount()).isEqualTo(expectedAmount);
        }
    }
}
