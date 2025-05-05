package com.portfolio.backend.domain.order.value;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProductTest {

    @DisplayName("상품 정보가 정상적으로 복사된다")
    @Test
    void createOrderProduct_Success() {
        // given
        Product product = ProductTestFixtures.createDefaultProduct();

        // when
        OrderProduct orderProduct = new OrderProduct(product);

        // then
        assertEquals(orderProduct.getId(), product.getId());
        assertEquals(orderProduct.getName(), product.getName());
        assertEquals(orderProduct.getOriginalPrice(), product.getOriginalPrice());
        assertEquals(orderProduct.getPrice(), product.getPrice());
        assertEquals(orderProduct.getThumbnailImageUrl(), product.getThumbnailImageUrl());
    }

    @DisplayName("상품이 null인 경우 예외가 발생한다")
    @Test
    void createOrderProduct_WithNullProduct_ThrowsException() {
        // when & then
        assertThrows(DomainException.class, () -> {
            new OrderProduct(null);
        });
    }
}
