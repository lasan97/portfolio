package com.portfolio.backend.service.user.dto;

import com.portfolio.backend.domain.common.value.Money;
import jakarta.validation.constraints.NotNull;

public class UserCreditServiceRequest {

    public record Increase(
            @NotNull(message = "충전 금액은 필수입니다.")
            Money amount
    ) {
    }
}
