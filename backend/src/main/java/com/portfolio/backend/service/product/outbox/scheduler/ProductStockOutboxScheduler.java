package com.portfolio.backend.service.product.outbox.scheduler;

import com.portfolio.backend.common.event.ProductStockStatus;
import com.portfolio.backend.domain.product.outbox.ProductStockOrderOutbox;
import com.portfolio.backend.domain.product.repository.ProductStockOrderOutboxRepository;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import com.portfolio.backend.service.product.outbox.ProductStockOrderOutboxManager;
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

    @Scheduled(fixedRate = 5000)
    public void processOrderOutbox() {
        Optional<List<ProductStockOrderOutbox>> response = productStockOrderOutboxRepository.findAllByOutboxStatusIsNull();
        Optional<List<ProductStockOrderOutbox>> failureResponse = productStockOrderOutboxRepository.findAllByOutboxStatusAndProductStockStatus(OutboxStatus.STARTED, ProductStockStatus.FAILED);

        if (response.isPresent() && !response.get().isEmpty()) {
            List<ProductStockOrderOutbox> productStockOrderOutboxes = response.get();
            log.info("Processing product stock outbox size: {}", productStockOrderOutboxes.size());
            productStockOrderOutboxes.forEach(productStockOrderOutboxManager::productStockOrderOutboxProcess);
        }

        if (failureResponse.isPresent() && !failureResponse.get().isEmpty()) {
            List<ProductStockOrderOutbox> productStockOrderOutboxes = failureResponse.get();
            log.info("Failure product stock outbox size: {}", productStockOrderOutboxes.size());
            productStockOrderOutboxes.forEach(productStockOrderOutboxManager::productStockOrderOutboxFailure);
        }
    }
}
