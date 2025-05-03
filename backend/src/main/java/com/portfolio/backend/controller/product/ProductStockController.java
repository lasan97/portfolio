package com.portfolio.backend.controller.product;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.product.ProductStockService;
import com.portfolio.backend.service.product.dto.ProductStockServiceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products/{productId}/stocks")
@RequiredArgsConstructor
public class ProductStockController {

    private final ProductStockService productStockService;

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public void adjustStock(@PathVariable Long productId,
                         @Valid @RequestBody ProductStockServiceRequest.AdjustStock request,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productStockService.adjustStock(productId, request);
    }
}
