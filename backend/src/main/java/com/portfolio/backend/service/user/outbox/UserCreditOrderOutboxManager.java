package com.portfolio.backend.service.user.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.common.event.PaymentStatus;
import com.portfolio.backend.common.event.payload.OrderPaymentEventPayload;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.outbox.UserCreditOrderOutbox;
import com.portfolio.backend.domain.user.repository.UserCreditOrderOutboxRepository;
import com.portfolio.backend.domain.common.outbox.OutboxStatus;
import com.portfolio.backend.service.order.outbox.event.UserCreditPaymentResponseEvent;
import com.portfolio.backend.service.user.UserCreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreditOrderOutboxManager {

    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final UserCreditService userCreditService;
    private final UserCreditOrderOutboxRepository userCreditOrderOutboxRepository;

    public void userCreditOrderOutboxProcess(UserCreditOrderOutbox userCreditOrderOutbox) {
        log.info("UserCreditOrderOutbox received order id : {}", userCreditOrderOutbox.getOrderId());

        try {
            OrderPaymentEventPayload payload = getPayload(userCreditOrderOutbox.getPayload(), OrderPaymentEventPayload.class);

            userCreditService.pay(payload.getUserId(), new Money(payload.getPrice()), "주문 결제");
            log.info("UserCreditOrderOutbox paid order id : {}", userCreditOrderOutbox.getOrderId());

            userCreditOrderOutbox.setPaymentStatus(PaymentStatus.COMPLETED);
            userCreditOrderOutbox.setOutboxStatus(OutboxStatus.COMPLETED);
            userCreditOrderOutbox.setProcessedAt(LocalDateTime.now());
            userCreditOrderOutboxRepository.save(userCreditOrderOutbox);

            eventPublisher.publishEvent(new UserCreditPaymentResponseEvent(
                    userCreditOrderOutbox.getSagaId(),
                    userCreditOrderOutbox.getPaymentStatus()));

        } catch (UnprocessableEntityException | DomainException e) {
            log.error("UserCreditOrderOutbox failed order id : {}", userCreditOrderOutbox.getOrderId());
            userCreditOrderOutbox.setPaymentStatus(PaymentStatus.FAILED);
            userCreditOrderOutbox.setOutboxStatus(OutboxStatus.STARTED);
            userCreditOrderOutbox.setProcessedAt(LocalDateTime.now());
            userCreditOrderOutboxRepository.save(userCreditOrderOutbox);
        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    public void userCreditOrderOutboxFailure(UserCreditOrderOutbox userCreditOrderOutbox) {
        log.info("UserCreditOrderOutbox failed order id : {}", userCreditOrderOutbox.getOrderId());
        userCreditOrderOutbox.setOutboxStatus(OutboxStatus.FAILED);
        userCreditOrderOutboxRepository.save(userCreditOrderOutbox);

        eventPublisher.publishEvent(new UserCreditPaymentResponseEvent(
                userCreditOrderOutbox.getSagaId(),
                userCreditOrderOutbox.getPaymentStatus()));
    }

    public void userCreditOrderOutboxCompensation(UserCreditOrderOutbox userCreditOrderOutbox) {
        log.info("UserCreditOrderOutbox compensation received order id : {}", userCreditOrderOutbox.getOrderId());

        try {
            OrderPaymentEventPayload payload = getPayload(userCreditOrderOutbox.getPayload(), OrderPaymentEventPayload.class);

            userCreditService.refund(payload.getUserId(), new Money(payload.getPrice()), "주문 실패 결제 환불");
            log.info("UserCreditOrderOutbox refund order id : {}", userCreditOrderOutbox.getOrderId());

            userCreditOrderOutbox.setPaymentStatus(PaymentStatus.COMPENSATED);
            userCreditOrderOutbox.setOutboxStatus(OutboxStatus.COMPLETED);
            userCreditOrderOutbox.setProcessedAt(LocalDateTime.now());
            userCreditOrderOutboxRepository.save(userCreditOrderOutbox);

            eventPublisher.publishEvent(new UserCreditPaymentResponseEvent(
                    userCreditOrderOutbox.getSagaId(),
                    userCreditOrderOutbox.getPaymentStatus()));

        } catch (OptimisticLockingFailureException e) {
            // No-Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    private <T> T getPayload(String payload, Class<T> outputType) {
        try {
            return objectMapper.readValue(payload, outputType);
        } catch (JsonProcessingException e) {
            log.error("Could not read {} object", outputType.getName(), e);
            throw new DomainException("Could not read " + outputType.getName() + " object", e);
        }
    }
}
