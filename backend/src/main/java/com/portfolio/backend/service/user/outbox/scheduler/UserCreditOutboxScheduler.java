package com.portfolio.backend.service.user.outbox.scheduler;

import com.portfolio.backend.domain.user.outbox.UserCreditOrderOutbox;
import com.portfolio.backend.domain.user.repository.UserCreditOrderOutboxRepository;
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

    @Scheduled(fixedRate = 5000) // 5초마다
    public void processOrderOutboxEveryTenSeconds() {
        log.info("Processing user credit outbox - 10 seconds scheduler");

        Optional<List<UserCreditOrderOutbox>> response = userCreditOrderOutboxRepository.findAllByOutboxStatusIsNull();

        response.ifPresent(userCreditOrderOutboxes ->
                userCreditOrderOutboxes.forEach(userCreditOrderOutboxManager::userCreditOrderOutboxProcess));

    }
}
