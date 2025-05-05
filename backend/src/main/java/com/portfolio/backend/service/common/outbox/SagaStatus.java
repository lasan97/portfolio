package com.portfolio.backend.service.common.outbox;

public enum SagaStatus {
    STARTED, FAILED, SUCCEEDED, COMPENSATING, COMPENSATED
}
