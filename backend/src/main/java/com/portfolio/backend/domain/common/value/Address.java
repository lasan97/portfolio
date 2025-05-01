package com.portfolio.backend.domain.common.value;

import com.portfolio.backend.common.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(nullable = false)
    private String address;

    private String detailAddress;

    @Column(nullable = false)
    private String postCode;

    public Address(String address, String detailAddress, String postCode) {
        if (address == null || address.isBlank()) {
            throw new DomainException("주소는 비어있을 수 없습니다.");
        }
        if (postCode == null || postCode.isBlank()) {
            throw new DomainException("우편번호는 비어있을 수 없습니다.");
        }

        this.address = address;
        this.detailAddress = detailAddress;
        this.postCode = postCode;
    }
}
