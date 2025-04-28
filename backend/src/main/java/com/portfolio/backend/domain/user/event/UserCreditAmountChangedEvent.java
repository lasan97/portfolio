package com.portfolio.backend.domain.user.event;

import com.portfolio.backend.domain.common.event.DomainEvent;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.CreditTransactionType;
import com.portfolio.backend.domain.user.entity.UserCredit;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCreditAmountChangedEvent extends DomainEvent {

    private final UserCredit userCredit;
    private final CreditTransactionType transactionType;
    private final Money amount;
    private final Money balanceAfterTransaction;
    private final String description;

    @Builder
    public UserCreditAmountChangedEvent(UserCredit userCredit, LocalDateTime transactionDateTime, CreditTransactionType transactionType,
                             Money amount, Money balanceAfterTransaction, String description) {
        super(transactionDateTime);
        this.userCredit = userCredit;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.description = description;
    }
}
