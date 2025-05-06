package com.portfolio.backend.domain.order.repository;

import com.portfolio.backend.domain.order.outbox.ProductStockOutbox;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockOutboxRepository extends JpaRepository<ProductStockOutbox, UUID> {

    Optional<ProductStockOutbox> findBySagaIdAndOutboxStatus(UUID sagaId, OutboxStatus outboxStatus);

    Optional<List<ProductStockOutbox>> findAllByOutboxStatusIsNull();
}
