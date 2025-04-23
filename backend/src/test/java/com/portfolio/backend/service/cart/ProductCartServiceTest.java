package com.portfolio.backend.service.cart;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.entity.ProductCartItem;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.cart.dto.ProductCartServiceMapper;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest;
import com.portfolio.backend.service.cart.dto.ProductCartServiceResponse;
import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductCartService에 대한 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCartService 테스트")
class ProductCartServiceTest {

    @Mock
    private ProductCartRepository productCartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCartServiceMapper mapper;

    @InjectMocks
    private ProductCartService productCartService;

    // 테스트에 사용할 기본 객체들
    private User user;
    private UserImpl userImpl;
    private Product product;
    private ProductCartServiceResponse.Get cartResponse;

    @BeforeEach
    void setUp() {
        // 기본 테스트 데이터 설정
        user = TestFixtures.createRegularUser();
        userImpl = new UserImpl(TestFixtures.USER_ID_1, "test@example.com", RoleType.USER);
        product = TestFixtures.createDefaultProduct();

        ServiceBaseResponse.SimpleProduct simpleProduct = new ServiceBaseResponse.SimpleProduct(
                TestFixtures.PRODUCT_ID_1,
                product.getName(),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(10000),
                ProductStatus.ACTIVE
        );

        cartResponse = new ProductCartServiceResponse.Get(simpleProduct, 2);
    }

    @Nested
    @DisplayName("장바구니 아이템 조회")
    class GetCartItems {

        @Test
        @DisplayName("장바구니가 존재하면 아이템 목록을 반환해야 한다")
        void shouldReturnCartItemsWhenCartExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartItem cartItem = new ProductCartItem(product, 2);
            List<ProductCartItem> cartItems = List.of(cartItem);
            List<ProductCartServiceResponse.Get> expectedResponses = List.of(cartResponse);

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productCartRepository.findByUserId(TestFixtures.USER_ID_1)).thenReturn(Optional.of(productCart));
            when(productCart.getItems()).thenReturn(cartItems);
            when(mapper.toGet(cartItem)).thenReturn(cartResponse);

            // When
            List<ProductCartServiceResponse.Get> result = productCartService.getCartItems(userImpl);

