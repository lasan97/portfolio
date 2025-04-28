package com.portfolio.backend.domain.cart.fixture;

import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.user.entity.User;

public class ProductCartFixtures {

    public static ProductCart createProductCart(User user) {
        return new ProductCart(user);
    }
}
