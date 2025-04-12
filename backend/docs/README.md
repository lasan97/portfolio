# 포트폴리오 백엔드 서비스

## 개요
이 프로젝트는 포트폴리오 웹 애플리케이션의 백엔드 시스템으로, Spring Boot 기반으로 구현되었습니다.

## 기술 스택
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- JWT 인증
- OAuth2 (GitHub)
- AWS S3 (파일 저장소)
- H2 Database
- Gradle (Kotlin DSL)
- Java 17

## 프로젝트 구조
```
backend
├── src/main/java/com/portfolio/backend
│   ├── controller          # REST API 컨트롤러
│   ├── service             # 비즈니스 로직
│   ├── domain              # 도메인 모델 및 Repository
│   ├── common              # 공통 기능(보안, 예외처리 등)
│   └── BackendApplication  # 메인 애플리케이션 클래스
```

## 주요 기능
1. **사용자 관리**
   - OAuth2를 통한 GitHub 로그인
   - 사용자 프로필 관리

2. **자기소개 관리**
   - 포트폴리오 자기소개 CRUD 기능
   - 외부 링크 관리

3. **파일 관리**
   - AWS S3를 이용한 파일 업로드/다운로드
   - 이미지 및 첨부파일 관리

4. **보안**
   - JWT 토큰 기반 인증
   - Spring Security 기반 권한 관리

## 환경 설정
`application.yml` 파일 또는 환경 변수를 통해 다음 설정이 필요합니다:
- `GITHUB_CLIENT_ID`: GitHub OAuth 앱 클라이언트 ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth 앱 클라이언트 시크릿
- `JWT_SECRET`: JWT 토큰 암호화 키
- `AWS_ACCESS_KEY`: AWS IAM 계정의 액세스 키
- `AWS_SECRET_KEY`: AWS IAM 계정의 시크릿 키
- `AWS_REGION`: AWS 리전 (기본값: ap-northeast-2)
- `AWS_S3_BUCKET_NAME`: 사용할 S3 버킷 이름

## 실행 방법
```bash
# Gradle로 실행
./gradlew bootRun

# 또는 Docker로 실행
docker build -t portfolio-backend .
docker run -p 8081:8081 portfolio-backend
```

## API 엔드포인트
- 인증: `/api/auth/**`
  - OAuth2 로그인: `/api/auth/oauth2/github`
- 사용자: `/api/users/**`
- 자기소개: `/api/introduction/**`
- 파일: `/api/files/**`
  - 단일 파일 업로드: `/api/files/upload` (POST)
  - 다중 파일 업로드: `/api/files/upload-multiple` (POST)
  - 파일 삭제: `/api/files` (DELETE)

서버는 기본적으로 8081 포트에서 실행됩니다.
