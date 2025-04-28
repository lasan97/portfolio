# 테스트 작성 가이드

## 개요

본 문서는 테스트 코드 작성에 대한 가이드라인을 제공합니다. 일관된 테스트 작성 방식을 통해 코드의 가독성과 유지보수성을 높이는 것을 목표로 합니다.

본 프로젝트의 백엔드 테스트 구조와 작성법에 대해 구체적인 예시를 들어 설명합니다.

## 테스트 구조 원칙

### 1. Given-When-Then 패턴

모든 테스트 코드는 다음의 Given-When-Then 패턴을 따릅니다:

```java
@Test
@DisplayName("테스트가 검증하려는 동작에 대한 명확한 설명")
void testMethodName() {
    // Given - 테스트를 위한 사전 조건 설정
    
    // When - 테스트할 동작 실행
    
    // Then - 결과 검증
}
```

이 패턴은 테스트 코드의 가독성과 명확성을 크게 향상시킵니다.

### 메소드 네이밍 컨벤션
테스트 메소드 이름은 `should + [기대하는 결과]` 형식을 사용하여 의도를 명확히 합니다.

예:
```java
void shouldReturnActiveProductsWhenStatusIsActive()
void shouldThrowExceptionWhenProductNotFound()
void shouldUpdatePriceWhenProductExists()
```

### 테스트 그룹화
비슷한 기능 또는 행위를 그룹화하여 관리하기 위해 `@Nested` 클래스를 적극 활용합니다.

예:
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

## 백엔드 테스트 주요 구조 및 패턴

### 1. 테스트 프로필 및 환경 설정
각 테스트는 기본적으로 @SpringBootTest 및 @ActiveProfiles("test") 어노테이션을 사용하여 독립된 테스트용 환경에서 실행됩니다.

### 2. 공통 Base 클래스 활용
백엔드 테스트는 서비스 레이어 테스트용 ServiceTest 클래스와 컨트롤러 레이어 테스트용 ControllerTest 클래스를 공통 베이스로 사용합니다.

- ServiceTest는 테스트용 S3Client를 @MockBean으로 등록하고, UserRepository를 통해 기본 테스트용 사용자(adminUser, user)를 생성하여 제공합니다.

- ControllerTest 또한 WebApplicationContext 및 MockMvc를 설정하여 웹 계층 테스트를 위한 기반을 구축합니다. UTF-8 문자 인코딩 필터와 Spring Security를 적용하는 등 실제 서비스 환경과 유사한 구성을 사용합니다.

### 3. 도메인 레이어 테스트 사례
- 도메인 엔티티 테스트는 주로 데이터 생성, 필드 유효성 검사, 비즈니스 로직 검증, 도메인 이벤트 발생 여부 등을 검증합니다.
- 예를 들어 UserTest에서는 정상적인 User 객체 생성, 필수 필드 누락 시 예외 발생 여부를 확인합니다.
- `@DisplayName`과 `@Nested`를 이용해 생성 시나리오, 예외 시나리오 등 테스트 케이스를 구분합니다.

### 4. 서비스 레이어 테스트
- ServiceTest를 상속받아 개별 서비스 테스트 클래스를 작성합니다.
- 실제 비즈니스 로직 실행과 결과 검증에 집중하며, 외부 서비스 호출(S3Client) 등은 Mockito를 통해 모킹 처리합니다.

### 5. 컨트롤러 테스트
- ControllerTest를 상속받아 MockMvc를 활용해 HTTP 요청/응답 테스트를 수행합니다.
- 인증/인가가 필요한 경우 @WithUserDetails, @WithAnonymousUser 등의 어노테이션을 사용해 보안 관련 상황도 테스트합니다.
- 예: `UserControllerTest`에서 로그인된 사용자 정보 조회 API 테스트

### 6. 통합/연동 테스트
- 외부 연동(예: AWS S3) 관련 기능은 S3StorageServiceTest에서처럼 Mockito 기반으로 인터페이스를 모킹하거나, 실제 인터페이스를 흉내내어 동작을 검증합니다.
- 파일 업로드/삭제 요청 시 생성되는 요청 오브젝트 검증과 반환 결과 URL 등을 확인합니다.

## 테스트 실행 방법

### Gradle 명령어 사용

```
./gradlew test
```

로 모든 테스트를 실행할 수 있습니다.

### 특정 테스트 실행
IDE 혹은 커맨드라인에서 개별 클래스 또는 메소드 단위로 실행 가능합니다.

예:
```
./gradlew test --tests com.portfolio.backend.domain.user.entity.UserTest
```

## 요약

- 백엔드 테스트는 명확하고 일관된 Given-When-Then 구조를 따릅니다.
- 테스트 이름은 should... 형식을 권장합니다.
- 기능별 그룹화는 @Nested와 @DisplayName을 적극 활용합니다.
- 서비스와 컨트롤러 테스트는 공통 베이스 클래스를 상속해 환경 설정을 재사용합니다.
- Mockito 기반 모킹과 Spring Security 테스트 유틸을 적절히 사용합니다.
- 실제 코드베이스의 다양한 예시를 참고해 작성하면 무리 없이 확장 가능합니다.