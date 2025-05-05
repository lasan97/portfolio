package com.portfolio.backend.domain.cart.value;

import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCartItemTest {

    private Product product1;
    private Product product2;
    private ProductCartItem cartItem1;
    private ProductCartItem cartItem2;
    private ProductCartItem cartItem3;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 상품 및 장바구니 아이템 준비
        product1 = ProductTestFixtures.createDefaultProduct(10);
        product2 = ProductTestFixtures.createDefaultProduct(10);

        // 장바구니 아이템 생성
        cartItem1 = new ProductCartItem(product1, 2);
        cartItem2 = new ProductCartItem(product1, 5);
        cartItem3 = new ProductCartItem(product2, 3);
    }

    @Nested
    @DisplayName("장바구니 아이템 생성")
    class CartItemCreation {
        
        @Test
        @DisplayName("생성된 장바구니 아이템은 상품과 수량을 올바르게 저장해야 한다")
        void shouldStoreProductAndQuantityCorrectly() {
            // Given - 장바구니 아이템이 생성된 상황(setUp)
            
            // When
            Product itemProduct = cartItem1.getProduct();
            int itemQuantity = cartItem1.getQuantity();
            
            // Then
            assertThat(itemProduct).isEqualTo(product1);
            assertThat(itemQuantity).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 동등성 비교")
    class CartItemEquality {
        
        @Test
        @DisplayName("같은 상품을 가진 장바구니 아이템은 수량이 달라도 동등해야 한다")
        void shouldBeEqualWhenProductsAreSame() {
            // Given - 동일한 상품을 가진 두 장바구니 아이템(setUp)
            
            // When & Then
            assertThat(cartItem1).isEqualTo(cartItem2);
            assertThat(cartItem1.hashCode()).isEqualTo(cartItem2.hashCode());
        }

        @Test
        @DisplayName("다른 상품을 가진 장바구니 아이템은 동등하지 않아야 한다")
        void shouldNotBeEqualWhenProductsAreDifferent() {
            // Given - 다른 상품을 가진 두 장바구니 아이템(setUp)
            
            // When & Then
            assertThat(cartItem1).isNotEqualTo(cartItem3);
            assertThat(cartItem1.hashCode()).isNotEqualTo(cartItem3.hashCode());
        }

        @Test
        @DisplayName("null과 비교했을 때 동등하지 않아야 한다")
        void shouldNotBeEqualToNull() {
            // Given
            ProductCartItem nullItem = null;
            
            // When & Then
            assertThat(cartItem1).isNotEqualTo(nullItem);
        }

        @Test
        @DisplayName("다른 타입의 객체와 비교했을 때 동등하지 않아야 한다")
        void shouldNotBeEqualToDifferentType() {
            // Given
            String notCartItem = "not a cart item";
            
            // When & Then
            assertThat(cartItem1).isNotEqualTo(notCartItem);
        }

        @Test
        @DisplayName("자기 자신과 비교했을 때 동등해야 한다")
        void shouldBeEqualToItself() {
            // When & Then
            assertThat(cartItem1).isEqualTo(cartItem1);
        }
    }
}