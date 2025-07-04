package com.portfolio.backend.domain.user.event.listener;

import com.portfolio.backend.domain.user.entity.UserCreditHistory;
import com.portfolio.backend.domain.user.event.UserCreditAmountChangedEvent;
import com.portfolio.backend.domain.user.repository.UserCreditHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreditDomainEventListener {

    private final UserCreditHistoryRepository userCreditHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProductStockChangedEvent(UserCreditAmountChangedEvent event) {
        log.debug("UserCreditAmountChangedEvent received userCreditId : {}", event.getUserCredit().getId());

        UserCreditHistory creditHistory = UserCreditHistory.builder()
                .userCredit(event.getUserCredit())
                .transactionType(event.getTransactionType())
                .amount(event.getAmount())
                .balanceAfterTransaction(event.getBalanceAfterTransaction())
                .description(event.getDescription())
                .transactionDateTime(event.getTransactionDateTime())
                .build();

        userCreditHistoryRepository.save(creditHistory);
    }
}
