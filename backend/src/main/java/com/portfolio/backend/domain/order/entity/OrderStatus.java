package com.portfolio.backend.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING_STOCK_REDUCTION("재고 확인 중"),
    PENDING_PAYMENT("결제 대기"),
    FAILED("주문 실패"),
    CANCELLED("주문 취소"),
    ORDERED("주문 완료");

    private final String description;

    public String getDescription() {
        return description;
    }
}
