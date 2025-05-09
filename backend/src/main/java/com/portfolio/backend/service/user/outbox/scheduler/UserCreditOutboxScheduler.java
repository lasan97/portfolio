package com.portfolio.backend.service.user.outbox.scheduler;

import com.portfolio.backend.common.event.PaymentStatus;
import com.portfolio.backend.domain.user.outbox.UserCreditOrderOutbox;
import com.portfolio.backend.domain.user.repository.UserCreditOrderOutboxRepository;
import com.portfolio.backend.domain.common.outbox.OutboxStatus;
import com.portfolio.backend.service.user.outbox.UserCreditOrderOutboxManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class UserCreditOutboxScheduler {

    private final UserCreditOrderOutboxRepository userCreditOrderOutboxRepository;
    private final UserCreditOrderOutboxManager userCreditOrderOutboxManager;

    @Scheduled(fixedRate = 5000)
    public void processOrderOutbox() {

        Optional<List<UserCreditOrderOutbox>> response = userCreditOrderOutboxRepository.findAllByOutboxStatusIsNull();
        Optional<List<UserCreditOrderOutbox>> failureResponse = userCreditOrderOutboxRepository
                .findAllByOutboxStatusAndPaymentStatus(OutboxStatus.STARTED, PaymentStatus.FAILED);

        if (response.isPresent() && !response.get().isEmpty()) {
            List<UserCreditOrderOutbox> userCreditOrderOutboxes = response.get();
            log.info("Processing user credit outbox size: {}", userCreditOrderOutboxes.size());
            userCreditOrderOutboxes.forEach(userCreditOrderOutboxManager::userCreditOrderOutboxProcess);
        }

        if (failureResponse.isPresent() && !failureResponse.get().isEmpty()) {
            List<UserCreditOrderOutbox> userCreditOrderOutboxes = failureResponse.get();
            log.info("Failure user credit outbox size: {}", userCreditOrderOutboxes.size());
            userCreditOrderOutboxes.forEach(userCreditOrderOutboxManager::userCreditOrderOutboxFailure);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void processCompensating() {
        Optional<List<UserCreditOrderOutbox>> compensatingOutboxesResponse = userCreditOrderOutboxRepository
                .findAllByOutboxStatusAndPaymentStatus(OutboxStatus.STARTED, PaymentStatus.COMPENSATING);

        if (compensatingOutboxesResponse.isPresent() && !compensatingOutboxesResponse.get().isEmpty()) {
            List<UserCreditOrderOutbox> userCreditOrderOutboxes = compensatingOutboxesResponse.get();
            log.info("Compensating user credit outbox size: {}", userCreditOrderOutboxes.size());
            userCreditOrderOutboxes.forEach(userCreditOrderOutboxManager::userCreditOrderOutboxCompensation);
        }
    }
}
