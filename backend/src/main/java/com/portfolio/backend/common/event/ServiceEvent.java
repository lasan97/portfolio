package com.portfolio.backend.common.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class ServiceEvent {

    private final UUID id;

    public ServiceEvent(UUID id) {
        this.id = id;
    }
}
