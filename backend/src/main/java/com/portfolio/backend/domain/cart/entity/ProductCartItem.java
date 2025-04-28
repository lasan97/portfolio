package com.portfolio.backend.domain.cart.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCartItem implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public ProductCartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;

        validation();
    }

    public ProductCartItem(Product product) {
        this.product = product;
        this.quantity = 0;

        validation();
    }

    private void validation() {
        if(product == null){
            throw new DomainException("상품은 비어있을 수 없습니다.");
        }
        if(quantity == null || quantity < 0){
            throw new DomainException("수량은 음수일 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductCartItem that = (ProductCartItem) o;
        return Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(product);
    }
}
