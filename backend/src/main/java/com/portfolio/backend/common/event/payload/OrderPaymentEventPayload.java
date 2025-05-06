package com.portfolio.backend.common.event.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class OrderPaymentEventPayload {

    @JsonProperty
    private Long userId;

    @JsonProperty
    private BigDecimal price;
}
