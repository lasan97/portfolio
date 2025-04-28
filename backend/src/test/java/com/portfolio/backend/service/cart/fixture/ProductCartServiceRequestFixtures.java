package com.portfolio.backend.service.cart.fixture;

import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest;

public class ProductCartServiceRequestFixtures {

    public static ProductCartServiceRequest.AddItem createAddItemRequest(Long productId, int quantity) {
        return new ProductCartServiceRequest.AddItem(productId, quantity);
    }

    public static ProductCartServiceRequest.RemoveItem createRemoveItemRequest(Long productId) {
        return new ProductCartServiceRequest.RemoveItem(productId);
    }
}
