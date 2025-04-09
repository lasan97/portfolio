# Portfolio 백엔드 프로젝트

이 문서는 Portfolio 백엔드 프로젝트의 구조와 주요 기능에 대한 가이드를 제공합니다.

## 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [주요 컴포넌트](#주요-컴포넌트)
5. [설정 가이드](#설정-가이드)
6. [API 엔드포인트](#api-엔드포인트)
7. [보안 관련 사항](#보안-관련-사항)
8. [빌드 및 실행](#빌드-및-실행)
9. [문제 해결](#문제-해결)
10. [OOP 및 유지보수성](#oop-및-유지보수성)

## 프로젝트 개요

이 프로젝트는 포트폴리오 웹 애플리케이션을 위한 백엔드 서버로, Spring Boot를 기반으로 개발되었습니다. 사용자 인증(JWT 및 GitHub OAuth2), 사용자 관리 등의 기능을 제공하며, RESTful API 아키텍처를 따릅니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.2.5**
  - Spring Security
  - Spring Data JPA
  - Spring Web
- **인증 및 권한**
  - JWT (JSON Web Token)
  - OAuth2 (GitHub)
- **데이터베이스**
  - H2 Database (개발용)
- **개발 도구**
  - Lombok
  - Gradle
- **API 문서화**
  - SpringDoc OpenAPI (Swagger)

## 프로젝트 구조

이 프로젝트는 계층형 아키텍처를 기반으로 관심사 분리와 모듈화를 고려하여 설계되었습니다. 주요 패키지 구조는 다음과 같습니다:

```
com.portfolio.backend
├── controller    # 클라이언트 요청 처리 및 응답 반환
├── service       # 비즈니스 로직 처리
├── domain        # 도메인 모델 및 리포지토리
├── common        # 공통 기능 및 유틸리티
│   ├── config    # 애플리케이션 설정
│   ├── security  # 보안 관련 컴포넌트
│   ├── exception # 예외 처리
│   └── integration # 외부 시스템 연동
```

각 패키지는 기능적으로 관련된 클래스들을 그룹화하여 코드의 응집도를 높이고 의존성을 최소화하는 방향으로 구성되었습니다.

## 주요 컴포넌트

이 프로젝트는 다음과 같은 주요 컴포넌트들로 구성되어 있습니다:

### 1. 컨트롤러 계층 (Controller Layer)

클라이언트의 요청을 받아 처리하고 응답을 반환하는 역할을 담당합니다. 주요 컨트롤러는 인증 관련 기능(OAuth2)과 사용자 관리 기능을 제공합니다.

### 2. 서비스 계층 (Service Layer)

비즈니스 로직을 구현하는 계층으로, 컨트롤러와 데이터 접근 계층 사이의 중간자 역할을 합니다. 각 도메인별로 서비스가 분리되어 있으며, 모든 비즈니스 규칙과 트랜잭션을 여기서 처리합니다.

### 3. 도메인 모델 (Domain Model)

애플리케이션의 핵심 비즈니스 개념을 표현하는 엔티티와 값 객체들이 포함됩니다. JPA 기반의 엔티티 클래스와 관련 리포지토리가 이 계층에 속합니다.

### 4. 보안 (Security)

인증 및 권한 부여를 담당하는 컴포넌트들로 구성됩니다:
- JWT 기반 인증 메커니즘
- Spring Security 설정
- 사용자 인증 정보 관리

### 5. 외부 통합 (External Integration)

GitHub OAuth2와 같은 외부 서비스와의 통합을 담당하는 컴포넌트들이 포함됩니다.

### 6. 공통 기능 (Common)

여러 계층에서 공통으로 사용되는 기능들을 제공합니다:
- 전역 예외 처리
- 애플리케이션 설정 (CORS, RestTemplate 등)
- 유틸리티 클래스

## 설정 가이드

### 환경 변수

다음 환경 변수를 `.env` 파일이나 시스템 환경 변수로 설정해야 합니다:

- `GITHUB_CLIENT_ID`: GitHub OAuth 앱 클라이언트 ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth 앱 클라이언트 시크릿
- `JWT_SECRET`: JWT 서명에 사용할 시크릿 키 (최소 32바이트)

### application.yml

주요 설정은 `application.yml`에서 관리됩니다:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}

jwt:
  secret: ${JWT_SECRET:12345678901234567890123456789012}
  expiration: 86400000 # 24시간

server:
  port: 8081
```

### 개발 환경 설정

1. **GitHub OAuth 앱 설정**
   - GitHub 개발자 설정에서 OAuth 앱 생성
   - Authorization callback URL을 `http://localhost:8081/oauth2/callback/github`로 설정
   - 생성된 Client ID와 Client Secret을 환경 변수로 설정

2. **H2 데이터베이스**
   - H2 콘솔 접근 URL: `http://localhost:8081/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - 사용자명: `sa`
   - 비밀번호: (비어 있음)

## API 엔드포인트

### 인증 API

- `GET /oauth2/authorization/github`: GitHub OAuth2 인증 시작
- `GET /oauth2/callback/github`: GitHub OAuth2 콜백 처리
- `GET /api/auth/me`: 현재 인증된 사용자 정보 조회

### 사용자 API

- `GET /api/users`: 모든 사용자 조회 (관리자 전용)
- `GET /api/users/{id}`: 특정 사용자 조회
- `POST /api/users`: 새 사용자 생성
- `PUT /api/users/{id}`: 사용자 정보 수정
- `DELETE /api/users/{id}`: 사용자 삭제

## 보안 관련 사항

### JWT 인증 관련 주의사항

1. JWT 라이브러리는 0.12.3 버전을 사용하고 있으며, 해당 버전에서는 이전 버전과 API가 크게 변경되었습니다.
2. 토큰 생성 시 최신 JJWT API를 사용합니다:
   - `subject()`, `claims()`, `issuedAt()`, `expiration()` 메소드 사용
   - `java.util.Date` 대신 `java.time.Instant` 사용
   - `signWith(key, Jwts.SIG.HS256)` 형식으로 서명
3. 토큰 파싱 시 `Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token)` 형식으로 사용합니다.

### 보안 고려사항

1. **JWT 시크릿 키**
   - 프로덕션 환경에서는 충분히 강력한 시크릿 키를 사용해야 합니다 (최소 256비트/32바이트).
   - 시크릿 키는 안전하게 관리되어야 하며 환경 변수로 주입하는 것이 좋습니다.

2. **CORS 설정**
   - 프로덕션 환경에서는 허용된 출처(Origin)만 명시적으로 설정해야 합니다.
   - `CorsConfig`에서 설정할 수 있습니다.

3. **HTTP 보안 헤더**
   - 적절한 보안 헤더(X-Content-Type-Options, X-Frame-Options, Content-Security-Policy 등)를 설정하는 것이 좋습니다.

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
docker run -p 8081:8081 --env-file .env portfolio-backend
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
   - H2 콘솔은 `http://localhost:8081/h2-console`에서 접근할 수 있습니다.
   - JDBC URL은 `jdbc:h2:mem:testdb`로 설정해야 합니다.

## OOP 및 유지보수성

이 프로젝트는 객체 지향 프로그래밍(OOP) 원칙과 유지보수성을 최우선으로 고려하여 설계되었습니다.

### 객체 지향 설계 원칙

프로젝트 전반에 걸쳐 SOLID 원칙을 적용하여 코드의 유연성, 확장성, 그리고 유지보수성을 향상시켰습니다:

- **단일 책임 원칙 (SRP)**: 각 클래스는 하나의 책임만 가지도록 설계되었습니다.
- **개방-폐쇄 원칙 (OCP)**: 새로운 기능의 추가는 기존 코드 수정 없이 확장을 통해 가능합니다.
- **리스코프 치환 원칙 (LSP)**: 상위 타입의 객체를 하위 타입의 객체로 대체해도 프로그램의 정확성이 유지됩니다.
- **인터페이스 분리 원칙 (ISP)**: 클라이언트는 자신이 사용하지 않는 메서드에 의존하지 않습니다.
- **의존성 역전 원칙 (DIP)**: 고수준 모듈은 저수준 모듈에 직접 의존하지 않고, 추상화에 의존합니다.

### 코드 구조 및 패턴

- **계층형 아키텍처**: 표준 계층형 구조를 통해 관심사를 명확히 분리합니다.
- **DTO 패턴**: 계층 간 데이터 전송 시 캡슐화와 의존성 관리를 위해 DTO를 사용합니다.
- **의존성 주입**: Spring의 DI를 통해 컴포넌트 간 결합도를 낮추고 테스트 용이성을 높입니다.

### 유지보수성 향상 전략

- **일관된 코드 스타일**: 코드 가독성과 이해도를 높이기 위한 일관된 코딩 스타일을 적용합니다.
- **중앙화된 예외 처리**: 전역 예외 핸들러를 통해 일관된 오류 응답을 제공합니다.
- **설정 외부화**: 환경 변수와 프로파일을 활용하여 다양한 환경에서 유연하게 동작합니다.
- **모듈화**: 기능별로 모듈을 분리하여 각 모듈이 독립적으로 발전할 수 있도록 합니다.
