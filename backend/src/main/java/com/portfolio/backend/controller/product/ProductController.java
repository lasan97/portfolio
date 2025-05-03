package com.portfolio.backend.controller.product;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.product.ProductService;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductServiceResponse.SimpleGet> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/{productId}")
    public ProductServiceResponse.Get getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long createProduct(@Valid @RequestBody ProductServiceRequest.Create request,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return productService.createProduct(request);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Long updateProduct(@PathVariable Long productId,
                           @Valid @RequestBody ProductServiceRequest.Update request,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return productService.updateProduct(productId, request);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable Long productId,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productService.deleteProduct(productId);
    }

    @PatchMapping("/{productId}/stock")
    @PreAuthorize("isAuthenticated()") // 인증된 사용자라면 누구나 재고 조정 가능
    public void adjustStock(@PathVariable Long productId,
                         @Valid @RequestBody ProductServiceRequest.AdjustStock request,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productService.adjustStock(productId, request);
    }
}
