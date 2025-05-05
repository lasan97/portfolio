package com.portfolio.backend.domain.order.value;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem implements Serializable {

    @Embedded
    private OrderProduct product;

    private Integer quantity;

    public OrderItem(Product product, Integer quantity) {
        if(product == null){
            throw new DomainException("상품은 비어있을 수 없습니다.");
        }
        if(quantity == null || quantity < 0){
            throw new DomainException("수량은 음수일 수 없습니다.");
        }

        this.product = new OrderProduct(product);
        this.quantity = quantity;
    }

    public Money getTotalPrice() {
        return product.getPrice().multiply(quantity);
    }
}
