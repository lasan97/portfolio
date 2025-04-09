# 포트폴리오 FSD - Vue 3 프론트엔드

## 개요

본 프로젝트는 Feature-Sliced Design (FSD) 아키텍처와 Vue 3 + TypeScript를 사용하여 구성된 프론트엔드 애플리케이션입니다. FSD는 프론트엔드 애플리케이션을 기능(feature) 단위로 분할하고, 각 기능을 여러 계층(layer)으로 구성하는 방법론으로, 코드 가독성, 유지보수성, 확장성을 향상시킵니다.

## 기술 스택

- **핵심 프레임워크**: Vue 3
- **언어**: TypeScript
- **빌드 도구**: Vite (Vue CLI에서 마이그레이션 완료)
- **상태 관리**: Pinia
- **라우팅**: Vue Router 4
- **HTTP 클라이언트**: Axios
- **스타일링**: Tailwind CSS
- **코드 품질**: ESLint

## 시작하기

### 설치

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (Vite)
npm run dev

# 프로덕션용 빌드
npm run build

# 빌드 결과 미리보기
npm run preview

# 린트 실행
npm run lint
```

### 환경 설정

본 프로젝트는 다음과 같은 환경 설정 파일을 사용합니다:

- `.env`: 환경 변수 기본값
  ```
  NODE_ENV=development
  VITE_API_URL=http://localhost:8081/api  # 백엔드 API URL
  VITE_BASE_URL=http://localhost:8080     # 프론트엔드 URL
  VITE_GITHUB_CLIENT_ID=your_github_client_id # Github Client_id
  ```
- `vite.config.ts`: Vite 설정

환경 변수는 애플리케이션 전체에서 `import.meta.env.VITE_*` 형식으로 접근할 수 있습니다. 
개발/프로덕션 환경에 따라 다른 설정을 사용하려면 `.env.development`와 `.env.production` 파일을 추가로 생성할 수 있습니다.

## 주요 디렉토리 구조 (FSD 아키텍처)

```
src/
├── app/            # 애플리케이션 초기화, 전역 스타일, 프로바이더
│   ├── router/     # Vue Router 설정
│   ├── store/      # Pinia 스토어 설정
│   ├── styles/     # 전역 스타일 (Tailwind CSS)
│   └── App.vue     # 루트 컴포넌트
├── pages/          # 페이지 컴포넌트
│   ├── home/       # 홈 페이지
│   ├── notFound/   # 404 페이지
│   └── ...
├── widgets/        # 복합 UI 블록 (헤더, 푸터, 사이드바 등)
├── features/       # 사용자 상호작용 기능
│   └── auth/       # 인증 관련 기능
│       ├── login/        # 로그인 기능
│       │   ├── api/      # 로그인 API 요청
│       │   ├── model/    # 로그인 로직
│       │   ├── ui/       # 로그인 컴포넌트
│       │   └── index.ts  # 공개 API
│       └── register/     # 회원가입 기능
├── entities/       # 비즈니스 엔티티 (사용자, 제품 등)
└── shared/         # 공유 유틸리티, 라이브러리, UI 키트
    ├── api/        # API 클라이언트, 요청 유틸리티
    ├── config/     # 환경 변수, 앱 상수
    ├── lib/        # 유틸리티 함수
    ├── styles/     # 스타일 관련 설정
    └── ui/         # 재사용 가능한 UI 컴포넌트
```

## FSD 아키텍처 주요 원칙

1. **계층화 (Layering)**:
   - 상위 레이어: `app` > `processes` > `pages` > `widgets` > `features` > `entities` > `shared`
   - 각 계층은 자신보다 하위 계층에만 의존 가능 (단방향 의존성)

2. **슬라이스 독립성 (Slice Independence)**:
   - 각 슬라이스(기능)는 독립적이며 다른 슬라이스에 의존하지 않음
   - 예: `features/auth/login`은 `features/auth/register`에 의존하지 않음

3. **공개 API (Public API)**:
   - 각 모듈은 `index.ts` 파일을 통해서만 외부에 노출
   - 직접적인 내부 파일 임포트 금지 (예: `@features/auth/login/ui/LoginForm.vue`)
   - 올바른 예: `import { LoginForm } from '@features/auth/login'`

## 개발 가이드 및 문서

다음 개발 가이드 및 문서는 `docs/` 디렉토리에서 확인할 수 있습니다:

- [FSD 개발 가이드](docs/development-guide.md) - FSD 아키텍처 기반 개발 방법
- [Vite 및 Pinia 가이드](docs/vite-pinia-guide.md) - Vite와 Pinia 사용 방법
- [SSR 구현 가이드](docs/ssr-guide.md) - SSR 및 하이브리드 렌더링 구현 방법
- [하이브리드 CSR/SSR 가이드](docs/hybrid-ssr-csr.md) - 하이브리드 렌더링 접근법

## 참고 자료

- [Feature-Sliced Design 공식 웹사이트](https://feature-sliced.design/)
- [Vue 3 문서](https://v3.vuejs.org/)
- [Vite 문서](https://vitejs.dev/guide/)
- [Pinia 문서](https://pinia.vuejs.org/)
- [Vue Composition API 문서](https://v3.vuejs.org/guide/composition-api-introduction.html)
- [Tailwind CSS 문서](https://tailwindcss.com/docs)
