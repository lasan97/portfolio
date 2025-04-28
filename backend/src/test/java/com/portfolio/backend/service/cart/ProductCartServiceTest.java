package com.portfolio.backend.service.cart;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockHistoryRepository;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest;
import com.portfolio.backend.service.cart.dto.ProductCartServiceResponse;
import com.portfolio.backend.service.cart.fixture.ProductCartServiceRequestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductCartService 테스트")
class ProductCartServiceTest extends ServiceTest {

    @Autowired
    private ProductCartRepository productCartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductStockHistoryRepository productStockHistoryRepository;


    @Autowired
    private ProductCartService productCartService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));
    }

    @AfterEach
    void tearDown() {
        productCartRepository.deleteAll();
        productStockHistoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("장바구니 아이템 조회")
    class GetCartItems {

        @Test
        @DisplayName("장바구니가 존재하면 아이템 목록을 반환해야 한다")
        void shouldReturnCartItemsWhenCartExists() {
            // Given
            int quantity = 2;
            ProductCart productCart = new ProductCart(user);
            productCart.addItem(product, quantity);
            productCartRepository.save(productCart);

            // When
            List<ProductCartServiceResponse.Get> result = productCartService.getCartItems(user.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).product().id()).isEqualTo(product.getId());
            assertThat(result.get(0).quantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 빈 목록을 반환해야 한다")
        void shouldCreateNewCartAndReturnEmptyListWhenCartNotExists() {
            // When
            List<ProductCartServiceResponse.Get> result = productCartService.getCartItems(user.getId());

            // Then
            assertThat(result).isEmpty();

            Optional<ProductCart> createdCart = productCartRepository.findByUserId(user.getId());
            assertThat(createdCart).isPresent();
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            Long userId = 0L;

            // When & Then
            assertThatThrownBy(() -> productCartService.getCartItems(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("해당 사용자가 존재하지 않습니다");
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 추가")
    class AddCartItem {

        @Test
        @DisplayName("장바구니가 존재하면 아이템을 추가해야 한다")
        void shouldAddItemWhenCartExists() {
            // Given
            ProductCart productCart = new ProductCart(user);
            productCartRepository.save(productCart);

            int quantity = 2;

            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures
                    .createAddItemRequest(product.getId(), quantity);

            // When
            productCartService.addCartItem(request, user.getId());

            // Then
            ProductCart response = productCartRepository.findByUserId(user.getId()).orElseThrow();
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getProduct().getId()).isEqualTo(product.getId());
            assertThat(response.getItems().get(0).getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 아이템을 추가해야 한다")
        void shouldCreateCartAndAddItemWhenCartNotExists() {
            // Given
            int quantity = 2;

            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures
                    .createAddItemRequest(product.getId(), quantity);

            // When
            productCartService.addCartItem(request, user.getId());

            // Then
            ProductCart response = productCartRepository.findByUserId(user.getId()).orElseThrow();
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getProduct().getId()).isEqualTo(product.getId());
            assertThat(response.getItems().get(0).getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            Long userId = 0L;
            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures
                    .createAddItemRequest(product.getId(), 2);

            // When & Then
            assertThatThrownBy(() -> productCartService.addCartItem(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("해당 사용자가 존재하지 않습니다");
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            Long productId = 0L;
            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures
                    .createAddItemRequest(productId, 2);

            // When & Then
            assertThatThrownBy(() -> productCartService.addCartItem(request, user.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(productId.toString());
        }
    }

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("장바구니 아이템 제거")
    class RemoveCartItem {

        @Test
        @DisplayName("장바구니가 존재하면 아이템을 제거해야 한다")
        void shouldRemoveItemWhenCartExists() {
            // Given
            Product product2 = productRepository.save(ProductTestFixtures.createDefaultProduct(10));
            ProductCart productCart = new ProductCart(user);
            productCart.addItem(product, 2);
            productCart.addItem(product2, 1);
            productCartRepository.save(productCart);

            ProductCartServiceRequest.RemoveItem request = ProductCartServiceRequestFixtures
                    .createRemoveItemRequest(product.getId());

            // When
            productCartService.removeCartItem(request, user.getId());

            // Then
            ProductCart response = productCartRepository.findByUserId(user.getId()).orElseThrow();
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getProduct().getId()).isEqualTo(product2.getId());
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            Long userId = 0L;
            ProductCartServiceRequest.RemoveItem request = ProductCartServiceRequestFixtures.createRemoveItemRequest(product.getId());

            // When & Then
            assertThatThrownBy(() -> productCartService.removeCartItem(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("해당 사용자가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            Long productId = 0L;
            ProductCartServiceRequest.RemoveItem request = ProductCartServiceRequestFixtures
                    .createRemoveItemRequest(productId);

            // When & Then
            assertThatThrownBy(() -> productCartService.removeCartItem(request, user.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
        }
    }
}