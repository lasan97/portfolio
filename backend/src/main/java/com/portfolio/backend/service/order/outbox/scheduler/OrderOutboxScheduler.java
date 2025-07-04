package com.portfolio.backend.service.order.outbox.scheduler;

import com.portfolio.backend.domain.common.outbox.SagaType;
import com.portfolio.backend.domain.order.outbox.PaymentOutbox;
import com.portfolio.backend.domain.order.outbox.ProductStockOutbox;
import com.portfolio.backend.domain.order.repository.PaymentOutboxRepository;
import com.portfolio.backend.domain.order.repository.ProductStockOutboxRepository;
import com.portfolio.backend.domain.common.outbox.OutboxStatus;
import com.portfolio.backend.domain.common.outbox.SagaStatus;
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

    @Scheduled(fixedRate = 5000)
    public void processOrderOutbox() {
        Optional<List<PaymentOutbox>> paymentOutboxesResponse = paymentOutboxRepository.findAllBySagaTypeAndOutboxStatusIsNull(SagaType.ORDER);
        Optional<List<ProductStockOutbox>> productStockOutboxesResponse = productStockOutboxRepository.findAllBySagaTypeAndOutboxStatusIsNull(SagaType.ORDER);
        Optional<List<PaymentOutbox>> cancelOutboxesResponse = paymentOutboxRepository.findAllBySagaTypeAndOutboxStatusIsNull(SagaType.ORDER_CANCELING);
        Optional<List<ProductStockOutbox>> cancelStockOutboxesResponse = productStockOutboxRepository.findAllBySagaTypeAndOutboxStatusIsNull(SagaType.ORDER_CANCELING);

        if (paymentOutboxesResponse.isPresent() && !paymentOutboxesResponse.get().isEmpty()) {
            List<PaymentOutbox> paymentOutboxes = paymentOutboxesResponse.get();
            log.info("Processing payment outbox size: {}", paymentOutboxes.size());
            paymentOutboxes.forEach(orderOutboxManager::paymentOutboxProcess);
        }

        if (productStockOutboxesResponse.isPresent() && !productStockOutboxesResponse.get().isEmpty()) {
            List<ProductStockOutbox> productStockOutboxes = productStockOutboxesResponse.get();
            log.info("Processing product stock outbox size: {}", productStockOutboxes.size());
            productStockOutboxes.forEach(orderOutboxManager::productStockOutboxProcess);
        }

        if (cancelOutboxesResponse.isPresent() && !cancelOutboxesResponse.get().isEmpty()) {
            List<PaymentOutbox> paymentOutboxes = cancelOutboxesResponse.get();
            log.info("Processing cancel outbox size: {}", paymentOutboxes.size());
            paymentOutboxes.forEach(orderOutboxManager::paymentOutboxCancelProcess);
        }

        if (cancelStockOutboxesResponse.isPresent() && !cancelStockOutboxesResponse.get().isEmpty()) {
            List<ProductStockOutbox> productStockOutboxes = cancelStockOutboxesResponse.get();
            log.info("Processing product stock cancel outbox size: {}", productStockOutboxes.size());
            productStockOutboxes.forEach(orderOutboxManager::productStockOutboxCancelProcess);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void processCompensating() {
        Optional<List<PaymentOutbox>> compensatingOutboxesResponse = paymentOutboxRepository.findAllBySagaStatusAndOutboxStatus(SagaStatus.COMPENSATING, OutboxStatus.COMPLETED);

        if (compensatingOutboxesResponse.isPresent() && !compensatingOutboxesResponse.get().isEmpty()) {
            List<PaymentOutbox> paymentOutboxes = compensatingOutboxesResponse.get();
            log.info("Compensating payment outbox size: {}", paymentOutboxes.size());
            paymentOutboxes.forEach(orderOutboxManager::paymentOutboxCompensation);
        }
    }
}
