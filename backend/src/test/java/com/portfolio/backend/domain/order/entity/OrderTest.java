package com.portfolio.backend.domain.order.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.value.Address;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.order.fixture.OrderTestFixtures;
import com.portfolio.backend.domain.order.value.DeliveryInfo;
import com.portfolio.backend.domain.order.value.OrderItem;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.fixture.UserTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @DisplayName("주문 생성 시 모든 필드가 정상적으로 설정된다")
        @Test
        void createOrder_Success() {
            // given
            User user = UserTestFixtures.createUser();

            Product product = ProductTestFixtures.createDefaultProduct();
            OrderItem orderItem = new OrderItem(product, 2);
            List<OrderItem> orderItems = Collections.singletonList(orderItem);

            Money totalPrice = orderItem.getTotalPrice();

            Address address = new Address("서울시 강남구", "삼성동 123", "12345");
            DeliveryInfo deliveryInfo = new DeliveryInfo("홍길동", "010-0000-0000", address, "부재시 경비실에 맡겨주세요");

            // when
            Order order = new Order(user, totalPrice, orderItems, deliveryInfo);

            // then
            assertNotNull(order.getId());
            assertEquals(OrderStatus.PENDING, order.getOrderStatus());
            assertEquals(totalPrice, order.getTotalPrice());
            assertEquals(1, order.getOrderItems().size());
            assertTrue(order.getOrderItems().contains(orderItem));
            assertEquals(deliveryInfo, order.getDeliveryInfo());
            assertNotNull(order.getCreatedAt());
        }

        @DisplayName("주문 생성 시 주문 항목이 비어있으면 예외가 발생한다")
        @Test
        void createOrder_WithEmptyOrderItems_ThrowsException() {
            // given
            User user = UserTestFixtures.createUser();

            Money totalPrice = new Money(new BigDecimal("16000"));
            List<OrderItem> orderItems = Collections.emptyList();

            Address address = new Address("서울시 강남구", "삼성동 123", "12345");
            DeliveryInfo deliveryInfo = new DeliveryInfo("홍길동", "010-1234-5678", address, "부재시 경비실에 맡겨주세요");

            // when & then
            assertThrows(DomainException.class, () -> {
                new Order(user, totalPrice, orderItems, deliveryInfo);
            });
        }

        @DisplayName("주문 생성 시 주문자 정보가 null이면 예외가 발생한다")
        @Test
        void createOrder_throwsException_whenOrdererIsNull() {
            // given
            Product product = ProductTestFixtures.createDefaultProduct();

            OrderItem orderItem = new OrderItem(product, 2);
            List<OrderItem> orderItems = Collections.singletonList(orderItem);

            Money totalPrice = orderItem.getTotalPrice();

            Address address = new Address("서울시 강남구", "삼성동 123", "12345");
            DeliveryInfo deliveryInfo = new DeliveryInfo("홍길동", "010-1234-5678", address, "부재시 경비실에 맡겨주세요");


            // when & then
            assertThrows(DomainException.class, () -> {
                new Order(null, totalPrice, orderItems, deliveryInfo);
            });
        }


        @DisplayName("주문 생성 시 배송 정보가 null이면 예외가 발생한다")
        @Test
        void createOrder_WithNullDeliveryInfo_ThrowsException() {
            // given
            User user = UserTestFixtures.createUser();

            Product product = ProductTestFixtures.createDefaultProduct();

            OrderItem orderItem = new OrderItem(product, 2);
            List<OrderItem> orderItems = Collections.singletonList(orderItem);

            Money totalPrice = orderItem.getTotalPrice();

            // when & then
            assertThrows(DomainException.class, () -> {
                new Order(user, totalPrice, orderItems, null);
            });
        }

        @DisplayName("주문 생성 시 총 가격이 주문 항목의 총 가격과 일치하지 않으면 예외가 발생한다")
        @Test
        void createOrder_WithMismatchedTotalPrice_ThrowsException() {
            // given
            User user = UserTestFixtures.createUser();

            Money originalPrice = new Money(new BigDecimal("10000"));
            Money price = new Money(new BigDecimal("8000"));

            Product product = ProductTestFixtures.createDefaultProduct(originalPrice, price);

            OrderItem orderItem = new OrderItem(product, 2);
            List<OrderItem> orderItems = Collections.singletonList(orderItem);

            Money totalPrice = price.multiply(1);

            Address address = new Address("서울시 강남구", "삼성동 123", "12345");
            DeliveryInfo deliveryInfo = new DeliveryInfo("홍길동", "010-1234-5678", address, "부재시 경비실에 맡겨주세요");

            // when & then
            assertThrows(UnprocessableEntityException.class, () -> {
                new Order(user, totalPrice, orderItems, deliveryInfo);
            });
        }
    }

    @Nested
    @DisplayName("결제완료")
    class PaymentCompleted {

        @DisplayName("결제 완료 상태로 변경할 수 있다")
        @Test
        void paymentCompleted_Success() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));

            // when
            order.paymentCompleted();

            // then
            assertEquals(OrderStatus.PAID, order.getOrderStatus());
        }

        @DisplayName("대기 상태가 아닐 때 결제 완료로 변경하면 예외가 발생한다")
        @Test
        void paymentCompleted_ThrowsException_WhenInvalidState() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));
            order.paymentCompleted();
            order.completedStockReduction();

            // when & Then
            assertThrows(DomainException.class, order::paymentCompleted);
        }
    }

    @Nested
    @DisplayName("재고 차감 완료")
    class CompletedStockReduction {

        @DisplayName("재고 확인 완료 상태로 변경할 수 있다")
        @Test
        void completedStockReduction_Success() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));
            order.paymentCompleted();

            // when
            order.completedStockReduction();

            // then
            assertEquals(OrderStatus.ORDERED, order.getOrderStatus());
        }

        @DisplayName("결제 완료 상태가 아닐 때 재고 차감 완료하면 예외가 발생한다")
        @Test
        void completedStockReduction_ThrowsException_WhenInvalidState() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));

            // when & then
            assertThrows(DomainException.class, order::completedStockReduction);
        }
    }

    @Nested
    @DisplayName("취소 요청")
    class Canceling {

        @DisplayName("취소 요청 상태로 변경할 수 있다.")
        @Test
        void canceling_Success() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));
            order.paymentCompleted();
            order.completedStockReduction();

            // when
            order.canceling();

            // then
            assertEquals(OrderStatus.CANCELING, order.getOrderStatus());
        }

        @DisplayName("주문 완료 상태가 아닐 때 취소 요청 완료하면 예외가 발생한다")
        @Test
        void canceling_ThrowsException_WhenInvalidState() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));

            // when & then
            assertThrows(DomainException.class, order::canceling);
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class Cancel {

        @DisplayName("주문 취소할 수 있다")
        @Test
        void cancel_Success() {
            // given
            User user = UserTestFixtures.createUser();

            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));
            order.paymentCompleted();
            order.completedStockReduction();
            order.canceling();

            // when
            order.cancel();

            // then
            assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        }

        @DisplayName("취소 요청 상태가 아닐 때 취소하면 예외가 발생한다")
        @Test
        void cancel_InvalidState_ThrowsException() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));

            // when & then
            assertThrows(DomainException.class, order::cancel);
        }
    }

    @Nested
    @DisplayName("주문 실패")
    class Failed {

        @DisplayName("주문이 실패 상태로 변경될 수 있다")
        @Test
        void failed_Success() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));

            // when
            order.failed();

            // then
            assertEquals(OrderStatus.FAILED, order.getOrderStatus());
        }

        @DisplayName("주문 완료 상태일 때 실패로 변경하면 예외가 발생한다")
        @Test
        void failed_ThrowsException_WhenInvalidState() {
            // given
            User user = UserTestFixtures.createUser();
            Product product = ProductTestFixtures.createDefaultProduct();

            Order order = OrderTestFixtures.createDefaultOrder(user, List.of(product));

            order.paymentCompleted();
            order.completedStockReduction();

            // when & then
            assertThrows(DomainException.class, order::failed);
        }
    }
}
