package com.portfolio.backend.domain.user.event.listener;

import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.event.UserCreatedEvent;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDomainEventListener {

    private final UserCreditRepository creditRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductStockChangedEvent(UserCreatedEvent event) {
        log.info("UserCreatedEvent received userId : {}", event.getUser().getId());

        creditRepository.save(new UserCredit(event.getUser()));
    }
}
