package com.portfolio.backend.service.user.dto;

import com.portfolio.backend.domain.user.entity.UserCredit;
import org.springframework.stereotype.Component;

@Component
public class UserCreditServiceMapper {

    public UserCreditServiceResponse.Get toGet(UserCredit userCredit) {
        return new UserCreditServiceResponse.Get(
                userCredit.getId(),
                userCredit.getAmount()
        );
    }
}
