package com.portfolio.backend.domain.product.fixture;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ProductTestFixtures {
    public static Product createProduct(Long id, String name, Money originalPrice, Money price, String description,
                                        String thumbnailImageUrl, ProductCategory category, int stock) {
        Product product = Product.builder()
                .name(name)
                .originalPrice(originalPrice)
                .price(price)
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

    public static Product createDefaultProduct(Long productId, Money originalPrice, Money price, int stock) {
        return createProduct(
                productId,
                "맥북 프로 M2",
                originalPrice,
                price,
                "2023년형 맥북 프로 M2 모델",
                "https://example.com/macbook.jpg",
                ProductCategory.ELECTRONICS,
                stock
        );
    }

    public static Product createDefaultProduct() {
        return createDefaultProduct(null, new Money(BigDecimal.valueOf(200000)), new Money(BigDecimal.valueOf(180000)), 10);
    }

    public static Product createDefaultProduct(Money originalPrice, Money price) {
        return createDefaultProduct(null, originalPrice, price, 100);
    }

    public static Product createDefaultProduct(int stock) {
        return createDefaultProduct(null, new Money(BigDecimal.valueOf(200000)), new Money(BigDecimal.valueOf(180000)), stock);
    }

    public static Product createProductWithStatus(ProductStatus status) {
        Product product = createDefaultProduct(1);

        switch (status) {
            case SOLD_OUT -> product.soldOut();
            case DELETED -> product.delete();
        }

        return product;
    }

    public static List<Product> createProductList() {
        return Arrays.asList(createDefaultProduct(1), createDefaultProduct(1));
    }
}
