# 프론트엔드 개발 가이드

이 문서는 Portfolio 프로젝트의 프론트엔드 개발 프로세스, 코딩 스타일, 아키텍처 결정에 대한 상세 정보를 제공합니다.

## 목차
1. [개발 환경 설정](#개발-환경-설정)
2. [아키텍처 가이드라인](#아키텍처-가이드라인)
3. [코딩 스타일 및 규칙](#코딩-스타일-및-규칙)
4. [컴포넌트 개발 가이드](#컴포넌트-개발-가이드)
5. [상태 관리](#상태-관리)
6. [라우팅](#라우팅)
7. [API 통신](#api-통신)
8. [테스트](#테스트)
9. [빌드 및 배포](#빌드-및-배포)
10. [관련 문서](#관련-문서)

## 개발 환경 설정

### 필수 도구
- Node.js 16 이상
- npm 또는 yarn
- Git

### 개발 환경 설정 단계
1. 저장소 클론
```bash
git clone <repository-url>
cd portfolio/frontend
```

2. 의존성 설치
```bash
npm install
```

3. 개발 서버 실행
```bash
# 일반 개발 모드 (CSR)
npm run dev

# SSR 개발 모드
npm run dev:ssr
```

4. 환경 변수 설정
- `.env.local` 파일 생성 (개발용)
- `.env.production` 파일은 프로덕션 배포용

기본 환경 변수 구성:
```
VITE_API_URL=http://localhost:8081  # 백엔드 API URL
VITE_BASE_URL=http://localhost:8080 # 프론트엔드 URL
VITE_BASE_PORT=8080                 # 프론트엔드 포트
VITE_GITHUB_CLIENT_ID=your_client_id  # GitHub OAuth 클라이언트 ID
VITE_SSR_DEBUG=false                # SSR 디버깅 모드
```

## 아키텍처 가이드라인

이 프로젝트는 Feature-Sliced Design(FSD) 아키텍처를 따릅니다. FSD는 코드를 기능 중심으로 조직화하여 확장성과 유지보수성을 높이는 아키텍처 방법론입니다.

### FSD 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                           app                               │
│               (진입점, 설정, 전역 프로바이더)                 │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                         processes                           │
│                 (비즈니스 프로세스, 흐름)                     │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                          pages                              │
│                 (라우트에 매핑된 페이지)                      │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                         widgets                             │
│            (독립적인 복합 블록, 페이지의 구성요소)             │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                         features                            │
│               (사용자 상호작용 기능 집합)                     │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                         entities                            │
│                (비즈니스 엔티티 모델)                        │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                          shared                             │
│          (공통 유틸리티, UI, 타입, 라이브러리)                │
└─────────────────────────────────────────────────────────────┘
```

### 프로젝트 구조 예시

```
src/
├── app/            # 앱 진입점, 설정
│   ├── router/     # 라우터 설정
│   ├── store/      # 스토어 설정
│   ├── styles/     # 전역 스타일
│   └── App.vue     # 루트 컴포넌트
├── processes/      # 비즈니스 프로세스
├── pages/          # 페이지 컴포넌트
│   ├── home/       # 홈 페이지
│   ├── auth/       # 인증 관련 페이지
│   └── profile/    # 프로필 페이지
├── widgets/        # 복합 UI 블록
│   ├── header/     # 헤더 위젯
│   └── footer/     # 푸터 위젯
├── features/       # 사용자 기능
│   ├── auth/       # 인증 관련 기능
│   └── profile/    # 프로필 관련 기능
├── entities/       # 비즈니스 엔티티
│   ├── user/       # 사용자 엔티티
│   └── product/    # 제품 엔티티
└── shared/         # 공유 코드
    ├── ui/         # UI 컴포넌트
    ├── api/        # API 클라이언트
    ├── lib/        # 유틸리티
    └── config/     # 상수 및 설정
```

### 계층 구조
- **app**: 애플리케이션 진입점 및 글로벌 설정
- **pages**: 라우트와 연결된 전체 페이지 컴포넌트
- **widgets**: 페이지 구성에 사용되는 복합 UI 블록
- **features**: 사용자 상호작용 기능
- **entities**: 비즈니스 엔티티 모델 및 관련 로직
- **shared**: 공통 유틸리티 및 UI 컴포넌트

### 종속성 규칙
- 하위 계층은 상위 계층을 임포트할 수 없음
- 예: `entities`는 `features`를 임포트할 수 없음
- 수평적 임포트도 제한됨
- 예: `features/auth`는 `features/profile`을 직접 임포트할 수 없음

### 슬라이스 구조
각 슬라이스(예: `features/auth`)는 다음과 같은 내부 세그먼트 구조를 가질 수 있습니다:

- **ui**: 표현 컴포넌트
- **model**: 상태 및 비즈니스 로직
- **api**: API 요청 처리
- **lib**: 유틸리티 함수
- **config**: 설정

### 공개 API (Public API)
- 각 모듈은 `index.ts` 파일을 통해서만 외부에 노출
- 직접적인 내부 파일 임포트 금지 (예: `@features/auth/login/ui/LoginForm.vue`)
- 올바른 예: `import { LoginForm } from '@features/auth'`

## 코딩 스타일 및 규칙

### TypeScript
- 모든 새로운 코드는 TypeScript로 작성
- 명시적 타입 선언 사용
- `any` 타입 사용 지양

```typescript
// Good
function getUserName(user: User): string {
  return user.name;
}

// Bad
function getUserName(user: any) {
  return user.name;
}
```

### Vue 컴포넌트
- Composition API 사용
- `<script setup>` 문법 권장 (단순 컴포넌트)
- 복잡한 컴포넌트는 `defineComponent`와 `setup()` 함수 사용

```vue
<!-- 단순 컴포넌트 -->
<script setup lang="ts">
import { ref } from 'vue';

const count = ref(0);
</script>

<!-- 복잡한 컴포넌트 -->
<script lang="ts">
import { defineComponent, ref, watch } from 'vue';

export default defineComponent({
  name: 'ComplexComponent',
  props: {
    initialValue: {
      type: Number,
      default: 0
    }
  },
  setup(props) {
    const count = ref(props.initialValue);
    // 복잡한 로직...
    return { count };
  }
});
</script>
```

### 파일 명명 규칙
- 컴포넌트: PascalCase (예: `UserProfile.vue`)
- 소스 파일: camelCase (예: `userStore.ts`)
- 인덱스 파일: 각 폴더에 `index.ts` 파일로 공개 API 정의
- 타입 정의: `types.ts` 또는 관련 파일에 인접하게 배치

### CSS 스타일링
- TailwindCSS 사용
- 컴포넌트 스코프 스타일 사용 (`<style scoped>`)
- 전역 스타일은 `app/styles` 또는 `shared/assets/styles`에 정의

## 컴포넌트 개발 가이드

### 컴포넌트 설계 원칙
1. **단일 책임 원칙**: 각 컴포넌트는 하나의 책임만 가져야 함
2. **UI와 로직 분리**: 표현(UI)과 로직을 분리
3. **재사용성**: 공통 UI 요소는 `shared/ui`에 배치
4. **접근성**: 모든 컴포넌트는 웹 접근성 표준을 준수해야 함

### 컴포넌트 구성
```vue
<template>
  <!-- 템플릿 마크업 -->
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'ComponentName', // 이름 필수
  props: {
    // 명확한 타입과 기본값 정의
    title: {
      type: String,
      required: true
    },
    count: {
      type: Number,
      default: 0
    }
  },
  setup(props) {
    // 컴포넌트 로직
    return {
      // 템플릿에 노출할 데이터/메소드
    };
  }
});
</script>

<style scoped>
/* 컴포넌트 스코프 스타일 */
</style>
```

### Props 및 Emits
- Props에 항상 타입과 필수 여부 또는 기본값 지정
- Emits도 명시적으로 정의
- TypeScript를 사용한 타입 안전성 확보

```typescript
// Props 타입 정의
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'danger';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
}

// Emits 정의
const emit = defineEmits<{
  (e: 'click', payload: { id: number }): void;
  (e: 'focus'): void;
}>();
```

## 상태 관리

### Pinia 스토어 사용
- 모든 상태 관리는 Pinia를 사용
- 도메인/기능별로 스토어 분리
- 스토어는 `model` 폴더에 배치

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { loginApi } from '../api';
import type { User, LoginCredentials } from '../types';

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const user = ref<User | null>(null);
  const token = ref<string | null>(null);
  
  // 게터
  const isAuthenticated = computed(() => !!token.value);
  
  // 액션
  async function login(credentials: LoginCredentials) {
    const response = await loginApi.login(credentials);
    token.value = response.token;
    user.value = response.user;
  }
  
  function logout() {
    token.value = null;
    user.value = null;
  }
  
  return {
    user,
    token,
    isAuthenticated,
    login,
    logout
  };
});
```

### 로컬 vs 글로벌 상태
- 글로벌 상태는 Pinia 스토어에 저장
- 컴포넌트 로컬 상태는 `ref` 또는 `reactive` 사용
- 컴포넌트 간 상태 공유는 props/emits 또는 provide/inject 사용

## 라우팅

### 라우트 구성
- 모든 라우트는 `app/router/index.ts`에 정의
- 경로 상수는 `shared/config`에 정의
- 라우트 메타데이터 활용 (인증, SSR 등)

```typescript
// shared/config/routes.ts
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  PROFILE: '/profile',
  // 기타 경로...
};

// app/router/index.ts
const routes: Array<RouteRecordRaw> = [
  {
    path: ROUTES.HOME,
    name: 'home',
    component: HomePage
  },
  {
    path: ROUTES.PROFILE,
    name: 'profile',
    component: ProfilePage,
    meta: { requiresAuth: true }
  },
  // 기타 라우트...
];
```

### 라우트 가드
- 인증 및 권한 검사에 라우트 가드 사용
- 비동기 데이터 로딩에 `beforeRouteEnter` 또는 `beforeRouteUpdate` 사용

```typescript
// 인증 가드 예시
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login' });
  } else {
    next();
  }
});
```

## API 통신

### API 인스턴스 구성
- Axios를 사용한 중앙 API 인스턴스 설정
- 인터셉터를 사용한 요청/응답 처리
- 오류 처리 표준화

```typescript
// shared/api/instance.ts
import axios from 'axios';
import { getAuthToken } from '@shared/lib';

export const apiInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: true // 쿠키 전송을 위해 필요
});

// 요청 인터셉터
apiInstance.interceptors.request.use(
  (config) => {
    // 요청 전 처리 (토큰 추가)
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
apiInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 Unauthorized 에러 처리
    if (error.response && error.response.status === 401) {
      if (typeof window !== 'undefined') {
        // 현재 URL을 저장하여 로그인 후 리다이렉트
        const currentPath = window.location.pathname;
        sessionStorage.setItem('redirectAfterLogin', currentPath);
        
        import('@shared/lib').then(({ logout }) => {
          logout();
          window.location.href = '/login';
        });
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiInstance;
```

### API 함수
- API 호출은 기능별 `api` 폴더에 정의
- 각 API 함수는 일관된 형식으로 작성
- 타입 안전성 보장

```typescript
// features/auth/api/loginApi.ts
import { apiInstance } from '@shared/api';
import type { LoginCredentials, AuthResponse } from '../types';

export const loginApi = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await apiInstance.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },
  
  async logout(): Promise<void> {
    await apiInstance.post('/auth/logout');
  }
};
```

## 테스트

현재 프로젝트에는 자동화된 테스트 설정이 구현되어 있지 않지만, 향후 다음과 같은 테스트 방식을 추가할 수 있습니다:

### 단위 테스트
- Vitest를 사용한 유닛 테스트
- 컴포넌트, 스토어, 유틸리티 함수에 대한 테스트 작성
- 테스트 파일은 테스트 대상 파일과 같은 디렉토리에 `*.test.ts` 형식으로 배치

### 수동 테스트 체크리스트
- 기능이 요구사항대로 작동하는지 확인
- 다양한 브라우저에서 테스트 (최소 Chrome, Firefox)
- 모바일 화면에서 반응형으로 작동하는지 확인
- 오류 상황 처리 테스트
- 접근성 검사

## 빌드 및 배포

### 개발 빌드
```bash
# 개발 빌드
npm run build:dev

# 개발 빌드 (SSR)
npm run build:ssr
```

### 프로덕션 빌드
```bash
# 프로덕션 빌드
npm run build:prod

# 프로덕션 빌드 (SSR)
npm run build:ssr
```

### 빌드 최적화
- 코드 분할을 통한 청크 최적화
- 이미지 및 정적 자산 최적화
- 트리 쉐이킹 및 데드 코드 제거

### 배포 전 체크리스트
- 환경 변수 검증
- 타입 체크 실행 (`npm run type-check`)
- 린트 검사 실행 (`npm run lint`)
- 테스트 통과 여부 확인
- 빌드 결과물 검증

## 관련 문서

프론트엔드 개발에 관한 추가 정보는 다음 문서를 참조하세요:

- [SSR 가이드](./ssr-guide.md) - 서버 사이드 렌더링 구현 상세 정보
- [Vite & Pinia 가이드](./vite-pinia-guide.md) - 빌드 도구 및 상태 관리 상세 정보
- [하이브리드 렌더링 가이드](./hybrid-ssr-csr.md) - CSR과 SSR 혼합 사용 방법
