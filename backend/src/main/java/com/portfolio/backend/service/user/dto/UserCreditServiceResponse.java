package com.portfolio.backend.service.user.dto;

import com.portfolio.backend.domain.common.value.Money;

public class UserCreditServiceResponse {

    public record Get(
            Long id,
            Money amount
    ) {
    }
}
