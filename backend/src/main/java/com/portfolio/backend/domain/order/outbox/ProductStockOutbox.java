package com.portfolio.backend.domain.order.outbox;

import com.portfolio.backend.domain.order.entity.OrderStatus;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import com.portfolio.backend.service.common.outbox.SagaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "product_stock_outbox")
@Entity
public class ProductStockOutbox {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sagaId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;

    @Version
    private int version;
}
