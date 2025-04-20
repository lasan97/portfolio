package com.portfolio.backend.service.cart.dto;

import com.portfolio.backend.domain.cart.domain.ProductCartItem;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductCartServiceMapper {

    public ProductCartServiceResponse.Get toGet(ProductCartItem item) {
        return new ProductCartServiceResponse.Get(
                toSimpleProduct(item.getProduct()),
                item.getQuantity()
        );
    }

    private ServiceBaseResponse.SimpleProduct toSimpleProduct(Product product) {
        return new ServiceBaseResponse.SimpleProduct(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getOriginalPrice().getAmount(),
                product.getStatus()
        );
    }
}
