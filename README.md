# 포트폴리오 사이트

> 클로드(Claude) AI를 활용하여 대부분의 프론트엔드를 개발한 포트폴리오 사이트입니다.

## 🤖 AI 개발 프로세스

이 프로젝트는 **Claude 3.7 Sonnet**과 **Claude MCP(Model Context Protocol)**를 활용하여 개발되었습니다.

### Claude 활용 방식

- **프론트엔드**: 대부분의 컴포넌트와 로직을 Claude와의 대화를 통해 구현
- **코드 리뷰 및 개선**: Claude를 활용한 지속적인 코드 개선 및 최적화
- **문서 작성**: README, 주석, 문서화도 Claude의 도움으로 작성
- **설계 결정**: 아키텍처와 디자인 패턴 선택에 Claude의 조언 활용

## 기술 스택

### 프론트엔드
- **Vue 3** + **TypeScript**
- **Vite** - 빌드 시스템
- **Pinia** - 상태 관리
- **Vue Router 4** - 라우팅
- **Tailwind CSS** - 스타일링
- **Feature-Sliced Design(FSD)** - 아키텍처

### 백엔드
- **Spring Boot 3.2.5**
- **Spring Security**
- **Spring Data JPA**
- **JWT** - 인증
- **OAuth2 (GitHub)** - 로그인
- **Java 17** + **Gradle**

## 프로젝트 구조

```
project-root/
├── frontend/                 # Vue.js 프론트엔드
│   ├── src/
│   │   ├── app/             # 앱 초기화, 전역 설정
│   │   ├── pages/           # 페이지 컴포넌트
│   │   ├── widgets/         # 복합 UI 블록
│   │   ├── features/        # 사용자 상호작용 기능
│   │   ├── entities/        # 비즈니스 엔티티
│   │   └── shared/          # 공유 유틸리티, UI 키트
│   └── docs/                # 프론트엔드 문서
└── backend/                 # Spring Boot 백엔드
    ├── src/
    │   ├── controller/      # REST API 컨트롤러
    │   ├── service/         # 비즈니스 로직
    │   ├── domain/          # 도메인 모델 및 Repository
    │   └── common/          # 공통 기능
    └── docs/                # 백엔드 문서
```

## 설치 및 실행

### 환경 변수 설정

1. `.env` 파일 생성 (프론트엔드)
```
NODE_ENV=development
VITE_API_URL=http://localhost:8081  # 백엔드 API URL
VITE_BASE_URL=http://localhost:8080 # 프론트엔드 URL
VITE_BASE_PORT=8080                 # 프론트엔드 Port
VITE_GITHUB_CLIENT_ID=              # GitHub OAuth 앱 클라이언트 ID
```

2. `application.yml` 설정 (백엔드)
```yaml
`application.yml` 파일 또는 환경 변수를 통해 다음 설정이 필요합니다:
- `GITHUB_CLIENT_ID`: GitHub OAuth 앱 클라이언트 ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth 앱 클라이언트 시크릿
- `JWT_SECRET`: JWT 토큰 암호화 키
- `AWS_ACCESS_KEY`: AWS IAM 계정의 액세스 키
- `AWS_SECRET_KEY`: AWS IAM 계정의 시크릿 키
- `AWS_REGION`: AWS 리전 (기본값: ap-northeast-2)
- `AWS_S3_BUCKET_NAME`: 사용할 S3 버킷 이름
```

### 실행 방법

#### 백엔드 실행
```bash
cd backend
./gradlew bootRun
```

#### 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

## 개발 과정

### Claude 활용 사례

1. **FSD 아키텍처 적용**
    - Claude와 함께 복잡한 FSD 구조를 체계적으로 설계
    - 각 레이어의 역할과 의존성 규칙을 명확히 정의

2. **컴포넌트 개발**
    - 복잡한 비즈니스 로직을 Claude와 논의하며 구현
    - 타입스크립트 타입 정의 최적화

3. **UI/UX 개선**
    - 사용자 친화적인 인터페이스를 위한 디자인 패턴 적용
    - 접근성과 반응형 디자인 고려

### 학습한 내용

- AI와의 협업을 통한 효율적인 개발 프로세스
- 체계적인 아키텍처 설계의 중요성
- 타입 안정성을 보장하는 TypeScript 활용법
- 모던 프론트엔드 개발 트렌드