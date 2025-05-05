package com.portfolio.backend.domain.common.value;

import com.portfolio.backend.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @DisplayName("주소가 정상적으로 생성된다")
    @Test
    void createAddress_Success() {
        // given
        String address = "서울시 강남구";
        String detailAddress = "삼성동 123";
        String postCode = "12345";

        // when
        Address addressObj = new Address(address, detailAddress, postCode);

        // then
        assertEquals(address, addressObj.getAddress());
        assertEquals(detailAddress, addressObj.getDetailAddress());
        assertEquals(postCode, addressObj.getPostCode());
    }

    @DisplayName("주소가 null인 경우 예외가 발생한다")
    @Test
    void createAddress_WithNullAddress_ThrowsException() {
        // given
        String detailAddress = "삼성동 123";
        String postCode = "12345";

        // when & then
        assertThrows(DomainException.class, () -> {
            new Address(null, detailAddress, postCode);
        });
    }

    @DisplayName("주소가 빈 문자열인 경우 예외가 발생한다")
    @Test
    void createAddress_WithBlankAddress_ThrowsException() {
        // given
        String detailAddress = "삼성동 123";
        String postCode = "12345";

        // when & then
        assertThrows(DomainException.class, () -> {
            new Address("", detailAddress, postCode);
        });
    }

    @DisplayName("상세 주소가 null인 경우에도 정상적으로 생성된다")
    @Test
    void createAddress_WithNullDetailAddress_Success() {
        // given
        String address = "서울시 강남구";
        String postCode = "12345";

        // when
        Address addressObj = new Address(address, null, postCode);

        // then
        assertEquals(address, addressObj.getAddress());
        assertNull(addressObj.getDetailAddress());
        assertEquals(postCode, addressObj.getPostCode());
    }

    @DisplayName("상세 주소가 빈 문자열인 경우에도 정상적으로 생성된다")
    @Test
    void createAddress_WithEmptyDetailAddress_Success() {
        // given
        String address = "서울시 강남구";
        String postCode = "12345";

        // when
        Address addressObj = new Address(address, "", postCode);

        // then
        assertEquals(address, addressObj.getAddress());
        assertEquals("", addressObj.getDetailAddress());
        assertEquals(postCode, addressObj.getPostCode());
    }

    @DisplayName("우편번호가 null인 경우 예외가 발생한다")
    @Test
    void createAddress_WithNullPostCode_ThrowsException() {
        // given
        String address = "서울시 강남구";
        String detailAddress = "삼성동 123";

        // when & then
        assertThrows(DomainException.class, () -> {
            new Address(address, detailAddress, null);
        });
    }

    @DisplayName("우편번호가 빈 문자열인 경우 예외가 발생한다")
    @Test
    void createAddress_WithBlankPostCode_ThrowsException() {
        // given
        String address = "서울시 강남구";
        String detailAddress = "삼성동 123";

        // when & then
        assertThrows(DomainException.class, () -> {
            new Address(address, detailAddress, "");
        });
    }
}
