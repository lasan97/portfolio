# Portfolio 프로젝트 Frontend 가이드

이 문서는 Portfolio 프로젝트의 프론트엔드 부분에 대한 상세 설명과 가이드라인을 제공합니다.

## 목차
1. [프로젝트 구조](#프로젝트-구조)
2. [기술 스택](#기술-스택)
3. [아키텍처](#아키텍처)
4. [설치 및 실행 방법](#설치-및-실행-방법)
5. [주요 기능](#주요-기능)
6. [렌더링 방식](#렌더링-방식)
7. [인증 시스템](#인증-시스템)
8. [상세 문서](#상세-문서)
9. [GitHub 공개 시 주의사항](#github-공개-시-주의사항)

## 프로젝트 구조

이 프로젝트는 Feature-Sliced Design 아키텍처를 따르며, 다음과 같은 계층 구조로 구성되어 있습니다:

```
frontend/
├── src/
│   ├── app/          # 애플리케이션 진입점 및 설정 (라우터, 스토어)
│   ├── pages/        # 페이지 컴포넌트
│   ├── widgets/      # 페이지를 구성하는 복합 UI 블록
│   ├── features/     # 사용자 상호작용 기능
│   ├── entities/     # 비즈니스 엔티티
│   ├── shared/       # 공유 유틸리티, UI, 타입
│   ├── processes/    # 전역 프로세스
│   ├── entry-client.ts  # 클라이언트 진입점
│   └── entry-server.ts  # 서버 진입점 (SSR)
├── public/           # 정적 파일
├── dist/             # 빌드 결과물
├── docs/             # 문서
└── server.js         # SSR 서버
```

## 기술 스택

- **프레임워크**: Vue 3 (Composition API)
- **상태 관리**: Pinia
- **라우터**: Vue Router 4
- **빌드 도구**: Vite
- **스타일링**: TailwindCSS 
- **API 통신**: Axios
- **언어**: TypeScript
- **SSR**: Vue SSR (custom implementation)
- **마크다운 렌더링**: marked + DOMPurify

## 아키텍처

이 프로젝트는 Feature-Sliced Design (FSD) 아키텍처 패턴을 따르고 있으며, 다음과 같은 주요 계층으로 구성됩니다:

1. **app**: 애플리케이션의 진입점 및 전역 설정을 포함합니다.
   - `App.vue`: 애플리케이션의 루트 컴포넌트
   - `router`: 라우팅 설정
   - `store`: Pinia 스토어 설정

2. **pages**: 전체 페이지 컴포넌트들이 위치합니다.
3. **widgets**: 페이지를 구성하는 복합적인 UI 블록입니다.
4. **features**: 사용자 상호작용 기능을 담당합니다.
5. **entities**: 비즈니스 모델과 관련된 로직과 컴포넌트입니다.
6. **shared**: 프로젝트 전체에서 사용되는 공통 코드를 포함합니다.

## 설치 및 실행 방법

### 요구 사항
- Node.js 16 이상
- npm 또는 yarn

### 환경 설정
프로젝트 루트에 `.env.local` 파일을 생성하고 다음과 같이 설정합니다:

```
NODE_ENV=development
VITE_API_URL=http://localhost:8081  # 백엔드 API URL
VITE_BASE_URL=http://localhost:8080 # 프론트엔드 URL
VITE_BASE_PORT=8080                 # 프론트엔드 포트
VITE_GITHUB_CLIENT_ID=your_github_client_id_here
VITE_SSR_DEBUG=false
```
- `vite.config.ts`: Vite 설정

환경 변수는 애플리케이션 전체에서 `import.meta.env.VITE_*` 형식으로 접근할 수 있습니다.
Vite는 현재 환경에 맞는 `.env` 파일들을 자동으로 로드합니다. 개발/프로덕션 환경에 따라 다른 설정을 사용하려면 `.env.development`와 `.env.production` 파일을 추가로 생성할 수 있습니다.

## 주요 디렉토리 구조 (FSD 아키텍처)

```
src/
├── app/            # 애플리케이션 초기화, 전역 스타일, 프로바이더
│   ├── router/     # Vue Router 설정
│   ├── store/      # Pinia 스토어 설정
│   ├── styles/     # 전역 스타일 (Tailwind CSS)
│   └── App.vue     # 루트 컴포넌트
├── processes/      # 비즈니스 프로세스와 워크플로우
├── pages/          # 페이지 컴포넌트
│   ├── home/       # 홈 페이지
│   ├── notFound/   # 404 페이지
│   └── ...
├── widgets/        # 복합 UI 블록 (헤더, 푸터, 사이드바 등)
├── features/       # 사용자 상호작용 기능
│   └── auth/       # 인증 관련 기능
├── entities/       # 비즈니스 엔티티 (사용자, 제품 등)
└── shared/         # 공유 유틸리티, 라이브러리, UI 키트
    ├── api/        # API 클라이언트, 요청 유틸리티
    ├── config/     # 환경 변수, 앱 상수
    ├── lib/        # 유틸리티 함수
    └── ui/         # 재사용 가능한 UI 컴포넌트
```

> **참고**: GitHub OAuth를 사용하려면 GitHub Developer Settings에서 OAuth 앱을 등록하고 클라이언트 ID를 얻어야 합니다.

### 설치
```bash
cd frontend
npm install
```

### 개발 서버 실행
```bash
# CSR 모드
npm run dev

# SSR 모드
npm run dev:ssr
```

### 빌드
```bash
# 클라이언트 빌드
npm run build

# SSR 빌드 (클라이언트 + 서버)
npm run build:ssr
```

### 프로덕션 서버 실행
```bash
# SSR 모드
npm run serve:ssr

# 정적 모드
npm run serve
```

## 주요 기능

1. **인증 시스템**
   - GitHub OAuth를 통한 로그인
   - 쿠키와 로컬 스토리지를 이용한 인증 상태 유지
   - 다중 탭 간의 인증 상태 동기화

2. **SSR(Server-Side Rendering) 구현**
   - 일부 페이지는 SSR 지원
   - 클라이언트와 서버 코드 공유 및 하이드레이션

## 렌더링 방식

이 프로젝트는 하이브리드 렌더링 방식을 사용합니다:

1. **CSR (Client-Side Rendering)**
   - 대부분의 페이지는 클라이언트에서 렌더링됩니다.

2. **SSR (Server-Side Rendering)**
   - 소개 페이지와 SSR 데모 페이지는 서버에서 첫 렌더링을 수행합니다.
   - `meta: { ssr: true }` 속성을 가진 라우트에 대해서만 SSR이 적용됩니다.

3. **하이드레이션 (Hydration)**
   - SSR로 렌더링된 HTML에 클라이언트 이벤트 핸들러를 연결합니다.
   - 서버에서 생성된 상태를 클라이언트로 전달합니다.

SSR이 필요한 페이지에는 ssr meta가 적용되어 있습니다.
```javascript
// 라우트 메타데이터에 ssr: true 설정
{
  path: ROUTES.INTRODUCTION,
  name: 'introduction',
  component: IntroductionPage,
  meta: { ssr: true }
}
```

## 인증 시스템

이 프로젝트는 다음과 같은 인증 흐름을 사용합니다:

1. GitHub OAuth를 통한 사용자 인증
2. 백엔드에서 JWT 토큰 발급
3. 쿠키와 로컬스토리지를 이용한 토큰 저장
4. 페이지 새로고침 시 토큰 복원
5. 다중 탭/창 간의 인증 상태 동기화

인증이 필요한 페이지에는 라우터 가드가 적용되어 있습니다.

```javascript
// 라우트 메타데이터에 requiresAuth: true 설정
{
  path: '/profile',
  name: 'profile',
  component: ProfilePage,
  meta: { requiresAuth: true }
}
```

## 상세 문서

더 자세한 정보는 다음 문서를 참조하세요:

- [개발 가이드](./docs/development-guide.md) - 개발 프로세스 및 코딩 규칙
- [SSR 가이드](./docs/ssr-guide.md) - SSR 구현 상세 정보
- [Vite & Pinia 가이드](./docs/vite-pinia-guide.md) - 빌드 도구 및 상태 관리 
- [하이브리드 렌더링 가이드](./docs/hybrid-ssr-csr.md) - CSR과 SSR 혼합 사용 방법
