package com.portfolio.backend.service.product.dto;

import com.portfolio.backend.domain.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductServiceMapper {

    public ProductServiceResponse.Get toGet(Product product) {
        return new ProductServiceResponse.Get(
                product.getId(),
                product.getName(),
                product.getOriginalPrice(),
                product.getPrice(),
                product.getDescription(),
                product.getThumbnailImageUrl(),
                product.getCategory(),
                product.getStatus(),
                product.getStock().getQuantity(),
                product.getDiscountRate(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public ProductServiceResponse.GetList toList(Product product) {
        return new ProductServiceResponse.GetList(
                product.getId(),
                product.getName(),
                product.getOriginalPrice(),
                product.getPrice(),
                product.getThumbnailImageUrl(),
                product.getCategory(),
                product.getStatus(),
                product.getStock().getQuantity(),
                product.getDiscountRate()
        );
    }

    public List<ProductServiceResponse.GetList> toList(List<Product> products) {
        return products.stream()
                .map(this::toList)
                .toList();
    }
}
