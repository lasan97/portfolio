package com.portfolio.backend.service.product.dto;

import jakarta.validation.constraints.NotNull;

public class ProductStockServiceRequest {

    public record AdjustStock(
            @NotNull(message = "수량은 필수입니다.")
            Integer quantity,

            @NotNull(message = "재고 변경 사유는 필수입니다.")
            StockChangeReason reason,

            String memo
    ) {
    }
}
