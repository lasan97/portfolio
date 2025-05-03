package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductStockHistoryTest {

    @Test
    @DisplayName("재고 변경 히스토리를 생성할 수 있어야 한다")
    void createStockHistory() {
        // given
        Product product = ProductTestFixtures.createDefaultProduct(10);
        int previousQuantity = 100;
        int changedQuantity = -10;
        StockChangeReason reason = StockChangeReason.SALE;
        String memo = "테스트 주문으로 인한 재고 감소";

        // when
        ProductStockHistory history = new ProductStockHistory(product, previousQuantity, changedQuantity, reason, memo);

        // then
        assertThat(history.getProduct()).isEqualTo(product);
        assertThat(history.getPreviousQuantity()).isEqualTo(previousQuantity);
        assertThat(history.getChangedQuantity()).isEqualTo(changedQuantity);
        assertThat(history.getCurrentQuantity()).isEqualTo(previousQuantity + changedQuantity);
        assertThat(history.getReason()).isEqualTo(reason);
        assertThat(history.getMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("재고증가 히스토리를 생성할 수 있어야 한다")
    void createIncreaseStockHistory() {
        // given
        Product product = ProductTestFixtures.createDefaultProduct(10);
        int previousQuantity = 100;
        int changedQuantity = 50;
        StockChangeReason reason = StockChangeReason.SALE;
        String memo = "재입고";

        // when
        ProductStockHistory history = new ProductStockHistory(product, previousQuantity, changedQuantity, reason, memo);

        // then
        assertThat(history.getProduct()).isEqualTo(product);
        assertThat(history.getPreviousQuantity()).isEqualTo(previousQuantity);
        assertThat(history.getChangedQuantity()).isEqualTo(changedQuantity);
        assertThat(history.getCurrentQuantity()).isEqualTo(previousQuantity + changedQuantity);
        assertThat(history.getReason()).isEqualTo(reason);
        assertThat(history.getMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("재고감소 히스토리를 생성할 수 있어야 한다")
    void createDecreaseStockHistory() {
        // given
        Product product = ProductTestFixtures.createDefaultProduct(10);
        int previousQuantity = 100;
        int changedQuantity = -20;
        StockChangeReason reason = StockChangeReason.SALE;
        String memo = "판매";

        // when
        ProductStockHistory history = new ProductStockHistory(product, previousQuantity, changedQuantity, reason, memo);

        // then
        assertThat(history.getProduct()).isEqualTo(product);
        assertThat(history.getPreviousQuantity()).isEqualTo(previousQuantity);
        assertThat(history.getChangedQuantity()).isEqualTo(changedQuantity);
        assertThat(history.getCurrentQuantity()).isEqualTo(previousQuantity + changedQuantity);
        assertThat(history.getReason()).isEqualTo(reason);
        assertThat(history.getMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("메모 없이 재고 히스토리를 생성할 수 있어야 한다")
    void createStockHistoryWithoutMemo() {
        // given
        Product product = ProductTestFixtures.createDefaultProduct(10);
        int previousQuantity = 100;
        int changedQuantity = -10;
        StockChangeReason reason = StockChangeReason.ADJUSTMENT;
        String memo = null;

        // when
        ProductStockHistory history = new ProductStockHistory(product, previousQuantity, changedQuantity, reason, memo);

        // then
        assertThat(history.getProduct()).isEqualTo(product);
        assertThat(history.getPreviousQuantity()).isEqualTo(previousQuantity);
        assertThat(history.getChangedQuantity()).isEqualTo(changedQuantity);
        assertThat(history.getCurrentQuantity()).isEqualTo(previousQuantity + changedQuantity);
        assertThat(history.getReason()).isEqualTo(reason);
        assertThat(history.getMemo()).isEqualTo(memo);
    }
}