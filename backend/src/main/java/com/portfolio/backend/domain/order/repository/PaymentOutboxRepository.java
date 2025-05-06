package com.portfolio.backend.domain.order.repository;

import com.portfolio.backend.domain.order.outbox.PaymentOutbox;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {

    Optional<PaymentOutbox> findBySagaIdAndOutboxStatus(UUID sagaId, OutboxStatus outboxStatus);
    Optional<List<PaymentOutbox>> findAllByOutboxStatusIsNull();
}
