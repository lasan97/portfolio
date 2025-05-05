package com.portfolio.backend.domain.order.event;

import com.portfolio.backend.domain.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderCreatedDomainEvent extends DomainEvent {

    public OrderCreatedDomainEvent(LocalDateTime transactionDateTime) {
        super(transactionDateTime);
    }
}
