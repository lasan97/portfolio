package com.portfolio.backend.domain.user.event;

import com.portfolio.backend.domain.common.event.DomainEvent;
import com.portfolio.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCreatedEvent extends DomainEvent {

    private final User user;

    @Builder
    public UserCreatedEvent(User user, LocalDateTime transactionDateTime) {
        super(transactionDateTime);
        this.user = user;
    }
}
