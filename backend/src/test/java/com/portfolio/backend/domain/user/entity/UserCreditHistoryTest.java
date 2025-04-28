package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.fixture.UserTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreditHistoryTest {

    @Test
    @DisplayName("UserCreditHistory를 생성할 수 있다")
    void shouldCreateUserCreditHistory() {
        // Given
        User user = UserTestFixtures.createUser();
        UserCredit userCredit = new UserCredit(user);
        LocalDateTime transactionDateTime = LocalDateTime.now();
        CreditTransactionType transactionType = CreditTransactionType.INCREASE;
        Money amount = new Money(BigDecimal.valueOf(10000));
        Money balanceAfterTransaction = new Money(BigDecimal.valueOf(10000));
        String description = "테스트 충전";

        // When
        UserCreditHistory history = UserCreditHistory.builder()
                .userCredit(userCredit)
                .transactionDateTime(transactionDateTime)
                .transactionType(transactionType)
                .amount(amount)
                .balanceAfterTransaction(balanceAfterTransaction)
                .description(description)
                .build();

        // Then
        assertThat(history.getUserCredit()).isEqualTo(userCredit);
        assertThat(history.getTransactionDateTime()).isEqualTo(transactionDateTime);
        assertThat(history.getTransactionType()).isEqualTo(transactionType);
        assertThat(history.getAmount()).isEqualTo(amount);
        assertThat(history.getBalanceAfterTransaction()).isEqualTo(balanceAfterTransaction);
        assertThat(history.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("설명(description)이 없는 UserCreditHistory를 생성할 수 있다")
    void shouldCreateUserCreditHistoryWithoutDescription() {
        // Given
        User user = UserTestFixtures.createUser();
        UserCredit userCredit = new UserCredit(user);
        LocalDateTime transactionDateTime = LocalDateTime.now();
        CreditTransactionType transactionType = CreditTransactionType.INCREASE;
        Money amount = new Money(BigDecimal.valueOf(10000));
        Money balanceAfterTransaction = new Money(BigDecimal.valueOf(10000));

        // When
        UserCreditHistory history = UserCreditHistory.builder()
                .userCredit(userCredit)
                .transactionDateTime(transactionDateTime)
                .transactionType(transactionType)
                .amount(amount)
                .balanceAfterTransaction(balanceAfterTransaction)
                .build();

        // Then
        assertThat(history.getUserCredit()).isEqualTo(userCredit);
        assertThat(history.getTransactionDateTime()).isEqualTo(transactionDateTime);
        assertThat(history.getTransactionType()).isEqualTo(transactionType);
        assertThat(history.getAmount()).isEqualTo(amount);
        assertThat(history.getBalanceAfterTransaction()).isEqualTo(balanceAfterTransaction);
        assertThat(history.getDescription()).isNull();
    }
}
