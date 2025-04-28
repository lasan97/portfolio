package com.portfolio.backend.domain.user.fixture;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.CreditTransactionType;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.entity.UserCreditHistory;

import java.time.LocalDateTime;

public class UserCreditHistoryTestFixtures {

    public static UserCreditHistory createUserCreditHistory(UserCredit credit, Money amount, Money affterMoney, CreditTransactionType transactionType) {
        return UserCreditHistory.builder()
                .userCredit(credit)
                .transactionDateTime(LocalDateTime.now())
                .transactionType(transactionType)
                .amount(amount)
                .balanceAfterTransaction(affterMoney)
                .build();
    }

    public static UserCreditHistory createUserCreditHistoryIncreaseInAfterZero(UserCredit credit, Money amount) {
        return createUserCreditHistory(credit, amount, Money.zero(), CreditTransactionType.INCREASE);
    }
}
