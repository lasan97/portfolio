# Portfolio 백엔드 프로젝트

이 문서는 Portfolio 백엔드 프로젝트의 구조와 주요 기능에 대한 안내를 제공합니다.

## 프로젝트 개요

이 프로젝트는 포트폴리오 웹 애플리케이션의 백엔드 부분으로, Spring Boot를 기반으로 구축되었습니다. 사용자 인증(JWT 및 OAuth2), 사용자 관리 등의 기능을 제공합니다.

## 기술 스택

- Java 17
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- JWT (JSON Web Token)
- OAuth2 (GitHub)
- H2 Database (개발용)
- Lombok

## 프로젝트 구조

```
backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── portfolio
│   │   │           └── backend
│   │   │               ├── BackendApplication.java
│   │   │               ├── common
│   │   │               │   ├── config
│   │   │               │   │   └── SecurityConfig.java
│   │   │               │   ├── exception
│   │   │               │   │   ├── ErrorResponse.java
│   │   │               │   │   ├── GlobalExceptionHandler.java
│   │   │               │   │   └── ResourceNotFoundException.java
│   │   │               │   └── security
│   │   │               │       ├── UserDetailsImpl.java
│   │   │               │       ├── jwt
│   │   │               │       │   ├── JwtAuthenticationFilter.java
│   │   │               │       │   └── JwtTokenProvider.java
│   │   │               │       └── oauth2
│   │   │               │           ├── CustomOAuth2UserService.java
│   │   │               │           ├── OAuth2AuthenticationSuccessHandler.java
│   │   │               │           └── userinfo
│   │   │               └── domain
│   │   │                   ├── auth
│   │   │                   │   ├── controller
│   │   │                   │   │   └── AuthController.java
│   │   │                   │   └── dto
│   │   │                   │       └── TokenResponseDto.java
│   │   │                   └── user
│   │   │                       ├── controller
│   │   │                       │   └── UserController.java
│   │   │                       ├── dto
│   │   │                       │   └── UserDto.java
│   │   │                       ├── entity
│   │   │                       │   └── User.java
│   │   │                       ├── repository
│   │   │                       │   └── UserRepository.java
│   │   │                       └── service
│   │   │                           └── UserService.java
│   │   └── resources
│   │       └── application.yml
│   └── test
└── build.gradle.kts
```

## 주요 컴포넌트

### 1. 인증 (Authentication)

#### JWT 인증

- `JwtTokenProvider`: JWT 토큰 생성, 검증, 사용자 인증 정보 추출
- `JwtAuthenticationFilter`: HTTP 요청에서 JWT 토큰을 추출하고 사용자 인증

#### OAuth2 인증 (GitHub)

- `CustomOAuth2UserService`: OAuth2 로그인 처리 및 사용자 정보 로드
- `OAuth2AuthenticationSuccessHandler`: OAuth2 로그인 성공 시 처리 (JWT 토큰 생성 등)

### 2. 사용자 관리

- `UserController`: 사용자 관련 API 엔드포인트
- `UserService`: 사용자 관련 비즈니스 로직
- `UserRepository`: 사용자 데이터 접근 계층

### 3. 예외 처리

- `GlobalExceptionHandler`: 애플리케이션 전체 예외 처리
- `ResourceNotFoundException`: 리소스를 찾을 수 없을 때 발생하는 예외
- `ErrorResponse`: 클라이언트에 반환할 에러 응답 형식

## 설정

### 환경 변수

다음 환경 변수를 `.env` 파일이나 시스템 환경 변수로 설정해야 합니다:

- `GITHUB_CLIENT_ID`: GitHub OAuth 앱 클라이언트 ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth 앱 클라이언트 시크릿
- `JWT_SECRET`: JWT 서명에 사용할 시크릿 키 (최소 32바이트)

### application.yml

주요 설정은 `application.yml`에서 관리됩니다:

```yaml
jwt:
  secret: ${JWT_SECRET:12345678901234567890123456789012}
  expiration: 86400000 # 24시간

spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
```

## API 엔드포인트

### 인증 API

- `POST /api/auth/login`: 이메일/비밀번호 로그인
- `GET /oauth2/callback`: GitHub OAuth2 콜백 URL
- `GET /api/auth/me`: 현재 인증된 사용자 정보

### 사용자 API

- `GET /api/users`: 모든 사용자 조회
- `GET /api/users/{id}`: 특정 사용자 조회
- `POST /api/users`: 새 사용자 생성
- `PUT /api/users/{id}`: 사용자 정보 수정
- `DELETE /api/users/{id}`: 사용자 삭제

## JWT 인증 관련 주의사항

1. JWT 라이브러리는 0.12.3 버전을 사용하고 있으며, 해당 버전에서는 이전 버전과 API가 크게 변경되었습니다.
2. 토큰 생성 시 최신 JJWT API를 사용합니다:
   - `subject()`, `claims()`, `issuedAt()`, `expiration()` 메소드 사용
   - `java.util.Date` 대신 `java.time.Instant` 사용
   - `signWith(key, Jwts.SIG.HS256)` 형식으로 서명
3. 토큰 파싱 시 `Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token)` 형식으로 사용합니다.

## 빌드 및 실행

### 필요 조건

- JDK 17 이상
- Gradle

### 빌드 방법

```bash
./gradlew build
```

### 실행 방법

```bash
./gradlew bootRun
```

또는 JAR 파일로 직접 실행:

```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### 도커로 실행

```bash
docker build -t portfolio-backend .
docker run -p 8080:8080 --env-file .env portfolio-backend
```

## 문제 해결

### 일반적인 문제

1. **JWT 관련 오류**:
   - JWT 라이브러리 버전을 확인하세요 (0.12.3 사용 중).
   - JWT 시크릿 키가 충분히 길고 올바르게 설정되었는지 확인하세요.

2. **OAuth2 인증 오류**:
   - GitHub OAuth 앱 설정에서 콜백 URL이 올바르게 설정되었는지 확인하세요.
   - 환경 변수 `GITHUB_CLIENT_ID`와 `GITHUB_CLIENT_SECRET`이 올바르게 설정되었는지 확인하세요.

3. **H2 데이터베이스 접근**:
   - H2 콘솔은 `http://localhost:8080/h2-console`에서 접근할 수 있습니다.
   - JDBC URL은 `jdbc:h2:mem:testdb`로 설정해야 합니다.
