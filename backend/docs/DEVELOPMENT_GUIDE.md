# 포트폴리오 백엔드 개발 가이드

## 프로젝트 아키텍처

이 프로젝트는 다음과 같은 계층형 아키텍처를 따릅니다:

1. **컨트롤러 계층 (Controller)**
   - REST API 엔드포인트 정의
   - 요청 검증 및 라우팅
   - `@Controller`, `@RestController` 어노테이션 사용

2. **서비스 계층 (Service)**
   - 비즈니스 로직 처리
   - 트랜잭션 관리
   - `@Service` 어노테이션 사용

3. **도메인 계층 (Domain)**
   - 엔티티 정의 및 관리
   - 값 객체(Value Object) 관리
   - 레포지토리 인터페이스 정의

4. **공통 계층 (Common)**
   - 보안 설정
   - 예외 처리
   - 공통 유틸리티

## 주요 도메인 모델

### 사용자 (User)
- OAuth2를 통한 인증 (GitHub)
- 역할 기반 접근 제어 (ROLE_USER, ROLE_ADMIN)
- 사용자 프로필 정보 관리

### 자기소개 (Introduction)
- 포트폴리오 컨텐츠 관리
- 외부 링크(ExternalLink) 값 객체 포함
- 생성/수정 기록 추적

## 개발 가이드라인

### 1. 도메인 모델 확장

새로운 도메인 개념이 필요할 경우 다음 구조를 따릅니다:

```
backend/src/main/java/com/portfolio/backend/domain/{도메인명}/
├── entity/            # 엔티티 클래스
├── repository/        # 레포지토리 인터페이스
└── value/             # 값 객체 (필요시)
```

모든 엔티티는 적절한 유효성 검증 로직을 포함해야 합니다.

### 2. API 엔드포인트 개발

새로운 API 추가 시 다음 단계를 따릅니다:

1. 컨트롤러 클래스 작성:
   ```java
   @RestController
   @RequestMapping("/api/{리소스명}")
   public class 리소스Controller {
       // 엔드포인트 정의
   }
   ```

2. 서비스 계층 인터페이스 및 구현체 작성
3. DTO 객체 정의 (Request/Response)
4. 필요한 예외 처리 추가
5. HTTP 상태 코드
- `200 OK`: 요청이 성공적으로 처리됨
- `201 Created`: 리소스가 생성됨
- `400 Bad Request`: 유효하지 않은 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스를 찾을 수 없음
- `422 Unprocessable Entity`: 요청은 유효하지만 처리할 수 없음
- `500 Internal Server Error`: 서버 오류


### 3. 보안 설정

모든 API는 적절한 보안 수준을 설정해야 합니다:

- 공개 API: `permitAll()`
- 인증 필요 API: `authenticated()`
- 특정 역할 필요 API: `@PreAuthorize("hasRole('ROLE_NAME')")`

### 4. DTO 구조

DTO 클래스는 불변 객체(record)로 설계합니다:

```java
public record SampleRequest(
    @NotBlank String name,
    @NotNull Integer age
) {}
```

### 5. 예외 처리

도메인 예외는 다음과 같이 분류됩니다:

- `DomainException`: 도메인 로직 위반 시
- `ResourceNotFoundException`: 리소스 미존재 시
- `UnprocessableEntityException`: 요청 처리 불가 시

모든 예외는 `GlobalExceptionHandler`에서 적절한 HTTP 상태 코드와 함께 처리됩니다.

## 패키지 구조 가이드

### 새로운 기능 개발 시 패키지 구조

```
com.portfolio.backend
├── controller
│   └── {feature}
│       └── {Feature}Controller.java
├── service
│   └── {feature}
│       ├── {Feature}Service.java
│       └── dto
│           ├── {Feature}ServiceRequest.java
│           └── {Feature}ServiceResponse.java
└── domain
    └── {feature}
        ├── entity
        │   └── {Feature}.java
        ├── repository
        │   └── {Feature}Repository.java
        └── value
            └── {Value}.java
```

## 데이터베이스 접근

- JPA 레포지토리 인터페이스 활용
- 복잡한 쿼리는 JPQL 또는 QueryDSL 사용 권장
- N+1 문제 방지를 위한 적절한 Fetch 전략 사용

## OAuth2 인증

현재 GitHub OAuth2 인증이 구현되어 있습니다.

새로운 OAuth2 제공자 추가 시:

1. `application.yml`에 새 OAuth2 클라이언트 등록
2. `Oauth2ProviderType` enum에 새 제공자 추가
3. OAuth2Service에 제공자별 로직 추가

## 테스트 작성 가이드

테스트 코드는 다음 계층별로 작성합니다:

1. 단위 테스트: 서비스 및 컨트롤러의 독립적 기능 검증
2. 통합 테스트: 여러 컴포넌트의 상호작용 검증
3. 레포지토리 테스트: 데이터 접근 로직 검증

## 빌드 및 배포

### 로컬 개발 환경

```bash
# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

### 도커 배포

```bash
# 도커 이미지 빌드
docker build -t portfolio-backend .

# 컨테이너 실행
docker run -p 8081:8081 -e JWT_SECRET=your_secret -e GITHUB_CLIENT_ID=your_id -e GITHUB_CLIENT_SECRET=your_secret portfolio-backend
```

## 환경 변수

필수 환경 변수:
- `JWT_SECRET`: JWT 토큰 서명에 사용되는 시크릿 키 (32자 이상 권장)
- `GITHUB_CLIENT_ID`: GitHub OAuth 앱 클라이언트 ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth 앱 클라이언트 시크릿

## API 문서화

새로운 API 추가 시 이 문서에 API 명세를 추가하는 것을 권장합니다.

## 코드 스타일 가이드

- 코드 작성 시 Lombok 활용 권장
- 모든 도메인 엔티티에 적절한 validation 로직 구현
- 불변성을 유지하기 위해 record와 final 적극 활용
