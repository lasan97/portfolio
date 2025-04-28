package com.portfolio.backend.service.product;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.fixture.ProductTestFixtures;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockHistoryRepository;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.product.dto.ProductServiceRequest;
import com.portfolio.backend.service.product.dto.ProductServiceResponse;
import com.portfolio.backend.service.product.dto.StockChangeReason;
import com.portfolio.backend.service.product.fixture.ProductServiceRequestTestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductService 테스트")
class ProductServiceTest extends ServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockHistoryRepository productStockHistoryRepository;

    @Autowired
    private ProductService productService;

    @AfterEach
    void tearDown() {
        productStockHistoryRepository.deleteAll();
        productRepository.deleteAll();
    }
    
    @Nested
    @DisplayName("상품 목록 조회")
    class GetProductsTest {

        @Test
        @DisplayName("삭제되지 않은 모든 상품을 반환해야 한다")
        void shouldReturnAllNonDeletedProducts() {
            // Given
            productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            // When
            List<ProductServiceResponse.GetList> result = productService.getProducts();

            // Then
            assertThat(result)
                    .extracting("status")
                    .doesNotContain(ProductStatus.DELETED);
        }

        @Test
        @DisplayName("삭제된 상품은 목록에서 제외되어야 한다")
        void shouldNotIncludeDeletedProducts() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            productRepository.save(product);

            // When
            List<ProductServiceResponse.GetList> result = productService.getProducts();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProductTest {
        
        @Test
        @DisplayName("존재하는 상품의 상세 정보를 반환해야 한다")
        void shouldReturnProductDetailWhenExists() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            // When
            ProductServiceResponse.Get result = productService.getProduct(product.getId());

            // Then
            assertThat(result)
                    .extracting(
                            ProductServiceResponse.Get::id,
                            ProductServiceResponse.Get::name,
                            ProductServiceResponse.Get::originalPrice,
                            ProductServiceResponse.Get::price,
                            ProductServiceResponse.Get::description,
                            ProductServiceResponse.Get::thumbnailImageUrl,
                            ProductServiceResponse.Get::category,
                            ProductServiceResponse.Get::status,
                            ProductServiceResponse.Get::stock)
                    .containsExactly(
                            product.getId(),
                            product.getName(),
                            product.getOriginalPrice(),
                            product.getPrice(),
                            product.getDescription(),
                            product.getThumbnailImageUrl(),
                            product.getCategory(),
                            product.getStatus(),
                            product.getStock().getQuantity());
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductNotFound() {
            // When & Then
            assertThatThrownBy(() -> productService.getProduct(0L))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("삭제된 상품 조회 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductIsDeleted() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            product = productRepository.save(product);

            Long productId = product.getId();

            // When & Then
            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 생성")
    class CreateProductTest {
        
        @Test
        @DisplayName("유효한 요청으로 상품을 생성하고 ID를 반환해야 한다")
        void shouldCreateProductAndReturnId() {
            // Given
            ProductServiceRequest.Create request = ProductServiceRequestTestFixtures.createProductCreateRequest();

            // When
            Long result = productService.createProduct(request);

            // Then
            Optional<Product> response = productRepository.findById(result);

            assertThat(response).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class UpdateProductTest {
        
        @Test
        @DisplayName("존재하는 상품을 수정하고 ID를 반환해야 한다")
        void shouldUpdateProductAndReturnId() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductServiceRequest.Update request = ProductServiceRequestTestFixtures.createProductUpdateRequest();

            // When
            Long result = productService.updateProduct(product.getId(), request);

            // Then
            Product response = productRepository.findById(result).get();

            assertThat(response)
                    .extracting(
                            Product::getName,
                            Product::getOriginalPrice,
                            Product::getPrice,
                            Product::getDescription,
                            Product::getThumbnailImageUrl,
                            Product::getCategory)
                    .containsExactly(
                            request.name(),
                            request.originalPrice(),
                            request.price(),
                            request.description(),
                            request.thumbnailImageUrl(),
                            request.category());
        }
        
        @Test
        @DisplayName("존재하지 않는 상품 수정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductToUpdateNotFound() {
            // Given
            Long productId = 0L;
            ProductServiceRequest.Update request = ProductServiceRequestTestFixtures.createProductUpdateRequest();

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("삭제된 상품 수정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductIsDeleted() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            product = productRepository.save(product);

            Long productId = product.getId();

            ProductServiceRequest.Update request = ProductServiceRequestTestFixtures.createProductUpdateRequest();

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(productId, request))
                    .isInstanceOf(ResourceNotFoundException.class);

        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class DeleteProductTest {
        
        @Test
        @DisplayName("존재하는 상품을 삭제 상태로 변경해야 한다")
        void shouldMarkProductAsDeleted() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            // When
            productService.deleteProduct(product.getId());

            // Then
            Product response = productRepository.findById(product.getId()).get();
            assertThat(response.getStatus()).isEqualTo(ProductStatus.DELETED);
        }

        @Test
        @DisplayName("존재하지 않는 상품 삭제 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductToDeleteNotFound() {
            // Given
            Long productId = 0L;

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("삭제된 상품 삭제 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductIsDeleted() {
            // Given
            Product product = ProductTestFixtures.createDefaultProduct(10);
            product.delete();
            product = productRepository.save(product);

            Long productId = product.getId();

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(productId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("재고 조정")
    class AdjustStockTest {

        @ParameterizedTest
        @CsvSource({
            "15, '재고 추가'",
            "5, '재고 감소'"
        })
        @DisplayName("재고를 지정한 수량으로 조정해야 한다")
        void shouldAdjustStockBySpecifiedAmount(int adjustment, String description) {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductServiceRequest.AdjustStock request = ProductServiceRequestTestFixtures.createStockAdjustRequest(
                    adjustment, StockChangeReason.ADJUSTMENT, description
            );

            // When
            productService.adjustStock(product.getId(), request);

            // Then
            Product response = productRepository.findById(product.getId()).get();

            assertThat(response.getStock().getQuantity()).isEqualTo(adjustment);
        }

        @Test
        @DisplayName("재고 수량은 음수로 조정할 수 없다")
        void shouldThrowExceptionWhenAdjustingStockToNegative() {
            // Given
            Product product = productRepository.save(ProductTestFixtures.createDefaultProduct(10));

            ProductServiceRequest.AdjustStock request = ProductServiceRequestTestFixtures
                    .createStockAdjustRequest(-5);

            // When & Then
            assertThatThrownBy(() -> productService.adjustStock(product.getId(), request))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("재고는 0보다 작을 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 상품의 재고 조정 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenProductForStockAdjustmentNotFound() {
            // Given
            Long productId = 0L;

            ProductServiceRequest.AdjustStock request = ProductServiceRequestTestFixtures
                    .createStockAdjustRequest(-5);

            // When & Then
            assertThatThrownBy(() -> productService.adjustStock(productId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}