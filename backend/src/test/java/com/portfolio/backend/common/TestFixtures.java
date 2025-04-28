package com.portfolio.backend.common;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.entity.ProductStock;
import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;
import com.portfolio.backend.service.product.dto.StockChangeReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 테스트에 사용되는 모든 테스트 픽스처(테스트 데이터)를 중앙화하는 클래스입니다.
 * 모든 테스트에서 일관된 테스트 데이터를 사용할 수 있도록 정적 메서드로 제공합니다.
 */
public class TestFixtures {

    // 테스트용 상수
    public static final Long PRODUCT_ID_1 = 1L;
    public static final Long PRODUCT_ID_2 = 2L;
    public static final Long USER_ID_1 = 1L;
    public static final Long USER_ID_2 = 2L;

    public static ProductServiceResponse.Get createProductGetResponse() {
        return new ProductServiceResponse.Get(
                PRODUCT_ID_1, 
                "맥북 프로", 
                new Money(BigDecimal.valueOf(2000000)), 
                new Money(BigDecimal.valueOf(1800000)),
                "Apple 맥북 프로 M3 칩", 
                "macbook-pro.jpg", 
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE, 
                10, 
                20, 
                LocalDateTime.now(), 
                LocalDateTime.now()
        );
    }

    public static ProductServiceResponse.GetList createProductGetListResponse1() {
        return new ProductServiceResponse.GetList(
                PRODUCT_ID_1, 
                "맥북 프로", 
                new Money(BigDecimal.valueOf(1800000)), 
                new Money(BigDecimal.valueOf(1620000)),
                "macbook-pro.jpg", 
                ProductCategory.ELECTRONICS, 
                ProductStatus.ACTIVE, 
                10, 
                10
        );
    }

    public static ProductServiceResponse.GetList createProductGetListResponse2() {
        return new ProductServiceResponse.GetList(
                PRODUCT_ID_2, 
                "아이폰 15", 
                new Money(BigDecimal.valueOf(1500000)), 
                new Money(BigDecimal.valueOf(1275000)),
                "iphone15.jpg", 
                ProductCategory.ELECTRONICS, 
                ProductStatus.ACTIVE, 
                10, 
                15
        );
    }

    public static List<ProductServiceResponse.GetList> createProductGetListResponseList() {
        return Arrays.asList(createProductGetListResponse1(), createProductGetListResponse2());
    }
}
