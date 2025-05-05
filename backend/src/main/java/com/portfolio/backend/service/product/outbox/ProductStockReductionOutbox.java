package com.portfolio.backend.service.product.outbox;

import com.portfolio.backend.domain.order.entity.OrderStatus;
import com.portfolio.backend.service.common.outbox.SagaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_stock_reduction_outbox")
@Entity
public class ProductStockReductionOutbox {

    @Id
    private UUID id;
    private Long sagaId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String payload;
    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
//    @Enumerated(EnumType.STRING)
//    private OutboxStatus outboxStatus;
    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductStockReductionOutbox that = (ProductStockReductionOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
