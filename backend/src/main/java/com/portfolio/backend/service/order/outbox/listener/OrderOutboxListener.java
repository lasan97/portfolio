package com.portfolio.backend.service.order.outbox.listener;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.order.outbox.PaymentOutbox;
import com.portfolio.backend.domain.order.outbox.ProductStockOutbox;
import com.portfolio.backend.domain.order.repository.PaymentOutboxRepository;
import com.portfolio.backend.domain.order.repository.ProductStockOutboxRepository;
import com.portfolio.backend.domain.common.outbox.OutboxStatus;
import com.portfolio.backend.service.order.outbox.OrderOutboxManager;
import com.portfolio.backend.service.order.outbox.event.ProductStockReductionResponseEvent;
import com.portfolio.backend.service.order.outbox.event.UserCreditPaymentResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxListener {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ProductStockOutboxRepository productStockOutboxRepository;
    private final OrderOutboxManager orderOutboxManager;

    @EventListener
    @Transactional
    public void handleUserCreditPaymentResponseEvent(UserCreditPaymentResponseEvent event) {
        log.info("UserCreditPaymentResponseEvent received saga id : {}", event.getSagaId());

        Optional<PaymentOutbox> response = paymentOutboxRepository.findBySagaIdAndOutboxStatus(event.getSagaId(), OutboxStatus.STARTED);

        if (response.isEmpty()) {
            log.error("PaymentOutbox가 존재하지 않습니다.");
            throw new DomainException("PaymentOutbox가 존재하지 않습니다.");
        }

        PaymentOutbox paymentOutbox = response.get();

        orderOutboxManager.userCreditPaymentResponseProcess(paymentOutbox, event.getPaymentStatus());
    }

    @EventListener
    @Transactional
    public void handleProductStockReductionResponseEvent(ProductStockReductionResponseEvent event) {
        log.info("ProductStockReductionResponseEvent received saga id : {}", event.getSagaId());

        Optional<ProductStockOutbox> response = productStockOutboxRepository.findBySagaIdAndOutboxStatus(event.getSagaId(), OutboxStatus.STARTED);

        if (response.isEmpty()) {
            log.error("ProductStockOutbox가 존재하지 않습니다.");
            throw new DomainException("ProductStockOutbox가 존재하지 않습니다.");
        }

        ProductStockOutbox productStockOutbox = response.get();

        orderOutboxManager.productStockReductionResponseProcess(productStockOutbox, event.getProductStockStatus());
    }
}
