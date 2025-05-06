package com.portfolio.backend.service.order.outbox.scheduler;

import com.portfolio.backend.domain.order.outbox.PaymentOutbox;
import com.portfolio.backend.domain.order.outbox.ProductStockOutbox;
import com.portfolio.backend.domain.order.repository.PaymentOutboxRepository;
import com.portfolio.backend.domain.order.repository.ProductStockOutboxRepository;
import com.portfolio.backend.service.order.outbox.OrderOutboxManager;
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
public class OrderOutboxScheduler {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ProductStockOutboxRepository productStockOutboxRepository;
    private final OrderOutboxManager orderOutboxManager;

    @Scheduled(fixedRate = 5000) // 5초마다
    public void processOrderOutboxEveryTenSeconds() {
        log.info("Processing order outbox - 10 seconds scheduler");
        Optional<List<PaymentOutbox>> paymentOutboxesResponse = paymentOutboxRepository.findAllByOutboxStatusIsNull();
        Optional<List<ProductStockOutbox>> productStockOutboxesResponse = productStockOutboxRepository.findAllByOutboxStatusIsNull();

        paymentOutboxesResponse.ifPresent(paymentOutboxes ->
                paymentOutboxes.forEach(orderOutboxManager::paymentOutboxProcess));

        productStockOutboxesResponse.ifPresent(productStockOutboxes ->
                productStockOutboxes.forEach(orderOutboxManager::productStockOutboxProcess));
    }
}
