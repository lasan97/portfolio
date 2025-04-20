package com.portfolio.backend.service.cart;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.entity.ProductCartItem;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        user = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .provider(Oauth2ProviderType.GITHUB)
                .providerId("github-1")
                .profileImageUrl("https://example.com/profile.jpg")
                .role(RoleType.USER)
                .build();
        setId(user, 1L);

        userImpl = new UserImpl(1L, "test@example.com", RoleType.USER);

        product = Product.builder()
                .name("테스트 상품")
                .originalPrice(new Money(BigDecimal.valueOf(10000)))
                .price(new Money(BigDecimal.valueOf(8000)))
                .description("테스트 상품 설명")
                .thumbnailImageUrl("test.jpg")
                .category(ProductCategory.ELECTRONICS)
                .stock(10)
                .build();
        setId(product, 1L);

        ServiceBaseResponse.SimpleProduct simpleProduct = new ServiceBaseResponse.SimpleProduct(
                1L, "테스트 상품", BigDecimal.valueOf(8000), BigDecimal.valueOf(10000), ProductStatus.ACTIVE
        );

        cartResponse = new ProductCartServiceResponse.Get(simpleProduct, 2);
    }

    @Nested
    @DisplayName("장바구니 아이템 조회")
    class GetCartItems {

        @Test
        @DisplayName("장바구니가 존재하면 아이템 목록을 반환한다")
        void shouldReturnCartItemsWhenCartExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartItem cartItem = new ProductCartItem(product, 2);
            List<ProductCartItem> cartItems = List.of(cartItem);
            List<ProductCartServiceResponse.Get> cartResponses = List.of(cartResponse);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productCartRepository.findByUserId(1L)).thenReturn(Optional.of(productCart));
            when(productCart.getItems()).thenReturn(cartItems);
            when(mapper.toGet(cartItem)).thenReturn(cartResponse);

            // When
            List<ProductCartServiceResponse.Get> result = productCartService.getCartItems(userImpl);

            // Then
            assertEquals(cartResponses.size(), result.size());
            assertEquals(cartResponses, result);
            verify(userRepository).findById(1L);
            verify(productCartRepository).findByUserId(1L);
            verify(productCart).getItems();
            verify(mapper).toGet(cartItem);
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 빈 목록을 반환한다")
        void shouldCreateNewCartAndReturnEmptyListWhenCartNotExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(productCartRepository.save(any(ProductCart.class))).thenReturn(productCart);
            when(productCart.getItems()).thenReturn(new ArrayList<>());

            // When
            List<ProductCartServiceResponse.Get> result = productCartService.getCartItems(userImpl);

            // Then
            assertTrue(result.isEmpty());
            verify(userRepository).findById(1L);
            verify(productCartRepository).findByUserId(1L);
            verify(productCartRepository).save(any(ProductCart.class));
            verify(productCart).getItems();
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productCartService.getCartItems(userImpl));
            verify(userRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 추가")
    class AddCartItem {

        @Test
        @DisplayName("장바구니가 존재하면 아이템을 추가한다")
        void shouldAddItemWhenCartExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(1L, 2);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED)).thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(1L)).thenReturn(Optional.of(productCart));
            
            // When
            productCartService.addCartItem(request, userImpl);
            
            // Then
            verify(userRepository).findById(1L);
            verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
            verify(productCartRepository).findByUserId(1L);
            
            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).addItem(cartItemCaptor.capture());
            
            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertEquals(product, capturedItem.getProduct());
            assertEquals(2, capturedItem.getQuantity());
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 아이템을 추가한다")
        void shouldCreateCartAndAddItemWhenCartNotExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(1L, 2);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED)).thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(productCartRepository.save(any(ProductCart.class))).thenReturn(productCart);
            
            // When
            productCartService.addCartItem(request, userImpl);
            
            // Then
            verify(userRepository).findById(1L);
            verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
            verify(productCartRepository).findByUserId(1L);
            verify(productCartRepository).save(any(ProductCart.class));
            
            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).addItem(cartItemCaptor.capture());
            
            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertEquals(product, capturedItem.getProduct());
            assertEquals(2, capturedItem.getQuantity());
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(1L, 2);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            
            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productCartService.addCartItem(request, userImpl));
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            ProductCartServiceRequest.AddItem request = new ProductCartServiceRequest.AddItem(1L, 2);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED)).thenReturn(Optional.empty());
            
            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productCartService.addCartItem(request, userImpl));
            verify(userRepository).findById(1L);
            verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 제거")
    class RemoveCartItem {

        @Test
        @DisplayName("장바구니가 존재하면 아이템을 제거한다")
        void shouldRemoveItemWhenCartExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(1L);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(1L)).thenReturn(Optional.of(productCart));
            
            // When
            productCartService.removeCartItem(request, userImpl);
            
            // Then
            verify(userRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(productCartRepository).findByUserId(1L);
            
            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).removeItem(cartItemCaptor.capture());
            
            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertEquals(product, capturedItem.getProduct());
        }

        @Test
        @DisplayName("장바구니가 없으면 새 장바구니를 생성하고 아이템 제거를 시도한다")
        void shouldCreateCartAndRemoveItemWhenCartNotExists() {
            // Given
            ProductCart productCart = mock(ProductCart.class);
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(1L);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(productCartRepository.save(any(ProductCart.class))).thenReturn(productCart);
            
            // When
            productCartService.removeCartItem(request, userImpl);
            
            // Then
            verify(userRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(productCartRepository).findByUserId(1L);
            verify(productCartRepository).save(any(ProductCart.class));
            
            ArgumentCaptor<ProductCartItem> cartItemCaptor = ArgumentCaptor.forClass(ProductCartItem.class);
            verify(productCart).removeItem(cartItemCaptor.capture());
            
            ProductCartItem capturedItem = cartItemCaptor.getValue();
            assertEquals(product, capturedItem.getProduct());
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            
            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productCartService.removeCartItem(request, userImpl));
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            ProductCartServiceRequest.RemoveItem request = new ProductCartServiceRequest.RemoveItem(1L);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.empty());
            
            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productCartService.removeCartItem(request, userImpl));
            verify(userRepository).findById(1L);
            verify(productRepository).findById(1L);
        }
    }

    // Helper method to set ID
    private <T> void setId(T entity, Long id) {
        try {
            Class<?> clazz = entity.getClass();
            java.lang.reflect.Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패: " + entity.getClass().getSimpleName(), e);
        }
    }
}