package com.portfolio.backend.service.order.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.portfolio.backend.common.event.PaymentStatus;
import com.portfolio.backend.common.event.ProductStockStatus;
import com.portfolio.backend.common.event.payload.ProductStockReductionEventPayload;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.outbox.PaymentOutbox;
import com.portfolio.backend.domain.order.outbox.ProductStockOutbox;
import com.portfolio.backend.domain.order.repository.OrderRepository;
import com.portfolio.backend.domain.order.repository.PaymentOutboxRepository;
import com.portfolio.backend.domain.order.repository.ProductStockOutboxRepository;
import com.portfolio.backend.domain.product.outbox.ProductStockOrderOutbox;
import com.portfolio.backend.domain.product.repository.ProductStockOrderOutboxRepository;
import com.portfolio.backend.domain.user.outbox.UserCreditOrderOutbox;
import com.portfolio.backend.domain.user.repository.UserCreditOrderOutboxRepository;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import com.portfolio.backend.service.common.outbox.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxManager {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ProductStockOutboxRepository productStockOutboxRepository;
    private final OrderRepository orderRepository;

    private final ObjectMapper objectMapper;

    private final ProductStockOrderOutboxRepository productStockOrderOutboxRepository;
    private final UserCreditOrderOutboxRepository userCreditOrderOutboxRepository;

    @Transactional
    public void paymentOutboxProcess(PaymentOutbox paymentOutbox) {
        log.info("PaymentOutbox received saga id : {}", paymentOutbox.getSagaId());

        try {
            paymentOutbox.setSagaStatus(SagaStatus.PROCESSING);
            paymentOutbox.setOutboxStatus(OutboxStatus.STARTED);
            paymentOutboxRepository.save(paymentOutbox);

            UserCreditOrderOutbox userCreditOrderOutbox = UserCreditOrderOutbox.builder()
                    .id(paymentOutbox.getId())
                    .sagaId(paymentOutbox.getSagaId())
                    .orderId(paymentOutbox.getOrderId())
                    .payload(paymentOutbox.getPayload())
                    .createdAt(LocalDateTime.now())
                    .build();

            userCreditOrderOutboxRepository.save(userCreditOrderOutbox);
        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    @Transactional
    public void productStockOutboxProcess(ProductStockOutbox productStockOutbox) {
        log.info("ProductStockOutbox received saga id : {}", productStockOutbox.getSagaId());

        try {
            productStockOutbox.setOutboxStatus(OutboxStatus.STARTED);
            productStockOutboxRepository.save(productStockOutbox);

            ProductStockOrderOutbox productStockOrderOutbox = ProductStockOrderOutbox.builder()
                    .id(productStockOutbox.getId())
                    .sagaId(productStockOutbox.getSagaId())
                    .orderId(productStockOutbox.getOrderId())
                    .payload(productStockOutbox.getPayload())
                    .createdAt(LocalDateTime.now())
                    .build();

            productStockOrderOutboxRepository.save(productStockOrderOutbox);
        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    @Transactional
    public void userCreditPaymentResponseProcess(PaymentOutbox paymentOutbox, PaymentStatus paymentStatus) {
        try {

            Order order = orderRepository.findById(paymentOutbox.getOrderId())
                    .orElseThrow(() -> new DomainException("주문 정보가 존재하지 않습니다."));

            if (paymentStatus == PaymentStatus.FAILED) {

                order.failed("결제 실패");

                paymentOutbox.setProcessedAt(LocalDateTime.now());
                paymentOutbox.setOutboxStatus(OutboxStatus.FAILED);
                paymentOutbox.setSagaStatus(SagaStatus.FAILED);
                paymentOutbox.setOrderStatus(order.getOrderStatus());

                paymentOutboxRepository.save(paymentOutbox);
            } else if (paymentStatus == PaymentStatus.COMPLETED) {

                order.paymentCompleted();

                paymentOutbox.setProcessedAt(LocalDateTime.now());
                paymentOutbox.setOutboxStatus(OutboxStatus.COMPLETED);
                paymentOutbox.setOrderStatus(order.getOrderStatus());
                paymentOutboxRepository.save(paymentOutbox);

                List<ProductStockReductionEventPayload.OrderItem> items = order.getOrderItems().stream()
                        .map(item -> new ProductStockReductionEventPayload.OrderItem(item.getProduct().getId(), item.getQuantity()))
                        .toList();

                ProductStockReductionEventPayload payload = ProductStockReductionEventPayload.builder()
                        .orderItems(items)
                        .build();

                String productStockEventPayload = createPayload(order.getId(), payload);
                ProductStockOutbox productStockOutbox = ProductStockOutbox.builder()
                        .id(UlidCreator.getUlid().toUuid())
                        .sagaId(paymentOutbox.getSagaId())
                        .orderId(order.getId())
                        .payload(productStockEventPayload)
                        .orderStatus(order.getOrderStatus())
                        .sagaStatus(paymentOutbox.getSagaStatus())
                        .createdAt(LocalDateTime.now())
                        .build();

                productStockOutboxRepository.save(productStockOutbox);
            } else if (paymentStatus == PaymentStatus.COMPLETED) {
                paymentOutbox.setSagaStatus(SagaStatus.COMPENSATED);
                paymentOutboxRepository.save(paymentOutbox);
            }
        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    @Transactional
    public void productStockReductionResponseProcess(ProductStockOutbox productStockOutbox, ProductStockStatus productStockStatus) {
        try {

            Order order = orderRepository.findById(productStockOutbox.getOrderId())
                    .orElseThrow(() -> new DomainException("주문 정보가 존재하지 않습니다."));

            if (productStockStatus == ProductStockStatus.FAILED) {

                order.failed("재고 부족");

                productStockOutbox.setProcessedAt(LocalDateTime.now());
                productStockOutbox.setOutboxStatus(OutboxStatus.FAILED);
                productStockOutbox.setSagaStatus(SagaStatus.FAILED);
                productStockOutbox.setOrderStatus(order.getOrderStatus());

                productStockOutboxRepository.save(productStockOutbox);

                PaymentOutbox paymentOutbox = paymentOutboxRepository.findBySagaIdAndOutboxStatus(productStockOutbox.getSagaId(), OutboxStatus.COMPLETED)
                        .orElseThrow(() -> new DomainException("PaymentOutbox가 존재하지 않습니다."));
                paymentOutbox.setSagaStatus(SagaStatus.COMPENSATING);
                paymentOutboxRepository.save(paymentOutbox);
            } else if (productStockStatus == ProductStockStatus.COMPLETED) {

                order.completedStockReduction();

                productStockOutbox.setProcessedAt(LocalDateTime.now());
                productStockOutbox.setOutboxStatus(OutboxStatus.COMPLETED);
                productStockOutbox.setSagaStatus(SagaStatus.SUCCEEDED);
                productStockOutbox.setOrderStatus(order.getOrderStatus());
                productStockOutboxRepository.save(productStockOutbox);

                PaymentOutbox paymentOutbox = paymentOutboxRepository.findBySagaIdAndOutboxStatus(productStockOutbox.getSagaId(), OutboxStatus.COMPLETED)
                        .orElseThrow(() -> new DomainException("PaymentOutbox가 존재하지 않습니다."));
                paymentOutbox.setSagaStatus(SagaStatus.SUCCEEDED);
                paymentOutboxRepository.save(paymentOutbox);
            }
        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    @Transactional
    public void paymentOutboxCompensation(PaymentOutbox paymentOutbox) {
        log.info("PaymentOutbox compensation received saga id : {}", paymentOutbox.getSagaId());

        try {
            paymentOutbox.setOutboxStatus(OutboxStatus.STARTED);
            paymentOutboxRepository.save(paymentOutbox);

            UserCreditOrderOutbox userCreditOrderOutbox = userCreditOrderOutboxRepository.findBySagaIdAndOutboxStatus(paymentOutbox.getSagaId(), OutboxStatus.COMPLETED)
                    .orElseThrow(() -> new DomainException("UserCreditOrderOutbox가 존재하지 않습니다."));
            userCreditOrderOutbox.setPaymentStatus(PaymentStatus.COMPENSATING);
            userCreditOrderOutboxRepository.save(userCreditOrderOutbox);
        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    private String createPayload(UUID orderId, ProductStockReductionEventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("ProductStockReductionEventPayload를 만드는데 실패했습니다. orderId: {}", orderId.toString(), e);
            throw new DomainException("ProductStockReductionEventPayload를 만드는데 실패했습니다. orderId :" + orderId.toString(), e);
        }
    }
}
