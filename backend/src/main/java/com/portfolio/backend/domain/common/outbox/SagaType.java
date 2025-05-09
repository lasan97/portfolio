package com.portfolio.backend.domain.common.outbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SagaType {

    ORDER("주문"),
    ORDER_CANCELING("주문 취소");

    private final String description;
}
