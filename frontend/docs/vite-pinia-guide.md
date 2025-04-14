# Vite와 Pinia 가이드

이 문서는 Portfolio 프로젝트에서 사용하는 Vite 빌드 도구와 Pinia 상태 관리 라이브러리에 대한 상세 가이드를 제공합니다.

## 목차
1. [Vite 개요](#vite-개요)
2. [Vite 설정 파일](#vite-설정-파일)
3. [환경 변수 관리](#환경-변수-관리)
4. [Vite 플러그인](#vite-플러그인)
5. [Pinia 상태 관리](#pinia-상태-관리)
6. [Pinia와 SSR](#pinia와-ssr)
7. [Pinia 스토어 설계 패턴](#pinia-스토어-설계-패턴)
8. [관련 문서](#관련-문서)

## Vite 개요

Vite는 Vue 팀에서 개발한 빠른 프론트엔드 빌드 도구입니다. ESM(ES Modules)을 활용하여 개발 서버를 매우 빠르게 시작하고, 효율적인 HMR(Hot Module Replacement)을 제공합니다.

### 주요 특징
- 빠른 개발 서버 시작
- 즉각적인 모듈 교체 (HMR)
- 최적화된 빌드 과정
- 기본적인 TypeScript 지원
- CSS 및 정적 자산 처리
- 다양한 플러그인 생태계

## Vite 설정 파일

프로젝트의 `vite.config.ts` 파일은 Vite의 동작을 정의합니다:

```typescript
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode, command }) => {
  // 환경 변수 로드
  const env = loadEnv(mode, process.cwd(), '');
  const isSSR = process.env.SSR === 'true'
  const port = Number(env.VITE_BASE_PORT || 8080);

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@app': path.resolve(__dirname, './src/app'),
        '@processes': path.resolve(__dirname, './src/processes'),
        '@pages': path.resolve(__dirname, './src/pages'),
        '@widgets': path.resolve(__dirname, './src/widgets'),
        '@features': path.resolve(__dirname, './src/features'),
        '@entities': path.resolve(__dirname, './src/entities'),
        '@shared': path.resolve(__dirname, './src/shared')
      }
    },
    build: {
      minify: mode === 'production',
      sourcemap: mode !== 'production',
      ssrManifest: isSSR,
      outDir: isSSR ? 'dist/server' : 'dist/client',
    },
    server: {
      port: port,
      open: false
    },
    preview: {
      port: port
    },
    css: {
      postcss: './postcss.config.js'
    }
  }
})
```

### 주요 설정 설명

1. **경로 별칭 (resolve.alias)**
   - FSD 아키텍처 계층별로 별칭이 정의되어 있어 간편하게 임포트 가능합니다.
   - 예: `import { Button } from '@shared/ui'` 형식으로 사용

2. **빌드 설정 (build)**
   - `minify`: 프로덕션 모드에서만 코드 압축
   - `sourcemap`: 개발 모드에서만 소스맵 생성
   - `ssrManifest`: SSR 모드에서 매니페스트 파일 생성
   - `outDir`: SSR 여부에 따라 다른 출력 디렉토리 사용

3. **서버 설정 (server)**
   - `port`: 환경 변수에서 포트 번호 설정
   - `open`: 서버 시작 시 브라우저 자동 실행 비활성화

## 환경 변수 관리

Vite는 `.env` 파일 시스템을 사용하여 환경 변수를 관리합니다:

1. **.env**: 모든 모드에 적용되는 기본 환경 변수
2. **.env.local**: 로컬 재정의 (Git에 커밋되지 않음)
3. **.env.development**: 개발 모드 환경 변수
4. **.env.production**: 프로덕션 모드 환경 변수

### 현재 프로젝트에서 사용 중인 환경 변수
```
# 개발 환경 (.env.local)
NODE_ENV=development
VITE_API_URL=http://localhost:8081  # 백엔드 API URL
VITE_BASE_URL=http://localhost:8080 # 프론트엔드 URL
VITE_BASE_PORT=8080                 # 프론트엔드 포트
VITE_GITHUB_CLIENT_ID=your_github_client_id
VITE_SSR_DEBUG=false

# 프로덕션 환경 (.env.production)
NODE_ENV=production
VITE_API_URL=http://localhost:8081
VITE_BASE_URL=http://localhost:3000
VITE_BASE_PORT=3000
VITE_GITHUB_CLIENT_ID=your_github_client_id
```

### 환경 변수 사용
```typescript
// VITE_ 접두사를 가진 환경 변수만 클라이언트 코드에서 접근 가능
const apiUrl = import.meta.env.VITE_API_URL;
const isDevMode = import.meta.env.DEV; // Vite 내장 환경 변수
```

## Vite 플러그인

현재 프로젝트에서는 다음 Vite 플러그인을 사용합니다:

1. **@vitejs/plugin-vue**
   - Vue 싱글 파일 컴포넌트(.vue) 처리

추가할 수 있는 유용한 플러그인:

1. **vite-plugin-checker**
   - TypeScript, ESLint 검사 병렬 실행

2. **rollup-plugin-visualizer**
   - 빌드 결과물 분석 및 시각화

## Pinia 상태 관리

Pinia는 Vue 3용 공식 상태 관리 라이브러리로, Vuex를 대체하는 간단하고 직관적인 API를 제공합니다.

### 프로젝트에서의 Pinia 설정

```typescript
// src/app/store/index.ts
import { createPinia } from 'pinia';

const pinia = createPinia();

export default pinia;
```

```typescript
// src/app/index.ts
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import pinia from './store';

// 앱, 라우터, 스토어 내보내기
export { App, router, pinia };

// 기본 진입점 (CSR)
export function createClientApp() {
  const app = createApp(App);
  app.use(pinia);
  app.use(router);
  return { app, router, pinia };
}
```

### Pinia 스토어 기본 구조

현재 프로젝트는 Composition API 스타일로 Pinia 스토어를 구현하고 있습니다:

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useUserStore } from '@entities/user/model/userStore';
import { processOAuthCallback } from '../api';
import { setAuthToken, getAuthToken, logout as logoutAuth } from '@shared/lib';

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const token = ref<string | null>(getAuthToken());
  const loading = ref(false);
  const error = ref<string | null>(null);
  const authenticated = ref(!!token.value);
  
  // 게터
  const isAuthenticated = computed(() => authenticated.value);
  
  // 액션
  function setToken(newToken: string) {
    token.value = newToken;
    setAuthToken(newToken);
    authenticated.value = true;
  }
  
  function clearAuth() {
    token.value = null;
    logoutAuth();
    authenticated.value = false;
  }
  
  async function handleOAuthCallback(code: string) {
    // 구현...
  }
  
  async function fetchCurrentUser() {
    // 구현...
  }
  
  function logout() {
    // 구현...
  }
  
  // 초기화 함수
  async function initialize() {
    // 구현...
  }
  
  return {
    // 상태, 게터, 액션 등 내보내기
    token,
    loading,
    error,
    isAuthenticated,
    setToken,
    clearAuth,
    handleOAuthCallback,
    fetchCurrentUser,
    logout,
    initialize
  };
});
```

### 스토어 사용하기

```vue
<script setup>
import { useAuthStore } from '@features/auth';
import { storeToRefs } from 'pinia';

// 스토어 인스턴스 생성
const authStore = useAuthStore();

// 반응형 참조 추출 (storeToRefs 사용)
const { isAuthenticated, loading, error } = storeToRefs(authStore);

// 액션 직접 접근
function handleLogout() {
  authStore.logout();
}
</script>

<template>
  <div>
    <p v-if="isAuthenticated">로그인 됨</p>
    <p v-else>로그인 필요</p>
    <button @click="handleLogout" :disabled="loading">로그아웃</button>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>
```

## Pinia와 SSR

이 프로젝트는 SSR과 Pinia를 통합하여 사용합니다:

### 1. 서버에서 Pinia 설정

```typescript
// entry-server.ts
import { createSSRApp } from 'vue';
import { renderToString } from 'vue/server-renderer';
import { App, router, pinia } from './app';

export async function render(url, context) {
  // 쿠키 정보 추출
  const cookieString = context?.headers?.cookie || context?.cookie || '';
  
  // 라우터 설정
  await router.push(url);
  await router.isReady();

  // SSR 앱 인스턴스 생성
  const app = createSSRApp(App);
  app.use(pinia);
  app.use(router);
  
  // 데이터 프리페칭 처리
  const matchedComponents = router.currentRoute.value.matched.flatMap(
    record => Object.values(record.components || {})
  );
  
  const asyncDataHooks = matchedComponents.map(Component => {
    const comp = Component as any;
    if (comp.ssrPrefetch) {
      return comp.ssrPrefetch({
        route: router.currentRoute.value,
        store: pinia,
        cookie: cookieString
      });
    }
    return null;
  }).filter(Boolean);
  
  await Promise.all(asyncDataHooks);

  // 앱을 문자열로 렌더링
  const appHtml = await renderToString(app);
  
  // Pinia 상태 추출
  const initialState = pinia.state.value;

  return { appHtml, initialState };
}
```

### 2. 클라이언트에서 상태 하이드레이션

```typescript
// entry-client.ts
import { createApp } from 'vue';
import { App, router, pinia } from './app';

// 서버에서 전달된 초기 상태가 있는지 확인
const initialState = (window as any).__INITIAL_STATE__;

// 초기 상태가 있으면 Pinia에 적용
if (initialState) {
  pinia.state.value = initialState;
}

// 클라이언트 측 앱 생성
const app = createApp(App);
app.use(pinia);
app.use(router);

// 초기 라우트가 해결되면 앱 마운트
router.isReady().then(() => {
  // SSR에서 hydrate 모드로 마운트하거나 CSR에서 일반 마운트
  app.mount('#app', initialState ? true : undefined);
});
```

## Pinia 스토어 설계 패턴

이 프로젝트에서 Pinia 스토어는 FSD 아키텍처에 맞춰 다음과 같은 패턴으로 사용됩니다:

### 1. 스토어 위치 및 접근 방식

```
src/
├── entities/          # 비즈니스 엔티티 스토어
│   └── user/
│       └── model/
│           └── userStore.ts
│
├── features/          # 기능별 스토어
│   └── auth/
│       └── model/
│           └── authStore.ts
```

주요 방식:
- 엔티티(entity) 레이어: 비즈니스 모델 관련 스토어 (User, Product 등)
- 기능(feature) 레이어: 사용자 상호작용 관련 스토어 (Auth, Profile 등)
- 임포트 경로: `import { useAuthStore } from '@features/auth'`

### 2. 스토어 초기화 및 생명주기

```typescript
// 앱 마운트 시 스토어 초기화
export default defineComponent({
  name: 'App',
  setup() {
    const authStore = useAuthStore();
    
    onMounted(() => {
      // 인증 상태 초기화
      authStore.initialize();
    });
    
    return {};
  }
});
```

### 3. SSR 대응

```typescript
// SSR을 고려한 조건부 코드
if (typeof window !== 'undefined') {
  // 브라우저 환경에서만 실행
  setTimeout(() => {
    initialize();
  }, 0);
} else {
  // 서버 환경 처리
  if (token.value) {
    authenticated.value = true;
  }
}
```

### 4. 인증 및 사용자 상태 관리

현재 프로젝트는 인증 관련 상태를 다음과 같이 처리합니다:

```typescript
// auth와 user 스토어 분리
const authStore = useAuthStore();
const userStore = useUserStore();

// 인증 처리 (토큰 관리)
authStore.setToken(token);

// 사용자 정보 처리
userStore.setUser(userData);

// 로그아웃 시 둘 다 초기화
authStore.clearAuth();
userStore.clearUser();
```

### 5. 공통 패턴: 로딩 및 오류 처리

모든 비동기 작업에 일관된 로딩 및 오류 처리 패턴을 적용합니다:

```typescript
async function fetchData() {
  // 1. 로딩 상태 설정
  loading.value = true;
  error.value = null;
  
  try {
    // 2. API 호출
    const response = await api.get('/some-endpoint');
    
    // 3. 상태 업데이트
    data.value = response.data;
    
    return response.data;
  } catch (err) {
    // 4. 오류 처리
    console.error('데이터 가져오기 실패:', err);
    error.value = err.message || '데이터를 가져오는 중 오류가 발생했습니다.';
    throw err;
  } finally {
    // 5. 로딩 상태 종료
    loading.value = false;
  }
}
```

### 6. 스토어 간 상호작용

스토어 간의 상호작용이 필요한 경우:

```typescript
// auth 스토어에서 user 스토어 사용
import { useUserStore } from '@entities/user';

export const useAuthStore = defineStore('auth', () => {
  // user 스토어 참조
  const userStore = useUserStore();
  
  async function logout() {
    // 1. auth 스토어 상태 초기화
    clearAuth();
    
    // 2. user 스토어 상태 초기화
    userStore.clearUser();
    
    // 3. 라우팅 처리
    router.push('/');
  }
  
  return {
    logout
  };
});
```

### 7. 타입 안전성 확보

TypeScript와 함께 Pinia를 사용하여 타입 안전성을 보장합니다:

```typescript
// 타입 정의
interface User {
  id: string;
  name: string;
  email: string;
}

export const useUserStore = defineStore('user', () => {
  // 명시적 타입 지정
  const user = ref<User | null>(null);
  
  // 게터도 타입이 자동으로 추론됨
  const userName = computed(() => user.value?.name || 'Guest');
  
  // 타입이 지정된 매개변수
  function setUser(userData: User) {
    user.value = userData;
  }
  
  return {
    user,
    userName,
    setUser
  };
});
```

### 8. 상태 지속성 및 복원

이 프로젝트는 인증 상태를 다음과 같이 지속하고 복원합니다:

```typescript
// 토큰을 로컬 스토리지와 쿠키에 저장
function setToken(newToken: string) {
  token.value = newToken;
  setAuthToken(newToken); // 여러 저장소에 토큰 저장
}

// 페이지 새로고침 시 상태 복원
function initialize() {
  recoverAuthState(); // 저장된 토큰 복원
  
  const persistedToken = getAuthToken();
  if (persistedToken) {
    token.value = persistedToken;
    authenticated.value = true;
    
    // 토큰이 있으면 사용자 정보 가져오기
    fetchCurrentUser();
  }
}
```

## 관련 문서

더 자세한 정보는 다음 문서들을 참조하세요:

- [개발 가이드](./development-guide.md) - 전반적인 개발 가이드라인
- [SSR 가이드](./ssr-guide.md) - 서버 사이드 렌더링 구현 상세 정보
- [하이브리드 렌더링 가이드](./hybrid-ssr-csr.md) - CSR과 SSR 혼합 사용 방법

### 외부 참고 자료

- [Pinia 공식 문서](https://pinia.vuejs.org/)
- [Vite 공식 문서](https://vitejs.dev/)
- [Vue.js 공식 문서](https://vuejs.org/)
- [Feature Sliced Design](https://feature-sliced.design/)
