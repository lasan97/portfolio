package com.portfolio.backend.domain.order.value;

import com.portfolio.backend.common.config.converter.MoneyConverter;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct implements Serializable {

    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money originalPrice;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money price;

    @Column
    private String thumbnailImageUrl;

    public OrderProduct(Product product) {
        if (product == null) {
            throw new DomainException("상품은 비어있을 수 없습니다.");
        }

        this.id = product.getId();
        this.name = product.getName();
        this.originalPrice = product.getOriginalPrice();
        this.price = product.getPrice();
        this.thumbnailImageUrl = product.getThumbnailImageUrl();
    }
}
