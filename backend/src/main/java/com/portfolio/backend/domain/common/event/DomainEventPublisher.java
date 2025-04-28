package com.portfolio.backend.domain.common.event;

import com.portfolio.backend.domain.common.entity.AggregateRoot;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishEventsFrom(AggregateRoot aggregate) {
        if (!aggregate.getDomainEvents().isEmpty()) {
            aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
            aggregate.clearDomainEvents();
        }
    }
}
