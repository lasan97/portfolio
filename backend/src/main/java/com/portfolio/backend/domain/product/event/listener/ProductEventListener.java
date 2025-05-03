package com.portfolio.backend.domain.product.event.listener;

import com.portfolio.backend.domain.product.entity.ProductStockHistory;
import com.portfolio.backend.domain.product.event.ProductStockChangedEvent;
import com.portfolio.backend.domain.product.repository.ProductStockHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductStockHistoryRepository productStockHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductStockChangedEvent(ProductStockChangedEvent event) {
        log.debug("ProductStockChangedEvent received productId : {}", event.getProduct().getId());

        ProductStockHistory productStockHistory = ProductStockHistory.builder()
                .product(event.getProduct())
                .previous(event.getPreviousQuantity())
                .changed(event.getChangedQuantity())
                .reason(event.getReason())
                .memo(event.getMemo())
                .build();

        productStockHistoryRepository.save(
                productStockHistory);
    }
}
