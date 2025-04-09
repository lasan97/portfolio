# 포트폴리오 FSD - Vue 3 프론트엔드

## 개요

본 프로젝트는 Feature-Sliced Design (FSD) 아키텍처와 Vue 3 + TypeScript를 사용하여 구성된 프론트엔드 애플리케이션입니다. FSD는 프론트엔드 애플리케이션을 기능(feature) 단위로 분할하고, 각 기능을 여러 계층(layer)으로 구성하는 방법론으로, 코드 가독성, 유지보수성, 확장성을 향상시킵니다.

## 기술 스택

- **핵심 프레임워크**: Vue 3
- **언어**: TypeScript
- **빌드 도구**: Vite (Vue CLI에서 마이그레이션 완료)
- **상태 관리**: Vuex 4
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

## Vue CLI에서 Vite로 마이그레이션

프로젝트는 최근에 Vue CLI에서 Vite로 마이그레이션되었습니다. 주요 변경 사항은 다음과 같습니다:

1. **환경 변수 변경**: 
   - 이전: `VUE_APP_*` 접두사 → 현재: `VITE_*` 접두사
   - 이전: `process.env.VUE_APP_*` → 현재: `import.meta.env.VITE_*`

2. **빌드 설정 변경**:
   - 이전: `vue.config.js` → 현재: `vite.config.ts`
   - 이전: Webpack → 현재: Vite (빌드 속도 향상)

3. **HTML 템플릿**:
   - 이전: `public/index.html` → 현재: 루트 디렉토리의 `index.html`
   - 웹팩 특화 플레이스홀더(`<%= BASE_URL %>`, `<%= htmlWebpackPlugin.options.title %>`) 제거

4. **라우터 설정**:
   - `createWebHistory(process.env.BASE_URL)` → `createWebHistory(import.meta.env.BASE_URL)`

## 주요 디렉토리 구조 (FSD 아키텍처)

```
src/
├── app/            # 애플리케이션 초기화, 전역 스타일, 프로바이더
│   ├── router/     # Vue Router 설정
│   ├── store/      # Vuex 스토어
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
   - 직접적인 내부 파일 임포트 금지 (예: `@/features/auth/login/ui/LoginForm.vue`)
   - 올바른 예: `import { LoginForm } from '@/features/auth/login'`

## 개발 가이드

### 새 기능 추가하기

1. 해당 기능이 속하는 계층(`features`, `entities` 등) 결정
2. 새 디렉토리 생성 및 다음과 같은 세그먼트로 구성:
   - `ui/`: 컴포넌트
   - `model/`: 상태 관리 (Vuex, Composition API)
   - `api/`: API 요청
   - `lib/`: 유틸리티 함수
   - `config/`: 설정
3. 모든 공개 요소를 `index.ts`에서 내보내기

### 예시: 로그인 기능 구현

```
features/auth/login/
├── api/                 # 로그인 API 요청
│   ├── loginUser.ts     # 로그인 API 함수
│   └── index.ts         # API 내보내기
├── model/               # 로그인 상태 관리
│   ├── useLogin.ts      # 로그인 컴포저블
│   └── index.ts         # 모델 내보내기
├── ui/                  # 로그인 UI 요소
│   └── LoginForm.vue    # 로그인 폼 컴포넌트
└── index.ts             # 공개 API
```

`index.ts` 예시:
```typescript
// Public API - 이 파일을 통해서만 모듈 내부 요소를 외부로 노출
export { default as LoginForm } from './ui/LoginForm.vue';
export { useLogin } from './model';
export * from './api';
```

### Vite 관련 주의사항

1. **환경 변수 사용**:
   ```typescript
   // 잘못된 사용 (Vue CLI)
   const apiUrl = process.env.VUE_APP_API_URL;
   
   // 올바른 사용 (Vite)
   const apiUrl = import.meta.env.VITE_API_URL;
   ```

2. **동적 임포트**:
   ```typescript
   // 잘못된 사용 (Webpack 주석)
   const HomePage = () => import(/* webpackChunkName: "home" */ '@/pages/home');
   
   // 올바른 사용 (Vite)
   const HomePage = () => import('@/pages/home');
   ```

3. **경로 별칭 (@)**:
   - Vite 설정에서 경로 별칭이 올바르게 설정되어 있는지 확인
   - `import` 문에서 항상 `.vue` 확장자 포함

## Vuex 사용 가이드

```typescript
// store/modules/auth.ts
import { Module } from 'vuex';
import { RootState } from '../types';

export interface AuthState {
  user: any | null;
  token: string | null;
}

const auth: Module<AuthState, RootState> = {
  namespaced: true,
  state: () => ({
    user: null,
    token: null
  }),
  mutations: {
    // ...
  },
  actions: {
    // ...
  },
  getters: {
    // ...
  }
};

export default auth;
```

## 타입스크립트 사용 예시

### 컴포넌트 정의

```typescript
<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'MyComponent',
  props: {
    title: {
      type: String,
      required: true
    }
  }
});
</script>
```

### Composition API 사용

```typescript
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useStore } from 'vuex';

// 타입 정의
interface User {
  id: number;
  name: string;
  email: string;
}

// 스토어 사용
const store = useStore();

// 상태 정의
const user = ref<User | null>(null);
const isAuthenticated = computed(() => user.value !== null);
</script>
```

## 스타일 가이드 (Tailwind CSS)

프로젝트는 Tailwind CSS를 사용하며, 다음과 같은 방식으로 스타일을 적용합니다:

```html
<template>
  <div class="flex items-center justify-between p-4 bg-white shadow rounded-lg">
    <h2 class="text-xl font-semibold text-gray-800">{{ title }}</h2>
    <button class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
      {{ buttonText }}
    </button>
  </div>
</template>
```

## 문제 해결 가이드

### Vite 관련 오류

1. **환경 변수 접근 오류**:
   - 모든 환경 변수가 `VITE_` 접두사로 시작하는지 확인
   - `import.meta.env`를 통해 접근하는지 확인

2. **모듈 해석 오류**:
   - 파일 확장자가 포함되어 있는지 확인 (특히 `.vue` 파일)
   - `vite.config.ts`의 별칭 설정 확인

3. **HMR (핫 모듈 리로딩) 문제**:
   - Vite 개발 서버 재시작
   - 순환 의존성 확인
   - 대소문자 구분 확인 (특히 Windows 환경)

## 참고 자료

- [Feature-Sliced Design 공식 웹사이트](https://feature-sliced.design/)
- [Vue 3 문서](https://v3.vuejs.org/)
- [Vite 문서](https://vitejs.dev/guide/)
- [Tailwind CSS 문서](https://tailwindcss.com/docs)