            // Then
            assertThat(result)
                .hasSize(expectedResponses.size())
                .isEqualTo(expectedResponses);

            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productCartRepository).findByUserId(TestFixtures.USER_ID_1);
            verify(productCart).getItems();
            verify(mapper).toGet(cartItem);
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 빈 목록을 반환해야 한다")
        void shouldCreateNewCartAndReturnEmptyListWhenCartNotExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productCartRepository.findByUserId(TestFixtures.USER_ID_1)).thenReturn(Optional.empty());
            when(productCartRepository.save(any(ProductCart.class))).thenReturn(productCart);
            when(productCart.getItems()).thenReturn(new ArrayList<>());

            // When
            List<ProductCartServiceResponse.Get> result = productCartService.getCartItems(userImpl);

            // Then
            assertThat(result).isEmpty();

            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productCartRepository).findByUserId(TestFixtures.USER_ID_1);
            verify(productCartRepository).save(any(ProductCart.class));
            verify(productCart).getItems();
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productCartService.getCartItems(userImpl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("해당 사용자가 존재하지 않습니다.");

            verify(userRepository).findById(TestFixtures.USER_ID_1);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 추가")
    class AddCartItem {

        @Test
        @DisplayName("장바구니가 존재하면 아이템을 추가해야 한다")
        void shouldAddItemWhenCartExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(
                TestFixtures.PRODUCT_ID_1, 2
            );

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndStatusNot(TestFixtures.PRODUCT_ID_1, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(TestFixtures.USER_ID_1)).thenReturn(Optional.of(productCart));

            // When
            productCartService.addCartItem(request, userImpl);

            // Then
            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productRepository).findByIdAndStatusNot(TestFixtures.PRODUCT_ID_1, ProductStatus.DELETED);
            verify(productCartRepository).findByUserId(TestFixtures.USER_ID_1);

            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).addItem(cartItemCaptor.capture());

            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertThat(capturedItem.getProduct()).isEqualTo(product);
            assertThat(capturedItem.getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 아이템을 추가해야 한다")
        void shouldCreateCartAndAddItemWhenCartNotExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(
                TestFixtures.PRODUCT_ID_1, 2
            );

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndStatusNot(TestFixtures.PRODUCT_ID_1, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(TestFixtures.USER_ID_1)).thenReturn(Optional.empty());
            when(productCartRepository.save(any(ProductCart.class))).thenReturn(productCart);

            // When
            productCartService.addCartItem(request, userImpl);

            // Then
            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productRepository).findByIdAndStatusNot(TestFixtures.PRODUCT_ID_1, ProductStatus.DELETED);
            verify(productCartRepository).findByUserId(TestFixtures.USER_ID_1);
            verify(productCartRepository).save(any(ProductCart.class));

            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).addItem(cartItemCaptor.capture());

            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertThat(capturedItem.getProduct()).isEqualTo(product);
            assertThat(capturedItem.getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(
                TestFixtures.PRODUCT_ID_1, 2
            );
            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productCartService.addCartItem(request, userImpl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("해당 사용자가 존재하지 않습니다.");

            verify(userRepository).findById(TestFixtures.USER_ID_1);
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(
                TestFixtures.PRODUCT_ID_1, 2
            );

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndStatusNot(TestFixtures.PRODUCT_ID_1, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productCartService.addCartItem(request, userImpl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(TestFixtures.PRODUCT_ID_1.toString());

            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productRepository).findByIdAndStatusNot(TestFixtures.PRODUCT_ID_1, ProductStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 제거")
    class RemoveCartItem {

        @Test
        @DisplayName("장바구니가 존재하면 아이템을 제거해야 한다")
        void shouldRemoveItemWhenCartExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(
                TestFixtures.PRODUCT_ID_1
            );

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productRepository.findById(TestFixtures.PRODUCT_ID_1)).thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(TestFixtures.USER_ID_1)).thenReturn(Optional.of(productCart));

            // When
            productCartService.removeCartItem(request, userImpl);

            // Then
            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productRepository).findById(TestFixtures.PRODUCT_ID_1);
            verify(productCartRepository).findByUserId(TestFixtures.USER_ID_1);

            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).removeItem(cartItemCaptor.capture());

            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertThat(capturedItem.getProduct()).isEqualTo(product);
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 아이템 제거를 시도해야 한다")
        void shouldCreateCartAndTryToRemoveItemWhenCartNotExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(
                TestFixtures.PRODUCT_ID_1
            );

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productRepository.findById(TestFixtures.PRODUCT_ID_1)).thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(TestFixtures.USER_ID_1)).thenReturn(Optional.empty());
            when(productCartRepository.save(any(ProductCart.class))).thenReturn(productCart);

            // When
            productCartService.removeCartItem(request, userImpl);

            // Then
            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productRepository).findById(TestFixtures.PRODUCT_ID_1);
            verify(productCartRepository).findByUserId(TestFixtures.USER_ID_1);
            verify(productCartRepository).save(any(ProductCart.class));

            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).removeItem(cartItemCaptor.capture());

            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertThat(capturedItem.getProduct()).isEqualTo(product);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(
                TestFixtures.PRODUCT_ID_1
            );
            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productCartService.removeCartItem(request, userImpl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("해당 사용자가 존재하지 않습니다.");

            verify(userRepository).findById(TestFixtures.USER_ID_1);
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(
                TestFixtures.PRODUCT_ID_1
            );

            when(userRepository.findById(TestFixtures.USER_ID_1)).thenReturn(Optional.of(user));
            when(productRepository.findById(TestFixtures.PRODUCT_ID_1)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productCartService.removeCartItem(request, userImpl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(TestFixtures.PRODUCT_ID_1.toString());

            verify(userRepository).findById(TestFixtures.USER_ID_1);
            verify(productRepository).findById(TestFixtures.PRODUCT_ID_1);
        }
    }
}