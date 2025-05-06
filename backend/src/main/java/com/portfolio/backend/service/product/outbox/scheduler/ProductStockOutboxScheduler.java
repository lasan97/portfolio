package com.portfolio.backend.service.product.outbox.scheduler;

import com.portfolio.backend.domain.product.outbox.ProductStockOrderOutbox;
import com.portfolio.backend.domain.product.repository.ProductStockOrderOutboxRepository;
import com.portfolio.backend.domain.user.outbox.UserCreditOrderOutbox;
import com.portfolio.backend.domain.user.repository.UserCreditOrderOutboxRepository;
import com.portfolio.backend.service.product.outbox.ProductStockOrderOutboxManager;
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
public class ProductStockOutboxScheduler {

    private final ProductStockOrderOutboxRepository productStockOrderOutboxRepository;
    private final ProductStockOrderOutboxManager productStockOrderOutboxManager;

    @Scheduled(fixedRate = 5000) // 5초마다
    public void processOrderOutboxEveryTenSeconds() {
        log.info("Processing user credit outbox - 10 seconds scheduler");

        Optional<List<ProductStockOrderOutbox>> response = productStockOrderOutboxRepository.findAllByOutboxStatusIsNull();

        response.ifPresent(productStockOrderOutboxes ->
                productStockOrderOutboxes.forEach(productStockOrderOutboxManager::productStockOrderOutboxProcess));

    }
}
