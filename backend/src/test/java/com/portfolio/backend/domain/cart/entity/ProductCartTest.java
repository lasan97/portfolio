package com.portfolio.backend.domain.cart.entity;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductCartTest {

    private User user;
    private Product product1;
    private Product product2;
    private ProductCart productCart;

    @BeforeEach
    void setUp() {
        // Given: 테스트에 필요한 사용자와 상품 준비
        user = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .provider(Oauth2ProviderType.GITHUB)
                .providerId("github-123")
                .profileImageUrl("https://example.com/profile.jpg")
                .role(RoleType.USER)
                .build();

        // ID 설정 (리플렉션 사용)
        setUserId(user, 1L);

        // 테스트용 상품 생성
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

        // 장바구니 생성
        productCart = new ProductCart(user);
    }

    @Test
    @DisplayName("장바구니 생성 시 사용자가 연결되어야 한다")
    void shouldLinkUserWhenCreated() {
        // Given: 장바구니가 생성된 상황
        
        // When: 사용자를 조회하면
        User cartUser = productCart.getUser();
        
        // Then: 설정한 사용자와 동일해야 함
        assertEquals(user, cartUser);
    }

    @Test
    @DisplayName("장바구니에 아이템을 추가할 수 있어야 한다")
    void shouldAddItemToCart() {
        // Given: 장바구니 아이템이 준비된 상황
        ProductCartItem item = new ProductCartItem(product1, 2);

        // When: 장바구니에 아이템을 추가하면
        productCart.addItem(item);

        // Then: 장바구니에 아이템이 포함되어야 함
        List<ProductCartItem> items = productCart.getItems();
        assertEquals(1, items.size());
        assertTrue(items.contains(item));
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    @DisplayName("장바구니에 동일한 상품을 다른 수량으로 추가하면 기존 항목을 새 항목으로 대체해야 한다")
    void shouldReplaceExistingItemWhenAddingSameProductWithDifferentQuantity() {
        // Given: 장바구니에 아이템이 이미 있는 상황
        ProductCartItem item1 = new ProductCartItem(product1, 2);
        ProductCartItem item2 = new ProductCartItem(product1, 5);
        productCart.addItem(item1);

        // When: 같은 상품을 다른 수량으로 추가하면
        productCart.addItem(item2);

        // Then: 기존 아이템이 새 수량으로 대체되어야 함
        List<ProductCartItem> items = productCart.getItems();
        assertEquals(1, items.size());
        assertEquals(5, items.get(0).getQuantity());
    }

    @Test
    @DisplayName("장바구니에서 아이템을 제거할 수 있어야 한다")
    void shouldRemoveItemFromCart() {
        // Given: 장바구니에 여러 아이템이 있는 상황
        ProductCartItem item1 = new ProductCartItem(product1, 2);
        ProductCartItem item2 = new ProductCartItem(product2, 3);
        productCart.addItem(item1);
        productCart.addItem(item2);

        // When: 아이템을 제거하면
        productCart.removeItem(item1);

        // Then: 해당 아이템만 제거되어야 함
        List<ProductCartItem> items = productCart.getItems();
        assertEquals(1, items.size());
        assertFalse(items.contains(item1));
        assertTrue(items.contains(item2));
    }

    @Test
    @DisplayName("장바구니에 없는 아이템을 제거하려고 해도 에러가 발생하지 않아야 한다")
    void shouldNotThrowErrorWhenRemovingNonExistentItem() {
        // Given: 빈 장바구니와 제거할 아이템
        ProductCartItem item = new ProductCartItem(product1, 2);

        // When & Then: 예외 없이 제거 가능해야 함
        assertDoesNotThrow(() -> productCart.removeItem(item));
    }

    @Test
    @DisplayName("장바구니 아이템 목록은 수정할 수 없어야 한다")
    void shouldReturnUnmodifiableList() {
        // Given: 장바구니에 아이템이 있는 상황
        ProductCartItem item = new ProductCartItem(product1, 2);
        productCart.addItem(item);

        // When: 아이템 목록을 가져오면
        List<ProductCartItem> items = productCart.getItems();

        // Then: 목록을 수정하려고 하면 예외가 발생해야 함
        assertThrows(UnsupportedOperationException.class, () -> items.add(new ProductCartItem(product2, 3)));
    }

    // Helper method to set User ID
    private void setUserId(User user, Long id) {
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }
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