# 인증 및 SSR 구현 가이드

이 문서는 Portfolio 프로젝트에서 인증(Authentication)과 SSR(Server-Side Rendering)을 구현하는 방법에 대한 상세 가이드를 제공합니다. 실제 프로젝트의 코드 구조와 구현 방식을 기반으로 작성되었습니다.

## 목차

1. [인증 구현](#인증-구현)
   - [인증 아키텍처](#인증-아키텍처)
   - [인증 스토어](#인증-스토어)
   - [인증 상태 관리](#인증-상태-관리)
   - [인증 가드](#인증-가드)
   - [OAuth 인증](#oauth-인증)
2. [SSR 구현](#ssr-구현)
   - [SSR 아키텍처](#ssr-아키텍처)
   - [SSR 라우팅 설정](#ssr-라우팅-설정)
   - [데이터 프리페칭](#데이터-프리페칭)
   - [SSR 관련 주의사항](#ssr-관련-주의사항)
3. [SSR과 인증 통합](#ssr과-인증-통합)
   - [서버 사이드 인증 처리](#서버-사이드-인증-처리)
   - [클라이언트 하이드레이션](#클라이언트-하이드레이션)

## 인증 구현

### 인증 아키텍처

인증 시스템은 FSD 아키텍처에 따라 다음과 같은 구성 요소로 이루어져 있습니다:

1. **인증 기능 모듈**: `features/auth`에 위치하며 인증 관련 기능을 제공합니다.
2. **사용자 엔티티**: `entities/user`에 위치하며 사용자 도메인 모델을 정의합니다.
3. **인증 관련 유틸리티**: `shared/lib`에 위치하며 토큰 관리, 인증 상태 복원 등의 기능을 제공합니다.

```
src/
├── features/
│   └── auth/
│       ├── api/             # 인증 API 통신
│       ├── model/           # 인증 상태 관리
│       ├── ui/              # 로그인/회원가입 UI
│       └── index.ts         # 공개 API
├── entities/
│   └── user/
│       ├── model/           # 사용자 도메인 모델
│       ├── api/             # 사용자 API 통신
│       └── index.ts         # 공개 API
└── shared/
    └── lib/
        ├── auth-utils.ts    # 인증 유틸리티
        └── github-login-utils.ts # GitHub 로그인 유틸리티
```

### 인증 스토어

인증 상태는 Pinia 스토어를 통해 관리됩니다. 실제 프로젝트에서는 사용자 스토어를 통해 인증 상태를 관리합니다:

```typescript
// entities/user/model/userStore.ts
import { defineStore } from 'pinia';
import { User } from './types';
import { userRepository } from '../api';
import { getAuthToken, removeAuthToken } from '@shared/lib/auth-utils';

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null as User | null,
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.currentUser,
    isAdmin: (state) => state.currentUser?.role === 'ADMIN',
  },
  
  actions: {
    // 현재 사용자 정보 가져오기
    async fetchCurrentUser() {
      if (!getAuthToken()) return;
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const user = await userRepository.getCurrentUser();
        this.currentUser = user;
      } catch (err: any) {
        this.error = err.message;
        this.currentUser = null;
        removeAuthToken();
      } finally {
        this.isLoading = false;
      }
    },
    
    // 로그인 처리
    async login(credentials: { email: string; password: string }) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const { user, token } = await userRepository.login(credentials);
        this.currentUser = user;
        // 토큰 저장
        localStorage.setItem('auth_token', token);
      } catch (err: any) {
        this.error = err.message;
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 로그아웃 처리
    logout() {
      this.currentUser = null;
      removeAuthToken();
      // 홈페이지로 리다이렉트 등 추가 처리
    },
    
    // 사용자 정보 설정 (SSR 등에서 사용)
    setUser(user: User) {
      this.currentUser = user;
    },
    
    // 사용자 정보 초기화
    clearUser() {
      this.currentUser = null;
    }
  }
});
```

### 인증 상태 관리

인증 상태 관리를 위한 유틸리티 함수들은 `shared/lib` 디렉토리에 구현되어 있습니다:

```typescript
// shared/lib/auth-utils.ts
// 토큰 저장 관련 유틸리티

// 인증 토큰 가져오기
export function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('auth_token');
}

// 인증 토큰 제거하기
export function removeAuthToken(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem('auth_token');
}

// 인증 상태 확인
export function isAuthenticated(): boolean {
  return !!getAuthToken();
}

// API 요청 시 인증 헤더 추가
export function addAuthHeader(headers: Record<string, string> = {}): Record<string, string> {
  const token = getAuthToken();
  if (token) {
    return {
      ...headers,
      Authorization: `Bearer ${token}`
    };
  }
  return headers;
}
```

### 인증 가드

라우터 가드를 사용하여 인증이 필요한 페이지에 대한 접근을 제어합니다:

```typescript
// app/router/routeConfig.ts
import { RouteRecordRaw } from 'vue-router';

// 라우트 구성
export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@pages/home'),
  },
  {
    path: '/profile',
    component: () => import('@pages/profile'),
    meta: { requiresAuth: true } // 인증 필요 표시
  },
  {
    path: '/admin',
    component: () => import('@pages/admin'),
    meta: { requiresAuth: true, requiresAdmin: true } // 관리자 권한 필요 표시
  },
  // 다른 라우트들...
];

// app/router/index.ts
import { createRouter, createWebHistory, createMemoryHistory } from 'vue-router';
import { routes } from './routeConfig';
import { useUserStore } from '@entities/user';

export function createAppRouter(mode = 'client') {
  const router = createRouter({
    history: mode === 'client' ? createWebHistory() : createMemoryHistory(),
    routes
  });
  
  // 인증 가드 설정
  router.beforeEach((to, from, next) => {
    const userStore = useUserStore();
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);
    
    // 인증이 필요한 페이지인지 확인
    if (requiresAuth && !userStore.isAuthenticated) {
      // 로그인 페이지로 리다이렉트
      next({
        path: '/auth/login',
        query: { redirect: to.fullPath }
      });
    } 
    // 관리자 권한이 필요한 페이지인지 확인
    else if (requiresAdmin && !userStore.isAdmin) {
      // 권한 없음 페이지 또는 홈으로 리다이렉트
      next({ path: '/unauthorized' });
    } 
    else {
      // 정상 진행
      next();
    }
  });
  
  return router;
}
```

### OAuth 인증

GitHub 소셜 로그인을 위한 OAuth 인증 구현:

```typescript
// shared/lib/github-login-utils.ts
import { getAuthToken } from './auth-utils';

// GitHub 로그인 URL 생성
export function getGitHubLoginUrl(): string {
  const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_GITHUB_REDIRECT_URI;
  
  return `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}`;
}

// GitHub 로그인 처리
export function handleGitHubLogin() {
  window.location.href = getGitHubLoginUrl();
}

// GitHub 콜백 처리를 위한 함수 (features/auth/api/repository.ts에서 사용)
export async function processGitHubCallback(code: string) {
  const response = await fetch('/api/auth/github', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ code })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'GitHub 로그인 처리 중 오류가 발생했습니다.');
  }
  
  return await response.json();
}
```

GitHub 로그인 버튼 컴포넌트:

```vue
<!-- features/auth/ui/GitHubLoginButton.vue -->
<template>
  <button
    class="w-full flex justify-center items-center px-4 py-2 border rounded-md shadow-sm text-sm font-medium bg-gray-800 text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
    @click="handleLogin"
  >
    <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
      <!-- GitHub 아이콘 SVG 경로 -->
    </svg>
    GitHub로 로그인
  </button>
</template>

<script setup lang="ts">
import { handleGitHubLogin } from '@shared/lib/github-login-utils';

const handleLogin = () => {
  handleGitHubLogin();
};
</script>
```

## SSR 구현

### SSR 아키텍처

SSR 아키텍처는 다음과 같은 주요 컴포넌트로 구성됩니다:

1. **Express 서버**: `server.js` 파일에 구현된 서버 사이드 렌더링을 처리하는 Node.js 서버입니다.
2. **진입점 파일**: 클라이언트와 서버 각각의 진입점 파일이 필요합니다.
   - `entry-server.ts`: 서버 렌더링 진입점
   - `entry-client.ts`: 클라이언트 하이드레이션 진입점
3. **Vite SSR 플러그인**: Vite의 SSR 기능을 활용합니다.

```
frontend/
├── server.js         # Express 서버
├── src/
│   ├── entry-server.ts # 서버 진입점
│   ├── entry-client.ts # 클라이언트 진입점
│   └── app/            # 공통 앱 코드
└── vite.config.ts      # Vite 설정
```

### SSR 라우팅 설정

SSR을 적용할 페이지는 별도의 페이지 컴포넌트를 만들고, SSR 데이터 프리페칭 로직을 구현합니다:

```typescript
// src/pages/ssr/ui/SsrPage.vue
<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">SSR 데모 페이지</h1>
    
    <div v-if="loading" class="flex justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
    </div>
    
    <div v-else-if="error" class="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded">
      {{ error }}
    </div>
    
    <div v-else class="bg-white shadow-md rounded-lg p-6">
      <!-- SSR로 렌더링된 컨텐츠 -->
      <p class="text-gray-700">이 페이지는 서버에서 렌더링되었습니다.</p>
      <p class="text-gray-700">현재 시간: {{ serverTime }}</p>
    </div>
  </div>
</template>

<script lang="ts">
// composition API를 사용하지 않고 옵션 API 형태로 구현
import { defineComponent } from 'vue';
import { useSsrStore } from '@pages/ssr/model/ssrStore';

export default defineComponent({
  name: 'SsrPage',
  
  // SSR 데이터 프리페칭 정적 메서드 (서버에서 호출됨)
  async serverPrefetch() {
    const ssrStore = useSsrStore();
    await ssrStore.fetchSsrData();
  },
  
  setup() {
    const ssrStore = useSsrStore();
    
    // 클라이언트에서 스토어 데이터가 없는 경우에만 데이터 로드
    if (!ssrStore.hasData && import.meta.env.SSR === false) {
      ssrStore.fetchSsrData();
    }
    
    return {
      loading: computed(() => ssrStore.loading),
      error: computed(() => ssrStore.error),
      serverTime: computed(() => ssrStore.serverTime)
    };
  }
});
</script>
```

### 데이터 프리페칭

SSR 데이터 프리페칭을 위한 스토어 구현:

```typescript
// src/pages/ssr/model/ssrStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { ssrApi } from '../api';

export const useSsrStore = defineStore('ssr', () => {
  // 상태
  const serverTime = ref<string | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터
  const hasData = computed(() => !!serverTime.value);
  
  // 액션
  async function fetchSsrData() {
    if (hasData.value) return; // 이미 데이터가 있으면 스킵
    
    loading.value = true;
    error.value = null;
    
    try {
      const data = await ssrApi.getSsrData();
      serverTime.value = data.serverTime;
    } catch (err: any) {
      error.value = err.message || 'SSR 데이터를 불러오는 중 오류가 발생했습니다.';
    } finally {
      loading.value = false;
    }
  }
  
  // 상태, 게터, 액션 반환
  return {
    serverTime,
    loading,
    error,
    hasData,
    fetchSsrData
  };
});
```

SSR API 클라이언트:

```typescript
// src/pages/ssr/api/index.ts
import { apiInstance } from '@shared/api';

interface SsrData {
  serverTime: string;
}

export const ssrApi = {
  async getSsrData(): Promise<SsrData> {
    const response = await apiInstance.get<SsrData>('/api/ssr-data');
    return response.data;
  }
};
```

### SSR 관련 주의사항

SSR 구현 시 주의해야 할 사항들:

1. **브라우저 API 접근**: `window`, `document` 등 브라우저 전용 API는 조건부로 사용해야 합니다.

```typescript
// 잘못된 예 - 서버에서 오류 발생
const isMobile = window.innerWidth < 768;

// 올바른 예
import { ref, onMounted } from 'vue';

const isMobile = ref(false);

onMounted(() => {
  isMobile.value = window.innerWidth < 768;
});

// 또는 SSR 여부 확인
const isBrowser = typeof window !== 'undefined';
const isMobile = isBrowser ? window.innerWidth < 768 : false;
```

2. **컴포넌트 라이프사이클**: `onMounted`, `onUpdated` 등은 서버에서 실행되지 않습니다.

```typescript
// SSR에서는 실행되지 않음
onMounted(() => {
  // 브라우저 환경에서만 실행되는 코드
});

// SSR 여부 확인
if (import.meta.env.SSR === false) {
  // 클라이언트에서만 실행되는 코드
}
```

3. **데이터 페칭**: SSR을 위한 데이터 페칭은 `serverPrefetch` 훅을 사용합니다.

```typescript
// 옵션 API 예시
export default {
  async serverPrefetch() {
    // 서버에서 데이터 로딩
    return this.$store.dispatch('fetchData');
  },
  
  mounted() {
    // 클라이언트에서 필요한 경우에만 데이터 로딩
    if (!this.$store.state.hasData) {
      this.$store.dispatch('fetchData');
    }
  }
};

// Composition API 예시 (setup 함수 내부)
const store = useStore();

// 서버 사이드 실행 여부 감지
if (import.meta.env.SSR) {
  // 서버에서 실행 시 전역 context에 prefetch 함수 등록
  const ssrContext = useSSRContext();
  ssrContext.modules = ssrContext.modules || [];
  ssrContext.modules.push(async () => {
    await store.fetchData();
  });
}
```

## SSR과 인증 통합

### 서버 사이드 인증 처리

서버에서 인증 상태를 처리하기 위해서는 클라이언트에서 서버로 인증 토큰을 전달해야 합니다. 쿠키를 사용하는 것이 가장 일반적인 방법입니다.

```typescript
// server.js (Express 서버 설정)
const express = require('express');
const fs = require('fs');
const path = require('path');
const cookieParser = require('cookie-parser');

const app = express();
app.use(cookieParser());

// SSR 핸들러
app.get('*', async (req, res) => {
  try {
    // 쿠키에서 인증 토큰 추출
    const authToken = req.cookies.auth_token;
    
    // SSR 렌더링 함수 호출 시 쿠키 정보 전달
    const { render } = require('./dist/server/entry-server.js');
    const { html, preloadLinks, initialState } = await render(req.url, {
      cookies: req.cookies,
      headers: req.headers
    });
    
    // HTML 템플릿에 결과 주입 및 응답
    const template = fs.readFileSync(path.resolve(__dirname, 'dist/client/index.html'), 'utf-8');
    
    const response = template
      .replace('<!--preload-links-->', preloadLinks)
      .replace('<!--app-html-->', html)
      .replace('<!--initial-state-->', `<script>window.__INITIAL_STATE__=${JSON.stringify(initialState)}</script>`);
    
    res.status(200).set({ 'Content-Type': 'text/html' }).end(response);
  } catch (e) {
    console.error(e.stack);
    res.status(500).end(e.stack);
  }
});
```

### 클라이언트 하이드레이션

클라이언트에서 서버 렌더링된 페이지를 하이드레이션하는 방법:

```typescript
// entry-client.ts
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './app/App.vue';
import { createAppRouter } from './app/router';

// 앱 생성
const app = createApp(App);
const pinia = createPinia();
const router = createAppRouter('client');

app.use(pinia);
app.use(router);

// 서버에서 전달받은 초기 상태 복원
if (window.__INITIAL_STATE__) {
  pinia.state.value = JSON.parse(JSON.stringify(window.__INITIAL_STATE__));
}

// 라우터 준비가 완료되면 앱 마운트
router.isReady().then(() => {
  app.mount('#app');
});
```

SSR 서버 진입점:

```typescript
// entry-server.ts
import { createSSRApp } from 'vue';
import { createPinia } from 'pinia';
import { renderToString } from 'vue/server-renderer';
import App from './app/App.vue';
import { createAppRouter } from './app/router';
import { useUserStore } from '@entities/user';

export async function render(url, context) {
  // 앱 생성
  const app = createSSRApp(App);
  const pinia = createPinia();
  const router = createAppRouter('server');
  
  app.use(pinia);
  app.use(router);
  
  // 인증 토큰이 있는 경우 사용자 정보 로드
  const userStore = useUserStore(pinia);
  if (context.cookies && context.cookies.auth_token) {
    try {
      // 서버 사이드에서 사용자 정보 로드 (헤더에 토큰 포함)
      await userStore.fetchCurrentUser();
    } catch (err) {
      console.error('Failed to load user data:', err);
    }
  }
  
  // 라우팅 처리
  await router.push(url);
  await router.isReady();
  
  // 페이지 컴포넌트의 serverPrefetch 실행
  const matchedComponents = router.currentRoute.value.matched
    .map(record => Object.values(record.components))
    .flat();
  
  // 데이터 프리페칭
  try {
    await Promise.all(
      matchedComponents.map(component => {
        if (component.serverPrefetch) {
          return component.serverPrefetch();
        }
        return Promise.resolve();
      })
    );
  } catch (error) {
    console.error('Error during data prefetching:', error);
  }
  
  // 앱 렌더링
  const html = await renderToString(app);
  
  // Pinia 상태 직렬화
  const initialState = pinia.state.value;
  
  return {
    html,
    initialState
  };
}
```
