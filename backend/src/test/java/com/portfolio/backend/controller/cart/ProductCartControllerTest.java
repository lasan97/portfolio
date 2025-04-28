package com.portfolio.backend.controller.cart;

import com.portfolio.backend.controller.ControllerTest;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.fixture.ProductCartFixtures;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest;
import com.portfolio.backend.service.cart.dto.ProductCartServiceResponse;
import com.portfolio.backend.service.cart.fixture.ProductCartServiceRequestFixtures;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DisplayName("ProductCartController 테스트")
class ProductCartControllerTest extends ControllerTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCartRepository productCartRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));
    }

    @Nested
    @DisplayName("장바구니 목록 조회 API")
    class GetCartItems {

        @SneakyThrows
        ResultActions getCartItems() {
            return mockMvc.perform(get("/api/cart"))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 장바구니 목록을 성공적으로 조회할 수 있다")
        @WithUserDetails
        void shouldGetCartItemsWithAuthentication() throws Exception {
            // Given
            ProductCart cart = ProductCartFixtures.createProductCart(user);
            cart.addItem(product, 1);
            cart = productCartRepository.save(cart);

            // When
            ResultActions resultActions = getCartItems();

            // Then
            resultActions
                    .andExpect(status().isOk());

            List<ProductCartServiceResponse.Get> response = resultActionsToList(resultActions, ProductCartServiceResponse.Get.class);

            assertThat(response).hasSize(1)
                    .extracting("product.id", "product.name", "quantity")
                    .containsExactly(tuple(product.getId(), product.getName(), 1));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 장바구니 목록 조회 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldDenyGetCartItemsWithoutAuthentication() throws Exception {
            // When & Then
            getCartItems()
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 추가 API")
    class AddCartItem {

        @SneakyThrows
        ResultActions addCartItem(ProductCartServiceRequest.AddItem request) {
            return mockMvc.perform(post("/api/cart")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 장바구니에 아이템을 성공적으로 추가할 수 있다")
        @WithUserDetails
        void shouldAddCartItemWithAuthentication() throws Exception {
            // Given
            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures.createAddItemRequest(product.getId(), 1);

            // When
            ResultActions resultActions = addCartItem(request);

            // Then
            resultActions
                    .andExpect(status().isOk());

            ProductCart response = productCartRepository.findByUserId(user.getId()).get();

            assertThat(response.getItems()).hasSize(1)
                    .extracting("product.id")
                    .containsExactly(product.getId());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 장바구니에 아이템 추가 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldDenyAddCartItemWithoutAuthentication() throws Exception {
            // Given
            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures.createAddItemRequest(product.getId(), 1);

            // When & Then
            addCartItem(request)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("유효하지 않은 요청으로 장바구니에 아이템 추가 시 검증 오류가 발생한다")
        @WithUserDetails
        void shouldValidateAddCartItemRequest() throws Exception {
            // Given
            ProductCartServiceRequest.AddItem request = ProductCartServiceRequestFixtures.createAddItemRequest(product.getId(), -1);

            // When & Then
            addCartItem(request)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 제거 API")
    class RemoveCartItem {

        ResultActions removeCartItem(ProductCartServiceRequest.RemoveItem request) throws Exception {
            return mockMvc.perform(delete("/api/cart")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 장바구니에서 아이템을 성공적으로 제거할 수 있다")
        @WithUserDetails
        void shouldRemoveCartItemWithAuthentication() throws Exception {
            // Given
            ProductCart cart = ProductCartFixtures.createProductCart(user);
            cart.addItem(product, 1);
            productCartRepository.save(cart);

            ProductCartServiceRequest.RemoveItem request = ProductCartServiceRequestFixtures.createRemoveItemRequest(product.getId());

            // When
            ResultActions resultActions = removeCartItem(request);

            // Then
            resultActions
                    .andExpect(status().isOk());

            ProductCart productCart = productCartRepository.findByUserId(user.getId()).get();

            assertThat(productCart.getItems())
                    .isEmpty();
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 장바구니에서 아이템 제거 시 인증 오류가 발생한다")
        @WithAnonymousUser
        void shouldDenyRemoveCartItemWithoutAuthentication() throws Exception {
            // Given
            ProductCartServiceRequest.RemoveItem request = ProductCartServiceRequestFixtures.createRemoveItemRequest(product.getId());

            // When & Then
            removeCartItem(request)
                    .andExpect(status().isForbidden());
        }
    }
}