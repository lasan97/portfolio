package com.portfolio.backend.service.product;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.entity.ProductStockHistory;
import com.portfolio.backend.domain.product.entity.StockChangeReason;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockHistoryRepository;
import com.portfolio.backend.service.product.dto.ProductServiceMapper;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductServiceMapper productServiceMapper;
    private final ProductStockHistoryRepository productStockHistoryRepository;

    @Transactional(readOnly = true)
    public List<ProductServiceResponse.GetList> getProducts() {
        List<Product> products = productRepository.findAllByStatusNot(ProductStatus.DELETED);
        return productServiceMapper.toList(products);
    }

    @Transactional(readOnly = true)
    public ProductServiceResponse.Get getProduct(Long id) {
        Product product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + id));
        return productServiceMapper.toGet(product);
    }

    @Transactional
    public Long createProduct(ProductServiceRequest.Create request) {
        Product product = Product.builder()
                .name(request.name())
                .originalPrice(request.originalPrice())
                .price(request.price())
                .description(request.description())
                .thumbnailImageUrl(request.thumbnailImageUrl())
                .category(request.category())
                .stock(request.stock())
                .build();

        Product savedProduct = productRepository.save(product);
        return savedProduct.getId();
    }

    @Transactional
    public Long updateProduct(Long id, ProductServiceRequest.Update request) {
        Product product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + id));

        product.update(
                request.name(),
                request.originalPrice(),
                request.price(),
                request.description(),
                request.thumbnailImageUrl(),
                request.category()
        );

        return product.getId();
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + id));
        product.delete();
    }

    @Transactional
    public void adjustStock(Long id, ProductServiceRequest.AdjustStock request) {
        Product product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + id));

        int previousQuantity = product.getStock().getQuantity();

        product.adjustStock(request.quantity());

        ProductStockHistory history = new ProductStockHistory(
                product, 
                previousQuantity, 
                request.quantity(),
                StockChangeReason.valueOf(request.reason().name()),
                request.memo()
        );

        productStockHistoryRepository.save(history);
    }
}
