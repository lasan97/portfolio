package com.portfolio.backend.service.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductCartServiceRequest {

    public record AddItem(
            @NotNull(message = "상품은 필수입니다.")
            Long productId,

            @NotNull(message = "수량은 필수입니다.")
            @Positive(message = "수량은 양수여야 합니다.")
            Integer quantity
    ) {}

    public record RemoveItem(
            @NotNull(message = "상품은 필수입니다.")
            Long productId
    ) {}
}
