package com.portfolio.backend.service.product;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.service.ProductStockManager;
import com.portfolio.backend.service.product.dto.ProductStockServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductRepository productRepository;
    private final ProductStockManager productStockManager;

    @Transactional
    public void adjustStock(Long productId, ProductStockServiceRequest.AdjustStock request) {
        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + productId));

        productStockManager.adjust(product.getId(), request.quantity(), "재고 조정");
    }
}
