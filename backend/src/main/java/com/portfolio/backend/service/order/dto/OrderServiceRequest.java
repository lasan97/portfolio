package com.portfolio.backend.service.order.dto;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.service.common.dto.ServiceBaseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderServiceRequest {

    public record Create(
            @NotNull(message = "주문 금액은 필수입니다.")
            Money totalPrice,

            @NotNull(message = "배송 정보는 필수입니다.")
            @Valid
            DeliveryInfo deliveryInfo
    ) {

        public record DeliveryInfo(
                @NotBlank(message = "주문자 이름은 필수입니다.")
                String name,

                @NotBlank(message = "주문자 연락처는 필수입니다.")
                String phone,

                @NotNull
                @Valid
                ServiceBaseRequest.Address address,

                String deliveryRequest
        ) {}
    }
}
