package com.portfolio.backend.domain.product.repository;

import com.portfolio.backend.common.event.ProductStockStatus;
import com.portfolio.backend.domain.product.outbox.ProductStockOrderOutbox;
import com.portfolio.backend.domain.common.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockOrderOutboxRepository extends JpaRepository<ProductStockOrderOutbox, UUID> {

    Optional<ProductStockOrderOutbox> findBySagaIdAndOutboxStatus(UUID sagaId, OutboxStatus outboxStatus);

    Optional<List<ProductStockOrderOutbox>> findAllByOutboxStatusIsNull();

    Optional<List<ProductStockOrderOutbox>> findAllByOutboxStatusAndProductStockStatus(OutboxStatus outboxStatus, ProductStockStatus productStockStatus);

}
