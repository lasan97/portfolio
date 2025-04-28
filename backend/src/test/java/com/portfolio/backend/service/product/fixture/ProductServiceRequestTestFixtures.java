package com.portfolio.backend.service.product.fixture;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.StockChangeReason;

import java.math.BigDecimal;

public class ProductServiceRequestTestFixtures {

    public static ProductServiceRequest.Create createProductCreateRequest() {
        return new ProductServiceRequest.Create(
                "맥북 프로",
                new Money(BigDecimal.valueOf(2000000)),
                new Money(BigDecimal.valueOf(1800000)),
                "Apple 맥북 프로 M3 칩",
                "macbook-pro.jpg",
                ProductCategory.ELECTRONICS,
                10
        );
    }

    public static ProductServiceRequest.Update createProductUpdateRequest() {
        return new ProductServiceRequest.Update(
                "LG 그램그램",
                new Money(BigDecimal.valueOf(2100000)),
                new Money(BigDecimal.valueOf(1900000)),
                "LG 그램 프로 슈퍼울트라 칩",
                "macbook-pro-m3.jpg",
                ProductCategory.ELECTRONICS
        );
    }

    public static ProductServiceRequest.AdjustStock createStockAdjustRequest(int quantity, StockChangeReason reason, String description) {
        return new ProductServiceRequest.AdjustStock(quantity, reason, description);
    }

    public static ProductServiceRequest.AdjustStock createStockAdjustRequest() {
        return createStockAdjustRequest(10, StockChangeReason.ADJUSTMENT, "재고 조정 테스트");
    }

    public static ProductServiceRequest.AdjustStock createStockAdjustRequest(int quantity) {
        return createStockAdjustRequest(quantity, StockChangeReason.ADJUSTMENT, "재고 조정 테스트");
    }
}
