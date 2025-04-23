# Vite와 Pinia 가이드

이 문서는 Portfolio 프로젝트에서 사용하는 Vite 빌드 도구와 Pinia 상태 관리 라이브러리에 대한 가이드를 제공합니다.

## 목차
1. [Vite 개요](#vite-개요)
2. [Vite 설정 파일](#vite-설정-파일)
3. [환경 변수 관리](#환경-변수-관리)
4. [Pinia 상태 관리](#pinia-상태-관리)
5. [Pinia 스토어 설계 패턴](#pinia-스토어-설계-패턴)

## Vite 개요

Vite는 Vue 팀에서 개발한 빠른 프론트엔드 빌드 도구입니다. ESM(ES Modules)을 활용하여 개발 서버를 매우 빠르게 시작하고, 효율적인 HMR(Hot Module Replacement)을 제공합니다.

### 주요 특징
- 빠른 개발 서버 시작
- 즉각적인 모듈 교체 (HMR)
- 최적화된 빌드 과정
- 기본적인 TypeScript 지원
- CSS 및 정적 자산 처리

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

## 환경 변수 관리

Vite는 `.env` 파일 시스템을 사용하여 환경 변수를 관리합니다:

1. **.env**: 모든 모드에 적용되는 기본 환경 변수
2. **.env.local**: 로컬 재정의 (Git에 커밋되지 않음)
3. **.env.development**: 개발 모드 환경 변수
4. **.env.production**: 프로덕션 모드 환경 변수

### 환경 변수 사용
```typescript
// VITE_ 접두사를 가진 환경 변수만 클라이언트 코드에서 접근 가능
const apiUrl = import.meta.env.VITE_API_URL;
const isDevMode = import.meta.env.DEV; // Vite 내장 환경 변수
```

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

export function createClientApp() {
  const app = createApp(App);
  app.use(pinia);
  app.use(router);
  return { app, router, pinia };
}
```

### Pinia 스토어 기본 구조

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useUserStore } from '@entities/user/model/userStore';

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const token = ref<string | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터
  const isAuthenticated = computed(() => !!token.value);
  
  // 액션
  function setToken(newToken: string) {
    token.value = newToken;
  }
  
  function clearAuth() {
    token.value = null;
  }
  
  // 기타 액션...
  
  return {
    token,
    loading,
    error,
    isAuthenticated,
    setToken,
    clearAuth,
    // 기타 반환값...
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
const { isAuthenticated, loading } = storeToRefs(authStore);

// 액션 직접 접근
function handleLogout() {
  authStore.logout();
}
</script>
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

### 2. 공통 패턴: 로딩 및 오류 처리

```typescript
async function fetchData() {
  loading.value = true;
  error.value = null;

  try {
    const response = await api.get('/some-endpoint');
    data.value = response.data;
    return response.data;
  } catch (err) {
    error.value = '데이터를 가져오는 중 오류가 발생했습니다.';
    throw err;
  } finally {
    loading.value = false;
  }
}
```

### 3. 스토어 간 상호작용

```typescript
// auth 스토어에서 user 스토어 사용
import { useUserStore } from '@entities/user';

export const useAuthStore = defineStore('auth', () => {
  const userStore = useUserStore();

  function logout() {
    clearAuth();
    userStore.clearUser();
  }

  return { logout };
});
```

### 4. 타입 안전성 확보

```typescript
interface User {
  id: string;
  name: string;
  email: string;
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null);
  
  function setUser(userData: User) {
    user.value = userData;
  }
  
  return { user, setUser };
});
```
