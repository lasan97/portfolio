package com.portfolio.backend.domain.order.value;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryInfoTest {

    @DisplayName("배송 정보가 정상적으로 생성된다")
    @Test
    void createDeliveryInfo_Success() {
        // given
        String name = "홍길동";
        String phone = "010-1234-5678";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");
        String deliveryRequest = "부재시 경비실에 맡겨주세요";

        // when
        DeliveryInfo deliveryInfo = new DeliveryInfo(name, phone, address, deliveryRequest);

        // then
        assertEquals(name, deliveryInfo.getName());
        assertEquals(phone, deliveryInfo.getPhone());
        assertEquals(address, deliveryInfo.getAddress());
        assertEquals(deliveryRequest, deliveryInfo.getDeliveryRequest());
    }

    @DisplayName("이름이 null인 경우 예외가 발생한다")
    @Test
    void createDeliveryInfo_WithNullName_ThrowsException() {
        // given
        String phone = "010-1234-5678";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");
        String deliveryRequest = "부재시 경비실에 맡겨주세요";

        // when & then
        assertThrows(DomainException.class, () -> {
            new DeliveryInfo(null, phone, address, deliveryRequest);
        });
    }

    @DisplayName("이름이 빈 문자열인 경우 예외가 발생한다")
    @Test
    void createDeliveryInfo_WithBlankName_ThrowsException() {
        // given
        String phone = "010-1234-5678";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");
        String deliveryRequest = "부재시 경비실에 맡겨주세요";

        // when & then
        assertThrows(DomainException.class, () -> {
            new DeliveryInfo("", phone, address, deliveryRequest);
        });
    }

    @DisplayName("전화번호가 null인 경우 예외가 발생한다")
    @Test
    void createDeliveryInfo_WithNullPhone_ThrowsException() {
        // given
        String name = "홍길동";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");
        String deliveryRequest = "부재시 경비실에 맡겨주세요";

        // when & then
        assertThrows(DomainException.class, () -> {
            new DeliveryInfo(name, null, address, deliveryRequest);
        });
    }

    @DisplayName("전화번호가 빈 문자열인 경우 예외가 발생한다")
    @Test
    void createDeliveryInfo_WithBlankPhone_ThrowsException() {
        // given
        String name = "홍길동";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");
        String deliveryRequest = "부재시 경비실에 맡겨주세요";

        // when & then
        assertThrows(DomainException.class, () -> {
            new DeliveryInfo(name, "", address, deliveryRequest);
        });
    }

    @DisplayName("주소가 null인 경우 예외가 발생한다")
    @Test
    void createDeliveryInfo_WithNullAddress_ThrowsException() {
        // given
        String name = "홍길동";
        String phone = "010-1234-5678";
        String deliveryRequest = "부재시 경비실에 맡겨주세요";

        // when & then
        assertThrows(DomainException.class, () -> {
            new DeliveryInfo(name, phone, null, deliveryRequest);
        });
    }

    @DisplayName("배송 요청사항이 null인 경우에도 정상적으로 생성된다")
    @Test
    void createDeliveryInfo_WithNullDeliveryRequest_Success() {
        // given
        String name = "홍길동";
        String phone = "010-1234-5678";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");

        // when
        DeliveryInfo deliveryInfo = new DeliveryInfo(name, phone, address, null);

        // then
        assertEquals(name, deliveryInfo.getName());
        assertEquals(phone, deliveryInfo.getPhone());
        assertEquals(address, deliveryInfo.getAddress());
        assertNull(deliveryInfo.getDeliveryRequest());
    }

    @DisplayName("배송 요청사항이 빈 문자열인 경우에도 정상적으로 생성된다")
    @Test
    void createDeliveryInfo_WithEmptyDeliveryRequest_Success() {
        // given
        String name = "홍길동";
        String phone = "010-1234-5678";
        Address address = new Address("서울시 강남구", "삼성동 123", "12345");

        // when
        DeliveryInfo deliveryInfo = new DeliveryInfo(name, phone, address, "");

        // then
        assertEquals(name, deliveryInfo.getName());
        assertEquals(phone, deliveryInfo.getPhone());
        assertEquals(address, deliveryInfo.getAddress());
        assertEquals("", deliveryInfo.getDeliveryRequest());
    }
}
