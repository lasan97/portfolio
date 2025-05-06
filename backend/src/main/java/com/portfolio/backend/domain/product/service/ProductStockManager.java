package com.portfolio.backend.domain.product.service;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.entity.ProductStock;
import com.portfolio.backend.domain.product.entity.StockChangeReason;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockRepository;
import com.portfolio.backend.domain.product.service.dto.ProductStockItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductStockManager {

    private final ProductStockRepository productStockRepository;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void refund(Long productId, int quantity) {
        ProductStock productStock = getLockedByProductId(productId);
        Product product = productStock.getProduct();

        productStock.increase(quantity, StockChangeReason.RETURN);

        if (product.getStatus() == ProductStatus.SOLD_OUT && productStock.isAvailable()) {
            product.active();
        }

        eventPublisher.publishDomainEventsFrom(productStock);
    }

    @Transactional
    public void sale(Long productId, int quantity) {
        ProductStock productStock = getLockedByProductId(productId);
        Product product = productStock.getProduct();

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new DomainException("삭제된 상품은 재고를 변경할 수 없습니다.");
        }

        productStock.decrease(quantity, StockChangeReason.SALE);

        if (product.getStatus() == ProductStatus.ACTIVE && !productStock.isAvailable()) {
            product.soldOut();
        }

        eventPublisher.publishDomainEventsFrom(productStock);
    }

    @Transactional
    public void sale(List<ProductStockItemDto> productStockItems) {
        productStockItems.forEach(item ->
                sale(item.getProductId(), item.getQuantity()));
    }

    @Transactional
    public void adjust(Long productId, int quantity, String memo) {
        ProductStock productStock = getLockedByProductId(productId);
        Product product = productStock.getProduct();

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new DomainException("삭제된 상품은 재고를 변경할 수 없습니다.");
        }

        productStock.adjust(quantity, memo);

        if (product.getStatus() != ProductStatus.DELETED) {
            if (productStock.isAvailable()) {
                product.active();
            } else {
                product.soldOut();
            }
        }

        eventPublisher.publishDomainEventsFrom(productStock);
    }

    @Transactional
    public void deleted(Long productId) {
        ProductStock productStock = getLockedByProductId(productId);
        Product product = productStock.getProduct();

        if (product.getStatus() != ProductStatus.DELETED) {
            throw new DomainException("삭제된 상품이 아닙니다.");
        }

        productStock.adjust(0, "판매 중지");

        eventPublisher.publishDomainEventsFrom(productStock);
    }

    private ProductStock getLockedByProductId(Long productId) {
        return productStockRepository.findLockedByProductId(productId)
                .orElseThrow(() -> new DomainException("재고가 존재하지 않습니다."));
    }
}
