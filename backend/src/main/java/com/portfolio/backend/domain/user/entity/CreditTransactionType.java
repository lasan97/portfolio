package com.portfolio.backend.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreditTransactionType {

    INCREASE("증가"),
    DECREASE("차감");

    private final String description;
}
