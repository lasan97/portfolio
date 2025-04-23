package com.portfolio.backend.service.product;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.exception.DomainException;
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
import com.portfolio.backend.service.product.dto.StockChangeReason;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ProductService에 대한 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductServiceMapper productServiceMapper;

    @Mock
    private ProductStockHistoryRepository productStockHistoryRepository;

    @InjectMocks
    private ProductService productService;
    
    @Nested
    @DisplayName("상품 목록 조회")
    class GetProductsTest {
        
        @Test
        @DisplayName("삭제되지 않은 모든 상품을 반환해야 한다")
        void shouldReturnAllNonDeletedProducts() {
            // Given
            List<Product> productList = TestFixtures.createProductList();
            List<ProductServiceResponse.GetList> expectedResponses = TestFixtures.createProductGetListResponseList();
            
            when(productRepository.findAllByStatusNot(ProductStatus.DELETED)).thenReturn(productList);
            when(productServiceMapper.toList(productList)).thenReturn(expectedResponses);

            // When
            List<ProductServiceResponse.GetList> result = productService.getProducts();

            // Then
            verify(productRepository).findAllByStatusNot(ProductStatus.DELETED);
            verify(productServiceMapper).toList(productList);
            
            assertThat(result)
                .isEqualTo(expectedResponses)
                .hasSize(2);
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProductTest {
        
        @Test
        @DisplayName("존재하는 상품의 상세 정보를 반환해야 한다")
        void shouldReturnProductDetailWhenExists() {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            Product product = TestFixtures.createDefaultProduct();
            ProductServiceResponse.Get expectedResponse = TestFixtures.createProductGetResponse();
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));
            when(productServiceMapper.toGet(product)).thenReturn(expectedResponse);

            // When
            ProductServiceResponse.Get result = productService.getProduct(productId);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            verify(productServiceMapper).toGet(product);
            
            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            Long productId = 999L;
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
            
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("상품 생성")
    class CreateProductTest {
        
        @Test
        @DisplayName("유효한 요청으로 상품을 생성하고 ID를 반환해야 한다")
        void shouldCreateProductAndReturnId() {
            // Given
            ProductServiceRequest.Create request = TestFixtures.createProductCreateRequest();
            Product savedProduct = TestFixtures.createDefaultProduct();
            
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // When
            Long result = productService.createProduct(request);

            // Then
            verify(productRepository).save(any(Product.class));
            
            assertThat(result).isEqualTo(savedProduct.getId());
        }
        
        @Test
        @DisplayName("상품 생성 시 올바른 값으로 저장되어야 한다")
        void shouldSaveProductWithCorrectValues() {
            // Given
            ProductServiceRequest.Create request = TestFixtures.createProductCreateRequest();
            
            // 저장된 Product 객체를 캡처하기 위한 ArgumentCaptor
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            
            when(productRepository.save(productCaptor.capture())).thenReturn(TestFixtures.createDefaultProduct());

            // When
            productService.createProduct(request);

            // Then
            Product capturedProduct = productCaptor.getValue();
            
            assertThat(capturedProduct.getName()).isEqualTo(request.name());
            assertThat(capturedProduct.getOriginalPrice()).isEqualTo(request.originalPrice());
            assertThat(capturedProduct.getPrice()).isEqualTo(request.price());
            assertThat(capturedProduct.getDescription()).isEqualTo(request.description());
            assertThat(capturedProduct.getThumbnailImageUrl()).isEqualTo(request.thumbnailImageUrl());
            assertThat(capturedProduct.getCategory()).isEqualTo(request.category());
            assertThat(capturedProduct.getStock().getQuantity()).isEqualTo(request.stock());
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class UpdateProductTest {
        
        @Test
        @DisplayName("존재하는 상품을 수정하고 ID를 반환해야 한다")
        void shouldUpdateProductAndReturnId() {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            Product product = TestFixtures.createDefaultProduct();
            ProductServiceRequest.Update request = TestFixtures.createProductUpdateRequest();
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));

            // When
            Long result = productService.updateProduct(productId, request);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            
            assertThat(result).isEqualTo(productId);
            
            // 상품 속성이 올바르게 업데이트되었는지 개별적으로 검증
            assertProductUpdated(product, request);
        }
        
        @Test
        @DisplayName("존재하지 않는 상품 수정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductToUpdateNotFound() {
            // Given
            Long productId = 999L;
            ProductServiceRequest.Update request = TestFixtures.createProductUpdateRequest();
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
            
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
        }
        
        // 상품 업데이트 검증을 위한 헬퍼 메소드
        private void assertProductUpdated(Product product, ProductServiceRequest.Update request) {
            assertThat(product.getName()).isEqualTo(request.name());
            assertThat(product.getOriginalPrice()).isEqualTo(request.originalPrice());
            assertThat(product.getPrice()).isEqualTo(request.price());
            assertThat(product.getDescription()).isEqualTo(request.description());
            assertThat(product.getThumbnailImageUrl()).isEqualTo(request.thumbnailImageUrl());
            assertThat(product.getCategory()).isEqualTo(request.category());
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class DeleteProductTest {
        
        @Test
        @DisplayName("존재하는 상품을 삭제 상태로 변경해야 한다")
        void shouldMarkProductAsDeleted() {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            Product product = TestFixtures.createDefaultProduct();
            
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // When
            productService.deleteProduct(productId);

            // Then
            verify(productRepository).findById(productId);
            
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        }

        @Test
        @DisplayName("존재하지 않는 상품 삭제 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductToDeleteNotFound() {
            // Given
            Long productId = 999L;
            
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
            
            verify(productRepository).findById(productId);
        }
    }

    @Nested
    @DisplayName("재고 조정")
    class AdjustStockTest {

        @ParameterizedTest
        @CsvSource({
            "5, '재고 추가'",
            "0, '재고 감소'"
        })
        @DisplayName("재고를 지정한 수량으로 조정해야 한다")
        void shouldAdjustStockBySpecifiedAmount(int adjustment, String description) {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            Product product = TestFixtures.createDefaultProduct();
            
            ProductServiceRequest.AdjustStock request = new ProductServiceRequest.AdjustStock(
                    adjustment, StockChangeReason.ADJUSTMENT, description
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));

            // When
            productService.adjustStock(productId, request);

            // Then
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            verify(productStockHistoryRepository).save(any());
            
            assertThat(product.getStock().getQuantity()).isEqualTo(adjustment);
        }

        @Test
        @DisplayName("재고 수량은 음수로 조정할 수 없다")
        void shouldThrowExceptionWhenAdjustingStockToNegative() {
            // Given
            Long productId = TestFixtures.PRODUCT_ID_1;
            Product product = TestFixtures.createDefaultProduct();
            ProductServiceRequest.AdjustStock request = new ProductServiceRequest.AdjustStock(
                    -5, StockChangeReason.ADJUSTMENT, "재고 감소"
            );

            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                    .thenReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> productService.adjustStock(productId, request))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("quantity는 0보다 작을 수 없습니다.");

            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
            verifyNoInteractions(productStockHistoryRepository);
        }

        @Test
        @DisplayName("존재하지 않는 상품의 재고 조정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductForStockAdjustmentNotFound() {
            // Given
            Long productId = 999L;
            ProductServiceRequest.AdjustStock request = TestFixtures.createStockAdjustRequest(
                    5, StockChangeReason.ADJUSTMENT, "재고 추가"
            );
            
            when(productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.adjustStock(productId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
            
            verify(productRepository).findByIdAndStatusNot(productId, ProductStatus.DELETED);
        }
    }
}