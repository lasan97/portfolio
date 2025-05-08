package com.portfolio.backend.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("대기"),
    PAID("결제완료"),
    FAILED("주문 실패"),
    CANCELLED("주문 취소"),
    CANCELING("주문 취소 요청"),
    ORDERED("주문 완료");

    private final String description;
}
