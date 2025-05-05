package com.portfolio.backend.service.order.dto;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.common.value.Address;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.value.DeliveryInfo;
import com.portfolio.backend.domain.order.value.OrderItem;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.common.dto.ServiceBaseRequest;
import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderServiceMapper {

    private final UserRepository userRepository;
    private final ProductCartRepository cartRepository;

    public Order createOrder(Long userId, OrderServiceRequest.Create request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("사용자 정보가 없습니다. 잘못된 접근입니다."));

        Optional<ProductCart> cartResponse = cartRepository.findByUserId(userId);

        if (cartResponse.isEmpty() || cartResponse.get().getItems().isEmpty()) {
            throw new UnprocessableEntityException("장바구니에 상품이 없습니다.");
        }

        List<OrderItem> orderItems = cartResponse.get().getItems().stream().map(item ->
                new OrderItem(item.getProduct(), item.getQuantity())).toList();

        ServiceBaseRequest.Address requestAddress = request.deliveryInfo().address();

        Address address = new Address(requestAddress.address(),
                requestAddress.detailAddress(),
                requestAddress.postCode());

        DeliveryInfo deliveryInfo = new DeliveryInfo(request.deliveryInfo().name(),
                request.deliveryInfo().phone(),
                address,
                request.deliveryInfo().deliveryRequest());

        return Order.builder()
                .user(user)
                .orderItems(orderItems)
                .totalPrice(request.totalPrice())
                .deliveryInfo(deliveryInfo)
                .build();
    }


    public OrderServiceResponse.Get toGet(Order order) {

        OrderServiceResponse.DeliveryInfo deliveryInfo = toDeliveryInfo(order.getDeliveryInfo());

        List<OrderServiceResponse.OrderItem> orderItems = toOrderItems(order.getOrderItems());

        return new OrderServiceResponse.Get(
                order.getId().toString(),
                order.getOrderStatus(),
                deliveryInfo,
                order.getTotalPrice(),
                orderItems,
                order.getCreatedAt()
        );
    }

    private List<OrderServiceResponse.OrderItem> toOrderItems(List<OrderItem> orderItems) {
        return orderItems.stream().map(item -> {
            OrderServiceResponse.OrderItem.OrderProduct product = new OrderServiceResponse.OrderItem.OrderProduct(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getOriginalPrice(),
                    item.getProduct().getPrice(),
                    item.getProduct().getThumbnailImageUrl());

            return new OrderServiceResponse.OrderItem(product, item.getQuantity());
        })
        .toList();
    }

    private OrderServiceResponse.DeliveryInfo toDeliveryInfo(DeliveryInfo deliveryInfo) {
        return new OrderServiceResponse.DeliveryInfo(
                deliveryInfo.getName(),
                deliveryInfo.getPhone(),
                deliveryInfo.getDeliveryRequest(),
                new ServiceBaseResponse.Address(deliveryInfo.getAddress())
        );
    }
}
