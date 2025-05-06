package com.portfolio.backend.domain.product.outbox;

import com.portfolio.backend.common.event.ProductStockStatus;
import com.portfolio.backend.domain.order.entity.OrderStatus;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "product_stock_reduction_outbox")
@Entity
public class ProductStockOrderOutbox {

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

    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;

    @Enumerated(EnumType.STRING)
    private ProductStockStatus productStockStatus;

    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductStockOrderOutbox that = (ProductStockOrderOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
