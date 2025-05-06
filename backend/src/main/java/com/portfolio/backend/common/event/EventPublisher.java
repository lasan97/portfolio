package com.portfolio.backend.common.event;

import com.portfolio.backend.domain.common.entity.AggregateRoot;
import com.portfolio.backend.domain.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishDomainEventsFrom(AggregateRoot aggregate) {
        log.info("Domain Events Size: {}", aggregate.getDomainEvents().size());
        if (!aggregate.getDomainEvents().isEmpty()) {
            for (DomainEvent domainEvent : aggregate.getDomainEvents()) {
                log.info("Publishing event: {}", domainEvent.getClass());
                eventPublisher.publishEvent(domainEvent);
            }
            aggregate.clearDomainEvents();
        }
    }

    public void publishServiceEvent(ServiceEvent event) {
        log.info("Publishing event: {}", event.getClass());
        eventPublisher.publishEvent(event);
    }

    public void publishEvent(Object event) {
        log.info("Publishing event: {}", event.getClass());
        eventPublisher.publishEvent(event);
    }
}
