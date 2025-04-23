package com.portfolio.backend.domain.common.event;

import java.time.LocalDateTime;

public abstract class DomainEvent {

    private final LocalDateTime transactionDateTime;

    public DomainEvent(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }
}
