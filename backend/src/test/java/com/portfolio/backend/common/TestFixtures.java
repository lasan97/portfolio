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

    // 제품 관련 픽스처
    public static Product createProduct(Long id, String name, int originalPrice, int price, String description,
                                     String thumbnailImageUrl, ProductCategory category, int stock) {
        Product product = Product.builder()
                .name(name)
                .originalPrice(new Money(BigDecimal.valueOf(originalPrice)))
                .price(new Money(BigDecimal.valueOf(price)))
                .description(description)
                .thumbnailImageUrl(thumbnailImageUrl)
                .category(category)
                .stock(stock)
                .build();

        // 리플렉션을 사용하여 ID 설정
        try {
            java.lang.reflect.Field idField = Product.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set product ID", e);
        }

        return product;
    }

    public static Product createDefaultProduct() {
        return createProduct(
                PRODUCT_ID_1, 
                "맥북 프로 M2", 
                2000000, 
                1800000, 
                "2023년형 맥북 프로 M2 모델", 
                "https://example.com/macbook.jpg", 
                ProductCategory.ELECTRONICS, 
                100
        );
    }

    public static Product createProductWithZeroStock() {
        return createProduct(
                PRODUCT_ID_1, 
                "맥북 프로 M2", 
                2000000, 
                1800000, 
                "2023년형 맥북 프로 M2 모델", 
                "https://example.com/macbook.jpg", 
                ProductCategory.ELECTRONICS, 
                0
        );
    }

    public static Product createProductWithStatus(ProductStatus status) {
        Product product = createDefaultProduct();
        try {
            java.lang.reflect.Field statusField = Product.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(product, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set product status", e);
        }
        return product;
    }

    public static Product createSecondProduct() {
        return createProduct(
                PRODUCT_ID_2, 
                "아이폰 15", 
                1500000, 
                1200000, 
                "2023년형 아이폰 15", 
                "https://example.com/iphone15.jpg", 
                ProductCategory.ELECTRONICS, 
                50
        );
    }

    public static List<Product> createProductList() {
        return Arrays.asList(createDefaultProduct(), createSecondProduct());
    }

    // ProductService DTO 관련 픽스처
    public static ProductServiceRequest.Create createProductCreateRequest() {
        return new ProductServiceRequest.Create(
                "맥북 프로", 
                new Money(BigDecimal.valueOf(2000000)), 
                new Money(BigDecimal.valueOf(1800000)),
                "Apple 맥북 프로 M3 칩", 
                "macbook-pro.jpg", 
                ProductCategory.ELECTRONICS, 
                10
        );
    }

    public static ProductServiceRequest.Update createProductUpdateRequest() {
        return new ProductServiceRequest.Update(
                "맥북 프로 M3", 
                new Money(BigDecimal.valueOf(2100000)), 
                new Money(BigDecimal.valueOf(1900000)),
                "Apple 맥북 프로 M3 칩 업데이트", 
                "macbook-pro-m3.jpg", 
                ProductCategory.ELECTRONICS
        );
    }

    public static ProductServiceRequest.AdjustStock createStockAdjustRequest(int quantity, StockChangeReason reason, String description) {
        return new ProductServiceRequest.AdjustStock(quantity, reason, description);
    }

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

    // 사용자 관련 픽스처
    public static User createAdminUser() {
        return new User("admin@email.com", "tester", Oauth2ProviderType.GITHUB, "1", null, RoleType.ADMIN);
    }

    public static User createRegularUser() {
        return new User("user@email.com", "tester", Oauth2ProviderType.GITHUB, "1", null, RoleType.USER);
    }
}
