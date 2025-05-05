package com.portfolio.backend.controller.order;

import com.portfolio.backend.controller.ControllerTest;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.fixture.OrderTestFixtures;
import com.portfolio.backend.domain.order.repository.OrderRepository;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.order.dto.OrderServiceRequest;
import com.portfolio.backend.service.order.fixture.OrderServiceRequestTestFixtures;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DisplayName("OrderController 테스트")
class OrderControllerTest extends ControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCartRepository productCartRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

        ProductCart cart = new ProductCart(user);
        cart.addItem(product, 1);
        productCartRepository.save(cart);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productCartRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("주문 생성 API")
    class CreateOrder {

        @SneakyThrows
        ResultActions createOrder(OrderServiceRequest.Create request) {
            return mockMvc.perform(post("/api/orders")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 주문을 생성할 수 있다")
        @WithUserDetails
        void shouldCreateOrderWhenAuthenticated() throws Exception {
            // Given
            OrderServiceRequest.Create request = OrderServiceRequestTestFixtures.createOrderCreateRequest(product.getPrice());

            // When
            ResultActions resultActions = createOrder(request);

            // Then
            resultActions
                    .andExpect(status().isOk());

            List<Order> orders = orderRepository.findAll();
            assertThat(orders).isNotEmpty();
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 주문을 생성할 수 없다")
        @WithAnonymousUser
        void shouldNotCreateOrderWhenNotAuthenticated() throws Exception {
            // Given
            OrderServiceRequest.Create request = OrderServiceRequestTestFixtures.createOrderCreateRequest(product.getPrice());

            // When & Then
            createOrder(request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("주문 목록 조회 API")
    class GetPage {

        @SneakyThrows
        ResultActions getPage() {
            return mockMvc.perform(get("/api/orders/page"))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 자신의 주문 목록을 조회할 수 있다")
        @WithUserDetails
        void shouldReturnOrderPageWhenAuthenticated() throws Exception {
            // Given
            orderRepository.save(OrderTestFixtures.createDefaultOrder(user, Collections.singletonList(product)));

            // When & Then
            getPage()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").exists())
                    .andExpect(jsonPath("$.content[0].orderStatus").exists())
                    .andExpect(jsonPath("$.content[0].totalPrice").exists());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 주문 목록을 조회할 수 없다")
        @WithAnonymousUser
        void shouldNotReturnOrderPageWhenNotAuthenticated() throws Exception {
            // When & Then
            getPage()
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("주문 상세 조회 API")
    class GetOrder {

        @SneakyThrows
        ResultActions getOrder(String orderId) {
            return mockMvc.perform(get("/api/orders/{orderId}", orderId))
                    .andDo(print());
        }

        @Test
        @DisplayName("인증된 사용자는 자신의 주문 상세를 조회할 수 있다")
        @WithUserDetails
        void shouldReturnOrderDetailWhenAuthenticated() throws Exception {
            // Given
            Order order = orderRepository.save(OrderTestFixtures.createDefaultOrder(user, Collections.singletonList(product)));

            // When & Then
            getOrder(order.getId().toString())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(order.getId().toString()))
                    .andExpect(jsonPath("$.orderStatus").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.deliveryInfo").exists())
                    .andExpect(jsonPath("$.orderItems").isArray());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 주문 상세를 조회할 수 없다")
        @WithAnonymousUser
        void shouldNotReturnOrderDetailWhenNotAuthenticated() throws Exception {
            // Given
            Order order = orderRepository.save(OrderTestFixtures.createDefaultOrder(user, Collections.singletonList(product)));

            // When & Then
            getOrder(order.getId().toString())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404 응답을 반환해야 한다")
        @WithUserDetails
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            String nonExistingOrderId = UUID.randomUUID().toString();

            // When & Then
            getOrder(nonExistingOrderId)
                    .andExpect(status().isNotFound());
        }
    }
}