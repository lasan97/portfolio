package com.portfolio.backend.domain.order.value;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryInfo implements Serializable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Embedded
    private Address address;

    @Column
    private String deliveryRequest;

    public DeliveryInfo(String name, String phone, Address address, String deliveryRequest) {
        if (name == null || name.isBlank()) {
            throw new DomainException("주문자 이름은 비어있을 수 없습니다.");
        }
        if (phone == null || phone.isBlank()) {
            throw new DomainException("주문자 연락처는 비어있을 수 없습니다.");
        }
        if (address == null) {
            throw new DomainException("배송 주소는 비어있을 수 없습니다.");
        }
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.deliveryRequest = deliveryRequest;
    }
}
