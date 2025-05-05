package com.portfolio.backend.domain.order.value;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OrderItemTest {

    @DisplayName("주문 항목이 정상적으로 생성된다")
    @Test
    void createOrderItem_Success() {
        // given
        Product mockProduct = Mockito.mock(Product.class);
        when(mockProduct.getId()).thenReturn(1L);
        when(mockProduct.getName()).thenReturn("테스트 상품");
        when(mockProduct.getOriginalPrice()).thenReturn(new Money(new BigDecimal("10000")));
        when(mockProduct.getPrice()).thenReturn(new Money(new BigDecimal("8000")));
        when(mockProduct.getThumbnailImageUrl()).thenReturn("http://example.com/image.jpg");

        // when
        OrderItem orderItem = new OrderItem(mockProduct, 2);

        // then
        assertNotNull(orderItem.getProduct());
        assertEquals(2, orderItem.getQuantity());
    }

    @DisplayName("상품이 null인 경우 예외가 발생한다")
    @Test
    void createOrderItem_WithNullProduct_ThrowsException() {
        // when & then
        assertThrows(DomainException.class, () -> {
            new OrderItem(null, 2);
        });
    }

    @DisplayName("수량이 null인 경우 예외가 발생한다")
    @Test
    void createOrderItem_WithNullQuantity_ThrowsException() {
        // given
        Product mockProduct = Mockito.mock(Product.class);
        when(mockProduct.getId()).thenReturn(1L);
        when(mockProduct.getName()).thenReturn("테스트 상품");
        when(mockProduct.getOriginalPrice()).thenReturn(new Money(new BigDecimal("10000")));
        when(mockProduct.getPrice()).thenReturn(new Money(new BigDecimal("8000")));
        when(mockProduct.getThumbnailImageUrl()).thenReturn("http://example.com/image.jpg");

        // when & then
        assertThrows(DomainException.class, () -> {
            new OrderItem(mockProduct, null);
        });
    }

    @DisplayName("수량이 음수인 경우 예외가 발생한다")
    @Test
    void createOrderItem_WithNegativeQuantity_ThrowsException() {
        // given
        Product mockProduct = Mockito.mock(Product.class);
        when(mockProduct.getId()).thenReturn(1L);
        when(mockProduct.getName()).thenReturn("테스트 상품");
        when(mockProduct.getOriginalPrice()).thenReturn(new Money(new BigDecimal("10000")));
        when(mockProduct.getPrice()).thenReturn(new Money(new BigDecimal("8000")));
        when(mockProduct.getThumbnailImageUrl()).thenReturn("http://example.com/image.jpg");

        // when & then
        assertThrows(DomainException.class, () -> {
            new OrderItem(mockProduct, -1);
        });
    }

    @DisplayName("총 가격이 정상적으로 계산된다")
    @Test
    void getTotalPrice_ReturnsCorrectValue() {
        // given
        Product mockProduct = Mockito.mock(Product.class);
        when(mockProduct.getId()).thenReturn(1L);
        when(mockProduct.getName()).thenReturn("테스트 상품");
        when(mockProduct.getOriginalPrice()).thenReturn(new Money(new BigDecimal("10000")));
        when(mockProduct.getPrice()).thenReturn(new Money(new BigDecimal("8000")));
        when(mockProduct.getThumbnailImageUrl()).thenReturn("http://example.com/image.jpg");

        OrderItem orderItem = new OrderItem(mockProduct, 2);

        // when
        Money totalPrice = orderItem.getTotalPrice();

        // then
        assertEquals(new Money(new BigDecimal("16000")), totalPrice);
    }
}
