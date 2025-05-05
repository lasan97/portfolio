package com.portfolio.backend.domain.cart.entity;

import com.portfolio.backend.domain.cart.value.ProductCartItem;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.fixture.UserTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProductCartTest {

    private User user;
    private Product product1;
    private Product product2;
    private ProductCart productCart;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 사용자와 상품 준비
        user = UserTestFixtures.createUser();
        product1 = ProductTestFixtures.createDefaultProduct(10);
        product2 = ProductTestFixtures.createDefaultProduct(10);
        
        // 장바구니 생성
        productCart = new ProductCart(user);
    }

    @Nested
    @DisplayName("장바구니 생성")
    class CartCreation {
        
        @Test
        @DisplayName("장바구니 생성 시 사용자가 연결되어야 한다")
        void shouldLinkUserWhenCreated() {
            // Given - 장바구니가 생성된 상황(setUp)
            
            // When
            User cartUser = productCart.getUser();
            
            // Then
            assertThat(cartUser).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 관리")
    class CartItemManagement {
        
        @Test
        @DisplayName("장바구니에 아이템을 추가할 수 있어야 한다")
        void shouldAddItemToCart() {
            // When
            productCart.addItem(product1, 2);

            // Then
            List<ProductCartItem> items = productCart.getItems();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("장바구니에 동일한 상품을 다른 수량으로 추가하면 기존 항목이 새 항목으로 대체되어야 한다")
        void shouldReplaceExistingItemWhenAddingSameProductWithDifferentQuantity() {
            // Given
            productCart.addItem(product1, 2);

            // When
            productCart.addItem(product1, 5);

            // Then
            List<ProductCartItem> items = productCart.getItems();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("장바구니에서 아이템을 제거할 수 있어야 한다")
        void shouldRemoveItemFromCart() {
            // Given
            productCart.addItem(product1, 2);
            productCart.addItem(product2, 3);

            // When
            productCart.removeItem(product1);

            // Then
            List<ProductCartItem> items = productCart.getItems();
            assertThat(items).hasSize(1)
                    .extracting("product")
                    .contains(product2);
        }

        @Test
        @DisplayName("장바구니에 없는 아이템을 제거하려고 해도 에러가 발생하지 않아야 한다")
        void shouldNotThrowErrorWhenRemovingNonExistentItem() {
            // When & Then
            assertDoesNotThrow(() -> productCart.removeItem(product1));
        }
    }
    
    @Nested
    @DisplayName("장바구니 상태 조회")
    class CartStateRetrieval {
        
        @Test
        @DisplayName("장바구니 아이템 목록은 수정할 수 없어야 한다")
        void shouldReturnUnmodifiableList() {
            // Given
            productCart.addItem(product1, 2);

            // When
            List<ProductCartItem> items = productCart.getItems();

            // Then
            assertThatThrownBy(() -> items.add(new ProductCartItem(product2, 3)))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}