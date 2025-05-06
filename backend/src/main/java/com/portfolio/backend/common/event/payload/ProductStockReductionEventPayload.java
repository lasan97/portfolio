package com.portfolio.backend.common.event.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockReductionEventPayload {

    private List<OrderItem> orderItems;

    public record OrderItem(
            @JsonProperty
            Long productId,

            @JsonProperty
            Integer quantity
    ){}
}
