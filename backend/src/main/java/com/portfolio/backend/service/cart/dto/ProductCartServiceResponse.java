package com.portfolio.backend.service.cart.dto;

import com.portfolio.backend.service.common.dto.ServiceBaseResponse;

public class ProductCartServiceResponse {

    public record Get(
            ServiceBaseResponse.SimpleProduct product,
            Integer quantity
    ) {
    }
}
