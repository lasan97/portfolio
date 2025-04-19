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
    public List<ProductServiceResponse.List> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/{id}")
    public ProductServiceResponse.Get getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long createProduct(@Valid @RequestBody ProductServiceRequest.Create request,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Long updateProduct(@PathVariable Long id,
                           @Valid @RequestBody ProductServiceRequest.Update request,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productService.deleteProduct(id);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public Long adjustStock(@PathVariable Long id,
                         @Valid @RequestBody ProductServiceRequest.AdjustStock request,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return productService.adjustStock(id, request);
    }
}
