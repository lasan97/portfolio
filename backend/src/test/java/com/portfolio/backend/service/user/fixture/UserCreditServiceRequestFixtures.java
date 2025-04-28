package com.portfolio.backend.service.user.fixture;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;

import java.math.BigDecimal;

public class UserCreditServiceRequestFixtures {

    public static UserCreditServiceRequest.Increase createIncrease(BigDecimal amount) {
        return new UserCreditServiceRequest.Increase(new Money(amount));
    }
}
