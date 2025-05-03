package com.portfolio.backend.domain.product.event;

import com.portfolio.backend.domain.common.event.DomainEvent;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.StockChangeReason;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductStockChangedEvent extends DomainEvent {

    private final Product product;
    private final int previousQuantity;
    private final int changedQuantity;
    private final StockChangeReason reason;
    private final String memo;

    @Builder
    public ProductStockChangedEvent(Product product, int previousQuantity, int changedQuantity, StockChangeReason reason, String memo, LocalDateTime transactionDateTime) {
        super(transactionDateTime);
        this.product = product;
        this.previousQuantity = previousQuantity;
        this.changedQuantity = changedQuantity;
        this.reason = reason;
        this.memo = memo;
    }
}
