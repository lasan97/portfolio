package com.portfolio.backend.service.product;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockHistoryRepository;
import com.portfolio.backend.service.product.dto.ProductServiceMapper;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;

import java.math.BigDecimal;

import com.portfolio.backend.service.product.dto.StockChangeReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductServiceMapper productServiceMapper;

    @Mock
    private ProductStockHistoryRepository productStockHistoryRepository;

    @InjectMocks
    private ProductService productService;
    
    // 테스트 데이터
    private Product product1;
    private Product product2;
    private List<Product> productList;
    private ProductServiceResponse.Get productGetResponse;
    private ProductServiceResponse.List productListResponse1;
    private ProductServiceResponse.List productListResponse2;
    private List<ProductServiceResponse.List> productListResponses;
    
    @BeforeEach
    void setUp() {
        // 상품 엔티티 초기화
        product1 = createProduct(1L, "Product 1", 10000, 8000, "Description 1", "thumbnail1.jpg", ProductCategory.ELECTRONICS, 10);
        product2 = createProduct(2L, "Product 2", 20000, 15000, "Description 2", "thumbnail2.jpg", ProductCategory.CLOTHING, 20);
        productList = Arrays.asList(product1, product2);
        
        // 응답 DTO 초기화
        productGetResponse = new ProductServiceResponse.Get(
                1L, "Product 1", new Money(BigDecimal.valueOf(10000)), new Money(BigDecimal.valueOf(8000)), 
                "Description 1", "thumbnail1.jpg", ProductCategory.ELECTRONICS, 
                ProductStatus.ACTIVE, 10, 20, null, null
        );
        
        productListResponse1 = new ProductServiceResponse.List(
                1L, "Product 1", new Money(BigDecimal.valueOf(8000)), 
                "thumbnail1.jpg", ProductCategory.ELECTRONICS, ProductStatus.ACTIVE, 20
        );
        
        productListResponse2 = new ProductServiceResponse.List(
                2L, "Product 2", new Money(BigDecimal.valueOf(15000)), 
                "thumbnail2.jpg", ProductCategory.CLOTHING, ProductStatus.ACTIVE, 25
        );
        
        productListResponses = Arrays.asList(productListResponse1, productListResponse2);
    }

    @Nested
    @DisplayName("상품 목록 조회 테스트")
    class GetProductsTest {
        
        @Test
        @DisplayName("삭제되지 않은 모든 상품을 반환해야 한다")
        void shouldReturnAllNonDeletedProducts() {
            // Given
            when(productRepository.findAllByStatusNot(ProductStatus.DELETED)).thenReturn(productList);
            when(productServiceMapper.toList(productList)).thenReturn(productListResponses);

            // When
            List<ProductServiceResponse.List> result = productService.getProducts();

            // Then
            verify(productRepository).findAllByStatusNot(ProductStatus.DELETED);
            verify(productServiceMapper).toList(productList);
            assertEquals(productListResponses, result);
        }
    }

    @Nested
    @DisplayName("상품 상세 조회 테스트")
    class GetProductTest {
        
        @Test
        @DisplayName("존재하는 상품의 상세 정보를 반환해야 한다")
        void shouldReturnProductDetailWhenExists() {
            // Given
            Long productId = 1L;
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.of(product1));
            when(productServiceMapper.toGet(product1)).thenReturn(productGetResponse);

            // When
            ProductServiceResponse.Get result = productService.getProduct(productId);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            verify(productServiceMapper).toGet(product1);
            assertEquals(productGetResponse, result);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            Long productId = 999L;
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> productService.getProduct(productId)
            );
            
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            assertTrue(exception.getMessage().contains(productId.toString()));
        }
    }

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {
        
        @Test
        @DisplayName("유효한 요청으로 상품을 생성하고 ID를 반환해야 한다")
        void shouldCreateProductAndReturnId() {
            // Given
            ProductServiceRequest.Create request = new ProductServiceRequest.Create(
                    "New Product", new Money(BigDecimal.valueOf(10000)), new Money(BigDecimal.valueOf(8000)), 
                    "Description", "thumbnail.jpg", ProductCategory.ELECTRONICS, 10
            );
            
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            // When
            Long result = productService.createProduct(request);

            // Then
            verify(productRepository).save(any(Product.class));
            assertEquals(product1.getId(), result);
        }
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class UpdateProductTest {
        
        @Test
        @DisplayName("존재하는 상품을 수정하고 ID를 반환해야 한다")
        void shouldUpdateProductAndReturnId() {
            // Given
            Long productId = 1L;
            ProductServiceRequest.Update request = new ProductServiceRequest.Update(
                    "Updated Product", new Money(BigDecimal.valueOf(15000)), new Money(BigDecimal.valueOf(12000)), 
                    "Updated Description", "updated-thumbnail.jpg", ProductCategory.CLOTHING
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.of(product1));

            // When
            Long result = productService.updateProduct(productId, request);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            assertEquals(productId, result);
            
            // 상품 속성이 올바르게 업데이트되었는지 확인
            assertEquals("Updated Product", product1.getName());
            assertEquals(new Money(BigDecimal.valueOf(15000)), product1.getOriginalPrice());
            assertEquals(new Money(BigDecimal.valueOf(12000)), product1.getPrice());
            assertEquals("Updated Description", product1.getDescription());
            assertEquals("updated-thumbnail.jpg", product1.getThumbnailImageUrl());
            assertEquals(ProductCategory.CLOTHING, product1.getCategory());
        }

        @Test
        @DisplayName("존재하지 않는 상품 수정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductToUpdateNotFound() {
            // Given
            Long productId = 999L;
            ProductServiceRequest.Update request = new ProductServiceRequest.Update(
                    "Updated Product", new Money(BigDecimal.valueOf(15000)), new Money(BigDecimal.valueOf(12000)), 
                    "Updated Description", "updated-thumbnail.jpg", ProductCategory.CLOTHING
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> productService.updateProduct(productId, request)
            );
            
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            assertTrue(exception.getMessage().contains(productId.toString()));
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {
        
        @Test
        @DisplayName("존재하는 상품을 삭제 상태로 변경해야 한다")
        void shouldMarkProductAsDeleted() {
            // Given
            Long productId = 1L;
            when(productRepository.findById(productId)).thenReturn(Optional.of(product1));

            // When
            productService.deleteProduct(productId);

            // Then
            verify(productRepository).findById(productId);
            assertEquals(ProductStatus.DELETED, product1.getStatus());
        }

        @Test
        @DisplayName("존재하지 않는 상품 삭제 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductToDeleteNotFound() {
            // Given
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> productService.deleteProduct(productId)
            );
            
            verify(productRepository).findById(productId);
            assertTrue(exception.getMessage().contains(productId.toString()));
        }
    }

    @Nested
    @DisplayName("재고 조정 테스트")
    class AdjustStockTest {
        
        @Test
        @DisplayName("양수 수량으로 재고를 증가시켜야 한다")
        void shouldIncreaseStockWhenQuantityIsPositive() {
            // Given
            Long productId = 1L;
            int initialQuantity = product1.getStock().getQuantity();
            int adjustment = 5;
            
            ProductServiceRequest.AdjustStock request = new ProductServiceRequest.AdjustStock(
                    adjustment, com.portfolio.backend.service.product.dto.StockChangeReason.ADJUSTMENT, "재고 추가"
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.of(product1));

            // When
            Long result = productService.adjustStock(productId, request);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            verify(productStockHistoryRepository).save(any());
            assertEquals(productId, result);
            assertEquals(initialQuantity + adjustment, product1.getStock().getQuantity());
        }

        @Test
        @DisplayName("음수 수량으로 재고를 감소시켜야 한다")
        void shouldDecreaseStockWhenQuantityIsNegative() {
            // Given
            Long productId = 1L;
            int initialQuantity = product1.getStock().getQuantity();
            int adjustment = -5;
            
            ProductServiceRequest.AdjustStock request = new ProductServiceRequest.AdjustStock(
                    adjustment, StockChangeReason.LOSS, "재고 손실로 인한 차감"
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.of(product1));

            // When
            Long result = productService.adjustStock(productId, request);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            verify(productStockHistoryRepository).save(any());
            assertEquals(productId, result);
            assertEquals(initialQuantity + adjustment, product1.getStock().getQuantity());
        }

        @Test
        @DisplayName("존재하지 않는 상품의 재고 조정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductForStockAdjustmentNotFound() {
            // Given
            Long productId = 999L;
            ProductServiceRequest.AdjustStock request = new ProductServiceRequest.AdjustStock(
                    5, com.portfolio.backend.service.product.dto.StockChangeReason.ADJUSTMENT, "재고 추가"
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> productService.adjustStock(productId, request)
            );
            
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            assertTrue(exception.getMessage().contains(productId.toString()));
        }
    }

    // Helper method to create a Product entity
    private Product createProduct(Long id, String name, int originalPrice, int price, String description, 
                                 String thumbnailImageUrl, ProductCategory category, int stock) {
        Product product = Product.builder()
                .name(name)
                .originalPrice(new Money(BigDecimal.valueOf(originalPrice)))
                .price(new Money(BigDecimal.valueOf(price)))
                .description(description)
                .thumbnailImageUrl(thumbnailImageUrl)
                .category(category)
                .stock(stock)
                .build();

        // Use reflection to set the ID
        try {
            java.lang.reflect.Field idField = Product.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set product ID", e);
        }

        return product;
    }
}