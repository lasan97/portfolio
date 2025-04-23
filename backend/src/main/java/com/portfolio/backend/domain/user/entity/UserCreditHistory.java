package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.common.config.converter.MoneyConverter;
import com.portfolio.backend.domain.common.value.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_credit_histories")
public class UserCreditHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_credit_id", nullable = false)
    private UserCredit userCredit;

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditTransactionType type;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money amount;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money balanceAfterTransaction;

    private String description;

    @Builder
    public UserCreditHistory(UserCredit userCredit, LocalDateTime transactionDateTime, CreditTransactionType type,
                             Money amount, Money balanceAfterTransaction, String description) {
        this.userCredit = userCredit;
        this.transactionDateTime = transactionDateTime;
        this.type = type;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.description = description;
    }
}
