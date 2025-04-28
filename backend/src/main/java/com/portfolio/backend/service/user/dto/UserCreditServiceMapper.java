package com.portfolio.backend.service.user.dto;

import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.entity.UserCreditHistory;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse.Get;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse.GetHistoryPage;
import org.springframework.stereotype.Component;

@Component
public class UserCreditServiceMapper {

    public Get toGet(UserCredit userCredit) {
        return new Get(
                userCredit.getId(),
                userCredit.getAmount(),
                userCredit.getUpdatedAt()
        );
    }

    public GetHistoryPage toGetHistoryPage(UserCreditHistory userCreditHistory) {
        return new GetHistoryPage(
                userCreditHistory.getId(),
                userCreditHistory.getTransactionType(),
                userCreditHistory.getAmount(),
                userCreditHistory.getTransactionDateTime()
        );
    }
}
