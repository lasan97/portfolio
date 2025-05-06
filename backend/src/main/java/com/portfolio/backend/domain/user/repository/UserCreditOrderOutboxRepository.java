package com.portfolio.backend.domain.user.repository;

import com.portfolio.backend.common.event.PaymentStatus;
import com.portfolio.backend.domain.user.outbox.UserCreditOrderOutbox;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCreditOrderOutboxRepository extends JpaRepository<UserCreditOrderOutbox, UUID> {

    Optional<UserCreditOrderOutbox> findBySagaIdAndOutboxStatus(UUID sagaId, OutboxStatus outboxStatus);

    Optional<List<UserCreditOrderOutbox>> findAllByOutboxStatusIsNull();
    Optional<List<UserCreditOrderOutbox>> findAllByOutboxStatusAndPaymentStatus(OutboxStatus outboxStatus, PaymentStatus paymentStatus);
}
