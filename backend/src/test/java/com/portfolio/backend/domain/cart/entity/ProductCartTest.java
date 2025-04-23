package com.portfolio.backend.domain.cart.entity;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * ProductCart 엔티티에 대한 단위 테스트
 */
@DisplayName("ProductCart 엔티티 테스트")
class ProductCartTest {

    private User user;
    private Product product1;
    private Product product2;
    private ProductCart productCart;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 사용자와 상품 준비
        user = TestFixtures.createRegularUser();
        product1 = TestFixtures.createDefaultProduct();
        product2 = TestFixtures.createSecondProduct();
        
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
            // Given
            ProductCartItem item = new ProductCartItem(product1, 2);

            // When
            productCart.addItem(item);

            // Then
            List<ProductCartItem> items = productCart.getItems();
            assertThat(items).hasSize(1)
                            .contains(item);
            assertThat(items.get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("장바구니에 동일한 상품을 다른 수량으로 추가하면 기존 항목이 새 항목으로 대체되어야 한다")
        void shouldReplaceExistingItemWhenAddingSameProductWithDifferentQuantity() {
            // Given
            ProductCartItem item1 = new ProductCartItem(product1, 2);
            ProductCartItem item2 = new ProductCartItem(product1, 5);
            productCart.addItem(item1);

            // When
            productCart.addItem(item2);

            // Then
            List<ProductCartItem> items = productCart.getItems();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("장바구니에서 아이템을 제거할 수 있어야 한다")
        void shouldRemoveItemFromCart() {
            // Given
            ProductCartItem item1 = new ProductCartItem(product1, 2);
            ProductCartItem item2 = new ProductCartItem(product2, 3);
            productCart.addItem(item1);
            productCart.addItem(item2);

            // When
            productCart.removeItem(item1);

            // Then
            List<ProductCartItem> items = productCart.getItems();
            assertThat(items).hasSize(1)
                            .doesNotContain(item1)
                            .contains(item2);
        }

        @Test
        @DisplayName("장바구니에 없는 아이템을 제거하려고 해도 에러가 발생하지 않아야 한다")
        void shouldNotThrowErrorWhenRemovingNonExistentItem() {
            // Given
            ProductCartItem item = new ProductCartItem(product1, 2);

            // When & Then
            assertDoesNotThrow(() -> productCart.removeItem(item));
        }
    }
    
    @Nested
    @DisplayName("장바구니 상태 조회")
    class CartStateRetrieval {
        
        @Test
        @DisplayName("장바구니 아이템 목록은 수정할 수 없어야 한다")
        void shouldReturnUnmodifiableList() {
            // Given
            ProductCartItem item = new ProductCartItem(product1, 2);
            productCart.addItem(item);

            // When
            List<ProductCartItem> items = productCart.getItems();

            // Then
            assertThatThrownBy(() -> items.add(new ProductCartItem(product2, 3)))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}