package com.portfolio.backend.domain.cart.entity;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductCartItemTest {

    private Product product1;
    private Product product2;
    private ProductCartItem cartItem1;
    private ProductCartItem cartItem2;
    private ProductCartItem cartItem3;

    @BeforeEach
    void setUp() {
        // Given: 테스트에 필요한 상품 및 장바구니 아이템 준비
        product1 = Product.builder()
                .name("상품1")
                .originalPrice(new Money(BigDecimal.valueOf(10000)))
                .price(new Money(BigDecimal.valueOf(8000)))
                .description("상품1 설명")
                .thumbnailImageUrl("thumbnail1.jpg")
                .category(ProductCategory.ELECTRONICS)
                .stock(10)
                .build();

        product2 = Product.builder()
                .name("상품2")
                .originalPrice(new Money(BigDecimal.valueOf(20000)))
                .price(new Money(BigDecimal.valueOf(15000)))
                .description("상품2 설명")
                .thumbnailImageUrl("thumbnail2.jpg")
                .category(ProductCategory.CLOTHING)
                .stock(20)
                .build();

        // ID 설정 (리플렉션 사용)
        setProductId(product1, 1L);
        setProductId(product2, 2L);

        // 장바구니 아이템 생성
        cartItem1 = new ProductCartItem(product1, 2);
        cartItem2 = new ProductCartItem(product1, 5);
        cartItem3 = new ProductCartItem(product2, 3);
    }

    @Test
    @DisplayName("장바구니 아이템 생성 시 상품과 수량이 올바르게 저장되어야 한다")
    void shouldCreateCartItemWithProductAndQuantity() {
        // Given: 장바구니 아이템이 생성된 상황
        
        // When: 상품과 수량을 조회하면
        Product itemProduct = cartItem1.getProduct();
        int itemQuantity = cartItem1.getQuantity();
        
        // Then: 설정한 값과 동일해야 함
        assertEquals(product1, itemProduct);
        assertEquals(2, itemQuantity);
    }

    @Test
    @DisplayName("같은 상품을 가진 장바구니 아이템은 수량이 달라도 동등해야 한다")
    void shouldBeEqualWhenProductIsSame() {
        // Given: 동일한 상품을 가진 두 장바구니 아이템 (수량은 다름)
        
        // When: equals 메소드로 비교하면
        boolean areEqual = cartItem1.equals(cartItem2);
        boolean hashCodesEqual = cartItem1.hashCode() == cartItem2.hashCode();
        
        // Then: 동등해야 함
        assertTrue(areEqual);
        assertTrue(hashCodesEqual);
    }

    @Test
    @DisplayName("다른 상품을 가진 장바구니 아이템은 동등하지 않아야 한다")
    void shouldNotBeEqualWhenProductIsDifferent() {
        // Given: 다른 상품을 가진 두 장바구니 아이템
        
        // When: equals 메소드로 비교하면
        boolean areEqual = cartItem1.equals(cartItem3);
        boolean hashCodesEqual = cartItem1.hashCode() == cartItem3.hashCode();
        
        // Then: 동등하지 않아야 함
        assertFalse(areEqual);
        assertFalse(hashCodesEqual);
    }

    @Test
    @DisplayName("null과 비교했을 때 동등하지 않아야 한다")
    void shouldNotBeEqualToNull() {
        // Given: 장바구니 아이템과 null
        
        // When & Then: null과 동등하지 않아야 함
        assertNotEquals(null, cartItem1);
    }

    @Test
    @DisplayName("다른 타입의 객체와 비교했을 때 동등하지 않아야 한다")
    void shouldNotBeEqualToDifferentType() {
        // Given: 장바구니 아이템과 문자열
        String notCartItem = "not a cart item";
        
        // When & Then: 다른 타입과 동등하지 않아야 함
        assertNotEquals(cartItem1, notCartItem);
    }

    @Test
    @DisplayName("자기 자신과 비교했을 때 동등해야 한다")
    void shouldBeEqualToItself() {
        // Given: 장바구니 아이템
        
        // When & Then: 자기 자신과 동등해야 함
        assertEquals(cartItem1, cartItem1);
    }

    // Helper method to set Product ID
    private void setProductId(Product product, Long id) {
        try {
            java.lang.reflect.Field idField = Product.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set product ID", e);
        }
    }
}