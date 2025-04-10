# Vite 및 Pinia 가이드

이 문서는 프로젝트에서 사용하는 Vite와 Pinia에 대한 상세 가이드를 제공합니다.

## 목차
1. [Vite 환경 설정](#vite-환경-설정)
2. [Vite 특화 기능](#vite-특화-기능)
3. [Pinia 상태 관리](#pinia-상태-관리)
4. [마이그레이션 노트](#마이그레이션-노트)
5. [문제 해결](#문제-해결)

## Vite 환경 설정

### 기본 환경 변수

Vite는 다음과 같은 환경 변수 파일을 지원합니다:

- `.env.production`: 프로덕션 환경에서만 적용되며 Git에 커밋되지 않음
- `.env.local`: 개발, 로컬 환경에만 적용되며 Git에 커밋되지 않음

### 환경 변수 형식

Vite에서 클라이언트 코드에서 환경 변수를 사용하려면 `VITE_` 접두사를 붙여야 합니다:

```
# .env
VITE_API_URL=http://localhost:8081/api
VITE_BASE_URL=http://localhost:8080
VITE_BASE_PORT=8080
VITE_GITHUB_CLIENT_ID=your_github_client_id
VITE_SSR_DEBUG=ture
```

### 환경 변수 접근 방법

환경 변수는 `import.meta.env` 객체를 통해 접근할 수 있습니다:

```typescript
const apiUrl = import.meta.env.VITE_API_URL;
const baseUrl = import.meta.env.VITE_BASE_URL;
```

## Vite 특화 기능

### 빠른 개발 서버

Vite는 esbuild 기반의 빠른 번들링과 개발 서버를 제공합니다. 개발 서버 실행 방법:

```bash
npm run dev
```

### 빌드 최적화

프로덕션 빌드는 Rollup으로 최적화되며, 다음 명령으로 실행합니다:

```bash
npm run build
```

다양한 환경에 맞게 빌드:

```bash
npm run build:dev    # 개발 환경 빌드
npm run build:prod   # 프로덕션 환경 빌드
```

### 경로 별칭

`vite.config.ts` 파일에서 경로 별칭이 다음과 같이 설정되어 있습니다:

```typescript
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
}
```

이 경로 별칭을 사용하여 코드를 임포트할 수 있습니다:

```typescript
import { Button } from '@shared/ui';
import { HomePage } from '@pages/home';
```

### HMR (Hot Module Replacement)

Vite는 기본적으로 빠른 HMR을 지원하므로, 코드를 수정하면 페이지 새로고침 없이 변경 사항이 적용됩니다.

## Pinia 상태 관리

### 스토어 생성

Pinia 스토어는 `defineStore` 함수를 사용하여 생성합니다:

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // 상태 (state)
  const user = ref(null);
  const token = ref(localStorage.getItem('token'));
  const loading = ref(false);
  
  // 게터 (getters)
  const isAuthenticated = computed(() => !!token.value);
  const userName = computed(() => user.value?.name || 'Guest');
  
  // 액션 (actions)
  function setToken(newToken) {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  }
  
  async function login(credentials) {
    loading.value = true;
    try {
      // 로그인 로직...
    } finally {
      loading.value = false;
    }
  }
  
  function logout() {
    token.value = null;
    user.value = null;
    localStorage.removeItem('token');
  }
  
  return {
    // 내보낼 상태, 게터, 액션들
    user, token, loading,
    isAuthenticated, userName,
    setToken, login, logout
  };
});
```

### 컴포넌트에서 스토어 사용

```vue
<template>
  <div>
    <div v-if="authStore.loading">로딩 중...</div>
    <div v-else>
      <p v-if="authStore.isAuthenticated">
        안녕하세요, {{ authStore.userName }}님!
        <button @click="handleLogout">로그아웃</button>
      </p>
      <p v-else>
        <button @click="navigateToLogin">로그인</button>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@features/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

function handleLogout() {
  authStore.logout();
  router.push('/');
}

function navigateToLogin() {
  router.push('/login');
}
</script>
```

### 상태 분해(Destructuring)

Pinia 스토어에서 상태를 분해할 때 반응성을 유지하려면 `storeToRefs`를 사용합니다:

```typescript
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@features/auth';

const authStore = useAuthStore();
// storeToRefs를 사용하여 반응성 유지
const { user, loading, isAuthenticated } = storeToRefs(authStore);
// 메서드는 그대로 사용 가능
const { login, logout } = authStore;
```

### 스토어 간 상호작용

스토어 간에는 직접 다른 스토어를 임포트하여 상호작용할 수 있습니다:

```typescript
import { useAuthStore } from '@features/auth';

export const useUserStore = defineStore('user', () => {
  const authStore = useAuthStore();
  
  async function fetchUserProfile() {
    if (!authStore.isAuthenticated) {
      throw new Error('인증이 필요합니다');
    }
    
    // 사용자 프로필 가져오기...
  }
  
  return { fetchUserProfile };
});
```

### 지속성(Persistence) 구현

로컬 스토리지를 사용하여 Pinia 상태를 지속적으로 유지하는 방법:

```typescript
// 상태 저장
function saveState() {
  localStorage.setItem('user', JSON.stringify(user.value));
}

// 상태 복원
function loadState() {
  const savedUser = localStorage.getItem('user');
  if (savedUser) {
    user.value = JSON.parse(savedUser);
  }
}

// 상태 변경 감지 및 저장
watch(() => user.value, saveState, { deep: true });

// 컴포넌트 마운트 시 상태 복원
onMounted(loadState);
```

## 마이그레이션 노트

### Vue CLI에서 Vite로 마이그레이션

프로젝트는 Vue CLI에서 Vite로 마이그레이션되었으며, 다음과 같은 변경 사항이 있습니다:

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

### Vuex에서 Pinia로 마이그레이션

1. **의존성 변경**:
   - Vuex 4 제거, Pinia 패키지 추가

2. **상태 관리 변경**:
   - Vuex 모듈 대신 Pinia 스토어 사용
   - mutations/actions 대신 함수 형태의 액션
   - mapState/mapGetters 대신 useStore() 훅 사용

3. **컴포지션 API 활용**:
   - `ref`, `computed` 등을 활용한 반응형 상태 관리
   - 컴포넌트에서 `useStore()` 훅을 사용하여 스토어 접근

## 문제 해결

### Vite 관련 문제

1. **빌드 오류**:
   - `node_modules` 디렉토리를 삭제하고 `npm install`을 다시 실행
   - 의존성 충돌이 있는지 확인

2. **HMR 작동 안 함**:
   - Vite 서버 재시작
   - Vue 컴포넌트가 Named Export가 아닌 Default Export 사용하는지 확인

3. **환경 변수 접근 오류**:
   - 환경 변수 이름에 `VITE_` 접두사가 붙어 있는지 확인
   - `.env` 파일이 올바른 위치에 있는지 확인

### Pinia 관련 문제

1. **defineStore 오류**:
   - Pinia 패키지가 올바르게 설치되었는지 확인 (`npm install pinia`)
   - `createPinia`와 `app.use(pinia)` 호출이 있는지 확인

2. **상태 반응성 상실**:
   - 스토어 상태를 분해할 때 `storeToRefs`를 사용했는지 확인
   - 중첩 객체를 업데이트할 때 정확한 반응형 API를 사용했는지 확인

3. **스토어 초기화 오류**:
   - `createPinia`가 앱 시작 시 한 번만 호출되었는지 확인
   - 스토어 ID가 고유한지 확인

4. **TypeScript 타입 오류**:
   - Pinia 상태 및 반환 값에 명시적인 타입 정의가 있는지 확인
   - 최신 버전의 Pinia와 TypeScript 사용 중인지 확인