package com.portfolio.backend.controller.cart;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.cart.ProductCartService;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest;
import com.portfolio.backend.service.cart.dto.ProductCartServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class ProductCartController {

    private final ProductCartService productCartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductCartServiceResponse.Get>> getCartItems(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(productCartService.getCartItems(userDetails.getId()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addCartItem(
            @Valid @RequestBody ProductCartServiceRequest.AddItem request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productCartService.addCartItem(request, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeCartItem(
            @Valid @RequestBody ProductCartServiceRequest.RemoveItem request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productCartService.removeCartItem(request, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
