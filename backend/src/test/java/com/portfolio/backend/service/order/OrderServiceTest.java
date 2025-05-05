package com.portfolio.backend.service.order;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.fixture.OrderTestFixtures;
import com.portfolio.backend.domain.order.repository.OrderRepository;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.order.dto.OrderServiceRequest;
import com.portfolio.backend.service.order.dto.OrderServiceResponse;
import com.portfolio.backend.service.order.fixture.OrderServiceRequestTestFixtures;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@DisplayName("OrderService 테스트")
class OrderServiceTest extends ServiceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCartRepository productCartRepository;

    @Autowired
    private OrderService orderService;

    @MockBean
    protected DomainEventPublisher eventPublisher;

    private Product product;
    private ProductCart cart;

    @BeforeEach
    void setUp() {
        product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

        cart = new ProductCart(user);
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
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("유효한 요청으로 주문을 생성해야 한다")
        void shouldCreateOrderWithValidRequest() {
            // Given
            OrderServiceRequest.Create request = OrderServiceRequestTestFixtures.createOrderCreateRequest(product.getPrice());

            // When
            orderService.createOrder(user.getId(), request);

            // Then
            List<Order> orders = orderRepository.findAll();
            assertThat(orders).hasSize(1);
            Order createdOrder = orders.get(0);

            assertThat(createdOrder.getUser().getId()).isEqualTo(user.getId());
            assertThat(createdOrder.getTotalPrice()).isEqualTo(request.totalPrice());
            assertThat(createdOrder.getDeliveryInfo().getName()).isEqualTo(request.deliveryInfo().name());
        }

        @Test
        @DisplayName("주문 생성 시 이벤트가 발행되어야 한다")
        void shouldPublishEventWhenOrderCreated() {
            // Given
            OrderServiceRequest.Create request = OrderServiceRequestTestFixtures.createOrderCreateRequest(product.getPrice());

            // When
            orderService.createOrder(user.getId(), request);

            // Then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(eventPublisher).publishEventsFrom(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();
            assertThat(capturedOrder).isNotNull();
            assertThat(capturedOrder.getDomainEvents()).isNotEmpty();
        }

        @Test
        @DisplayName("주문 생성 시 카트에 상품이 없다면 에러가 발생한다.")
        void shouldThrowExceptionWhenCreateOrderWithEmptyCart() {
            // Given
            OrderServiceRequest.Create request = OrderServiceRequestTestFixtures.createOrderCreateRequest(Money.zero());
            ProductCart productCart = productCartRepository.findByUserId(user.getId()).get();
            productCart.removeAllItem();
            productCartRepository.save(productCart);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(user.getId(), request))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("장바구니에 상품이 없습니다.");
        }

        @Test
        @DisplayName("주문 생성 시 주문 금액과 카트의 상품들의 총 금액이 맞지않으면 에러가 발생한다.")
        void shouldThrowExceptionWhenOrderAmountNotMatchCartTotal() {
            // Given
            OrderServiceRequest.Create request = OrderServiceRequestTestFixtures.createOrderCreateRequest(new Money(BigDecimal.ONE));

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(user.getId(), request))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("주문 금액이 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetPage {

        @Test
        @DisplayName("사용자의 주문 목록을 페이징하여 반환해야 한다")
        void shouldReturnOrderPageByUserId() {
            // Given
            Order order = orderRepository.save(OrderTestFixtures.createDefaultOrder(user, List.of(product)));
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<OrderServiceResponse.Get> result = orderService.getPage(user.getId(), pageable);

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);

            OrderServiceResponse.Get orderResponse = result.getContent().get(0);
            assertThat(orderResponse.id()).isEqualTo(order.getId().toString());
            assertThat(orderResponse.totalPrice()).isEqualTo(order.getTotalPrice());
        }

        @Test
        @DisplayName("주문이 없는 경우 빈 페이지를 반환해야 한다")
        void shouldReturnEmptyPageWhenNoOrders() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<OrderServiceResponse.Get> result = orderService.getPage(user.getId(), pageable);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문 상세 조회")
    class Get {

        @Test
        @DisplayName("존재하는 주문의 상세 정보를 반환해야 한다")
        void shouldReturnOrderDetailWhenExists() {
            // Given
            Order order = orderRepository.save(OrderTestFixtures.createDefaultOrder(user, List.of(product)));

            // When
            OrderServiceResponse.Get result = orderService.get(user.getId(), order.getId().toString());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(order.getId().toString());
            assertThat(result.orderStatus()).isEqualTo(order.getOrderStatus());
            assertThat(result.totalPrice()).isEqualTo(order.getTotalPrice());
            assertThat(result.deliveryInfo().name()).isEqualTo(order.getDeliveryInfo().getName());
            assertThat(result.orderItems()).hasSize(order.getOrderItems().size());
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            String nonExistingOrderId = UUID.randomUUID().toString();

            // When & Then
            assertThatThrownBy(() -> orderService.get(user.getId(), nonExistingOrderId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}