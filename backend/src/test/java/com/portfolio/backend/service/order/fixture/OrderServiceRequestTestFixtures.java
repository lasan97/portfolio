package com.portfolio.backend.service.order.fixture;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.service.common.dto.ServiceBaseRequest;
import com.portfolio.backend.service.order.dto.OrderServiceRequest;

import java.math.BigDecimal;

public class OrderServiceRequestTestFixtures {

    public static OrderServiceRequest.Create createOrderCreateRequest(Money totalPrice) {
        OrderServiceRequest.Create.DeliveryInfo deliveryInfo = new OrderServiceRequest.Create.DeliveryInfo(
                "홍길동",
                "010-1234-5678",
                new ServiceBaseRequest.Address("서울시 노원구", "테스트 아파트 101동 101호", "01234"),
                "문 앞에 놓아주세요"
        );

        return new OrderServiceRequest.Create(
                totalPrice,
                deliveryInfo
        );
    }
}