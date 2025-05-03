package com.portfolio.backend.domain.product.service;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.entity.ProductStock;
import com.portfolio.backend.domain.product.entity.StockChangeReason;
import com.portfolio.backend.domain.product.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductStockManager {

    private final ProductStockRepository productStockRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void refund(Product product, int quantity) {
        ProductStock productStock = getLockedByProductId(product.getId());

        productStock.increase(quantity, StockChangeReason.RETURN);

        // TODO - 여기있을 로직이 아님 테스트가 실패할 것
        if (product.getStatus() == ProductStatus.SOLD_OUT && productStock.isAvailable()) {
            product.active();
        }

        eventPublisher.publishEventsFrom(productStock);
    }

    @Transactional
    public void sale(Product product, int quantity) {
        ProductStock productStock = getLockedByProductId(product.getId());

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new DomainException("삭제된 상품은 재고를 변경할 수 없습니다.");
        }

        productStock.decrease(quantity, StockChangeReason.SALE);

        // TODO - 여기있을 로직이 아님 테스트가 실패할 것
        if (product.getStatus() == ProductStatus.ACTIVE && productStock.isAvailable()) {
            product.soldOut();
        }

        eventPublisher.publishEventsFrom(productStock);
    }

    @Transactional
    public void adjust(Product product, int quantity, String memo) {
        ProductStock productStock = getLockedByProductId(product.getId());

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new DomainException("삭제된 상품은 재고를 변경할 수 없습니다.");
        }

        productStock.adjust(quantity, memo);

        // TODO - 여기있을 로직이 아님 테스트가 실패할 것
        if (product.getStatus() != ProductStatus.DELETED) {
            if (productStock.isAvailable()) {
                product.active();
            } else {
                product.soldOut();
            }
        }

        eventPublisher.publishEventsFrom(productStock);
    }

    @Transactional
    public void deleted(Product product) {
        ProductStock productStock = getLockedByProductId(product.getId());

        if (product.getStatus() != ProductStatus.DELETED) {
            throw new DomainException("삭제된 상품이 아닙니다.");
        }

        productStock.adjust(0, "판매 중지");

        eventPublisher.publishEventsFrom(productStock);
    }

    private ProductStock getLockedByProductId(Long productId) {
        return productStockRepository.findLockedByProductId(productId)
                .orElseThrow(() -> new DomainException("재고가 존재하지 않습니다."));
    }
}
