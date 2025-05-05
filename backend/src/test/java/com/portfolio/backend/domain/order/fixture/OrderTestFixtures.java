package com.portfolio.backend.domain.order.fixture;

import com.portfolio.backend.domain.common.value.Address;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.value.DeliveryInfo;
import com.portfolio.backend.domain.order.value.OrderItem;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.user.entity.User;

import java.util.List;

public class OrderTestFixtures {

    public static Order createDefaultOrder(User user, List<Product> products) {
        List<OrderItem> orderItems = products.stream()
                .map(product -> new OrderItem(product, 1))
                .toList();

        Money totalPrice = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money.zero(), Money::add);

        Address address = new Address("서울시 노원구", "테스트 아파트 101동 101호", "01234");
        DeliveryInfo deliveryInfo = new DeliveryInfo("홍길동", "010-1234-5678", address, "문 앞에 놓아주세요");

        return Order.builder()
                .user(user)
                .orderItems(orderItems)
                .totalPrice(totalPrice)
                .deliveryInfo(deliveryInfo)
                .build();
    }
}