package com.portfolio.backend.service.user.dto;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.CreditTransactionType;

import java.time.LocalDateTime;

public class UserCreditServiceResponse {

    public record Get(
            Long id,
            Money amount,
            LocalDateTime updatedAt
    ) {}

    public record GetHistoryPage(
            Long id,
            CreditTransactionType transactionType,
            Money amount,
            LocalDateTime transactionDateTime
    ) {}
}
