package com.portfolio.backend.domain.product.fixture;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ProductTestFixtures {

    // 테스트용 상수
    public static final Long PRODUCT_ID_1 = 1L;
    public static final Long PRODUCT_ID_2 = 2L;

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

        if (id != null) {
            try {
                java.lang.reflect.Field idField = Product.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(product, id);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set product ID", e);
            }
        }

        return product;
    }

    public static Product createDefaultProduct(Long productId, int stock) {
        return createProduct(
                productId,
                "맥북 프로 M2",
                2000000,
                1800000,
                "2023년형 맥북 프로 M2 모델",
                "https://example.com/macbook.jpg",
                ProductCategory.ELECTRONICS,
                stock
        );
    }

    public static Product createDefaultProduct(int stock) {
        return createDefaultProduct(null, stock);
    }

    public static Product createProductWithStatus(ProductStatus status) {
        Product product = createDefaultProduct(1);
        try {
            java.lang.reflect.Field statusField = Product.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(product, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set product status", e);
        }
        return product;
    }

    public static List<Product> createProductList() {
        return Arrays.asList(createDefaultProduct(1), createDefaultProduct(1));
    }
}
