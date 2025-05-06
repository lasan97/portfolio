package com.portfolio.backend.service.order.outbox.event;

import com.portfolio.backend.common.event.ProductStockStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductStockReductionResponseEvent {

    private UUID sagaId;

    private ProductStockStatus productStockStatus;
}
