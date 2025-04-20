# 테스트 작성 가이드

## 개요
본 문서는 서비스 계층 테스트 작성에 대한 가이드를 제공합니다. Given-When-Then 패턴을 사용하여 테스트 코드의 가독성과 유지보수성을 높입니다.

## 테스트 구조

### 1. 기본 구조
```java
@Test
@DisplayName("테스트 설명")
void testMethod() {
    // Given
    // 테스트를 위한 사전 조건 설정
    
    // When
    // 테스트할 동작 실행
    
    // Then
    // 결과 검증
}
```

### 2. 목 객체 활용
- 테스트할 클래스와 상호작용하는 의존성은 모두 모킹하세요.
- 검증 대상이 되는 객체가 모킹 객체인지 확인하세요.

```java
// 잘못된 예: 실제 객체 사용
ProductCart productCart = new ProductCart(user);
verify(productCart).addItem(...); // 오류 발생

// 올바른 예: 모킹 객체 사용
ProductCart productCart = mock(ProductCart.class);
verify(productCart).addItem(...); // 정상 작동
```

### 3. ArgumentCaptor 활용
메서드 호출 시 전달되는 인자를 검증할 때는 ArgumentCaptor를 사용하세요.

```java
ArgumentCaptor<ProductCartItem> captor = ArgumentCaptor.forClass(ProductCartItem.class);
verify(productCart).addItem(captor.capture());
assertEquals(product, captor.getValue().getProduct());
```

## 모범 사례
- 테스트당 하나의 동작만 검증하세요.
- 불필요한 설정을 최소화하고 테스트 관련 설정만 포함하세요.
- 중첩 클래스를 활용해 테스트를 논리적으로 그룹화하세요.
