package com.portfolio.backend.service.product.fixture;

import com.portfolio.backend.service.product.dto.ProductStockServiceRequest;
import com.portfolio.backend.service.product.dto.StockChangeReason;

public class ProductStockServiceRequestTestFixtures {

    public static ProductStockServiceRequest.AdjustStock createStockAdjustRequest(int quantity, StockChangeReason reason, String description) {
        return new ProductStockServiceRequest.AdjustStock(quantity, reason, description);
    }

    public static ProductStockServiceRequest.AdjustStock createStockAdjustRequest() {
        return createStockAdjustRequest(10, StockChangeReason.ADJUSTMENT, "재고 조정 테스트");
    }

    public static ProductStockServiceRequest.AdjustStock createStockAdjustRequest(int quantity) {
        return createStockAdjustRequest(quantity, StockChangeReason.ADJUSTMENT, "재고 조정 테스트");
    }
}
