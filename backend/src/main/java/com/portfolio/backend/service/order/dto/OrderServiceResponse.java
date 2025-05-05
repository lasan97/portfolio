package com.portfolio.backend.service.order.dto;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.order.entity.OrderStatus;
import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderServiceResponse {

    public record Get(
            String id,
            OrderStatus orderStatus,
            DeliveryInfo deliveryInfo,
            Money totalPrice,
            List<OrderItem> orderItems,
            LocalDateTime createdAt
    ) {}

    public record DeliveryInfo(
            String name,
            String phone,
            String deliveryRequest,
            ServiceBaseResponse.Address address
    ) {}

    public record OrderItem(
            OrderProduct product,
            Integer quantity
    ) {
        public record OrderProduct(
                Long id,
                String name,
                Money originalPrice,
                Money price,
                String thumbnailImageUrl
        ) {}
    }

}
