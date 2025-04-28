package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.common.config.converter.MoneyConverter;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.entity.AggregateRoot;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.event.UserCreditAmountChangedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_credits")
@EntityListeners(AuditingEntityListener.class)
public class UserCredit extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money amount;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(updatable = false, nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public UserCredit(User user) {
        this.amount = new Money(BigDecimal.ZERO);
        this.user = user;
    }
    
    public void add(Money amount) {
        if (amount.isLessThanOrEqual(Money.zero())) {
            throw new UnprocessableEntityException("충전금액은 0과 같거나 작을 수 없습니다.");
        }

        this.amount = this.amount.add(amount);

        registerEvent(UserCreditAmountChangedEvent.builder()
                .userCredit(this)
                .transactionType(CreditTransactionType.INCREASE)
                .amount(amount)
                .balanceAfterTransaction(this.amount)
                .transactionDateTime(LocalDateTime.now())
                .build());
    }

    public void subtract(Money amount) {
        if (amount.isLessThanOrEqual(Money.zero())) {
            throw new UnprocessableEntityException("차감금액은 0과 같거나 작을 수 없습니다.");
        }

        if (this.amount.isLessThan(amount)) {
            throw new UnprocessableEntityException("잔액이 부족합니다.");
        }

        this.amount = this.amount.subtract(amount);

        registerEvent(UserCreditAmountChangedEvent.builder()
                .userCredit(this)
                .transactionType(CreditTransactionType.DECREASE)
                .amount(amount)
                .balanceAfterTransaction(this.amount)
                .transactionDateTime(LocalDateTime.now())
                .build());
    }
}
