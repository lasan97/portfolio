# 테스트 작성 가이드

## 개요
본 문서는 테스트 코드 작성에 대한 가이드라인을 제공합니다. 일관된 테스트 작성 방식을 통해 코드의 가독성과 유지보수성을 높이는 것을 목표로 합니다.

## 테스트 구조 원칙

### 1. Given-When-Then 패턴
모든 테스트 코드는 다음 패턴을 따릅니다:

```java
@Test
@DisplayName("테스트가 검증하려는 동작에 대한 명확한 설명")
void testMethodName() {
    // Given - 테스트를 위한 사전 조건 설정
    
    // When - 테스트할 동작 실행
    
    // Then - 결과 검증
}
```

이 패턴은 테스트 코드의 가독성을 높이고, 각 테스트가 무엇을 검증하는지 명확하게 합니다.

### 2. 테스트 메소드 명명 규칙
테스트 메소드의 이름은 `should + [기대하는 결과]` 형식을 사용합니다:

```java
void shouldReturnActiveProductsWhenStatusIsActive()
void shouldThrowExceptionWhenProductNotFound()
void shouldUpdatePriceWhenProductExists()
```

이러한 네이밍 규칙은 테스트의 의도를 명확하게 전달합니다.

### 3. 테스트 그룹화
관련된 테스트는 `@Nested` 어노테이션을 사용하여 그룹화합니다:

```java
@Nested
@DisplayName("상품 검색 기능")
class ProductSearchTest {
    @Test
    @DisplayName("이름으로 상품을 검색할 수 있다")
    void shouldFindProductsByName() {
        // ...
    }
    
    @Test
    @DisplayName("카테고리로 상품을 검색할 수 있다")
    void shouldFindProductsByCategory() {
        // ...
    }
}
```

## 테스트 레이어별 가이드

### 1. 단위 테스트 (Unit Tests)
- 클래스나 메소드 단위의 기능을 독립적으로 테스트합니다.
- 외부 의존성은 모두 Mock 객체로 대체합니다.
- JUnit 5와 Mockito를 사용합니다.
- AssertJ를 사용하여 가독성 높은 검증문을 작성합니다.

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    void shouldReturnProductWhenIdExists() {
        // Given
        Long productId = 1L;
        Product expectedProduct = TestFixtures.createDefaultProduct();
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));
        
        // When
        Product result = productService.getProduct(productId);
        
        // Then
        assertThat(result).isEqualTo(expectedProduct);
    }
}
```

### 2. 통합 테스트 (Integration Tests)
- 여러 구성 요소 간의 상호작용을 테스트합니다.
- 실제 의존성을 사용하거나 테스트용 구현체로 대체합니다.
- `@SpringBootTest` 또는 `@DataJpaTest` 등의 어노테이션을 사용합니다.

```java
@DataJpaTest
class ProductRepositoryIntegrationTest {
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void shouldSaveAndFindProduct() {
        // Given
        Product product = TestFixtures.createDefaultProduct();
        
        // When
        productRepository.save(product);
        Optional<Product> foundProduct = productRepository.findById(product.getId());
        
        // Then
        assertThat(foundProduct).isPresent()
            .get().satisfies(p -> {
                assertThat(p.getName()).isEqualTo(product.getName());
                assertThat(p.getPrice()).isEqualTo(product.getPrice());
            });
    }
}
```

### 3. 웹 레이어 테스트 (Web Layer Tests)
- 컨트롤러 및 API 엔드포인트를 테스트합니다.
- `@WebMvcTest`를 사용하여 웹 레이어만 테스트합니다.
- 서비스 레이어는 Mock으로 대체합니다.
- MockMvc를 사용하여 HTTP 요청과 응답을 테스트합니다.

```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProductService productService;
    
    @Test
    void shouldReturnProductWhenGetById() throws Exception {
        // Given
        Long productId = 1L;
        ProductDTO productDTO = TestFixtures.createProductDTO();
        when(productService.getProductDTO(productId)).thenReturn(productDTO);
        
        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value(productDTO.getName()));
    }
}
```

## 테스트 가독성 향상 방법

### 1. 테스트 데이터 중앙화
TestFixtures 클래스를 사용하여 테스트 데이터를 중앙화합니다:

```java
public class TestFixtures {
    public static Product createDefaultProduct() {
        return Product.builder()
                .name("맥북 프로")
                .originalPrice(new Money(BigDecimal.valueOf(2000000)))
                .price(new Money(BigDecimal.valueOf(1800000)))
                .description("Apple 맥북 프로 M3 칩")
                .thumbnailImageUrl("macbook-pro.jpg")
                .category(ProductCategory.ELECTRONICS)
                .stock(10)
                .build();
    }
    
    // 더 많은 테스트 데이터 생성 메소드...
}
```

### 2. 헬퍼 메소드 활용
반복되는 검증 로직을 헬퍼 메소드로 분리합니다:

```java
private void assertProductEquals(Product actual, Product expected) {
    assertThat(actual.getName()).isEqualTo(expected.getName());
    assertThat(actual.getPrice()).isEqualTo(expected.getPrice());
    assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
    // ...
}
```

### 3. 파라미터화된 테스트
여러 입력값으로 동일한 로직을 테스트할 때는 파라미터화된 테스트를 사용합니다:

```java
@ParameterizedTest
@CsvSource({
    "10000, 9000, 10",  // 10% 할인
    "10000, 8000, 20",  // 20% 할인
    "10000, 7500, 25"   // 25% 할인
})
void shouldCalculateCorrectDiscountRate(int originalPrice, int price, int expectedDiscountRate) {
    // Given
    Product product = createProductWithPrices(originalPrice, price);
    
    // When
    int discountRate = product.getDiscountRate();
    
    // Then
    assertThat(discountRate).isEqualTo(expectedDiscountRate);
}
```

## 모범 사례

1. **테스트 독립성**: 각 테스트는 다른 테스트에 의존하지 않고 독립적으로 실행 가능해야 합니다.

2. **한 가지만 테스트**: 각 테스트는 한 가지 동작 또는 조건만 검증해야 합니다.

3. **충분한 테스트 커버리지**: 핵심 비즈니스 로직에 대해 충분한 테스트 케이스를 작성합니다.

4. **테스트 가독성**: 테스트 코드는 문서의 역할도 합니다. 가독성 높은 테스트 코드를 작성합니다.

5. **경계값 테스트**: 최소값, 최대값, null 값 등 경계 조건에 대한 테스트를 작성합니다.

6. **예외 테스트**: 예외 상황에 대한 테스트도 반드시 작성합니다.

7. **실패하는 테스트 먼저 작성**: TDD(Test-Driven Development) 방식으로 테스트를 먼저 작성하는 것을 권장합니다.

## 도구 및 라이브러리

- **JUnit 5**: 테스트 프레임워크
- **Mockito**: 모킹 라이브러리
- **AssertJ**: 가독성 높은 검증문 작성을 위한 라이브러리
- **Spring Test**: 스프링 애플리케이션 테스트를 위한 라이브러리

## 결론

좋은 테스트 코드는 애플리케이션의 품질을 보장하고 유지보수를 용이하게 합니다. 이 가이드라인을 따라 일관되고 가독성 높은 테스트 코드를 작성해주세요.
