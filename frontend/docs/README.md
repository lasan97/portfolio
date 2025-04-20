# Portfolio 프론트엔드 개발 가이드

이 문서는 Portfolio 프로젝트의 프론트엔드 개발에 대한 종합적인 가이드를 제공합니다. 인간 개발자와 AI 모두가 프로젝트를 이해하고 효율적으로 개발할 수 있도록 작성되었습니다.

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [아키텍처](#아키텍처)
3. [개발 환경 설정](#개발-환경-설정)
4. [코드 구조 및 조직](#코드-구조-및-조직)
5. [개발 워크플로우](#개발-워크플로우)
6. [상태 관리](#상태-관리)
7. [API 통신](#api-통신)
8. [컴포넌트 개발](#컴포넌트-개발)
9. [라우팅](#라우팅)
10. [테스트](#테스트)
11. [빌드 및 배포](#빌드-및-배포)
12. [문제 해결 및 디버깅](#문제-해결-및-디버깅)
13. [AI 개발자를 위한 가이드](#ai-개발자를-위한-가이드)

## 프로젝트 개요

Portfolio 프로젝트는 Vue 3, TypeScript, Pinia를 기반으로 한 모던 웹 애플리케이션입니다. 이 프로젝트는 Feature-Sliced Design(FSD) 아키텍처를 채택하여 코드의 확장성과 유지보수성을 높이고 있습니다.

### 주요 기술 스택

- **프레임워크**: Vue 3 (Composition API)
- **언어**: TypeScript
- **상태 관리**: Pinia
- **라우팅**: Vue Router
- **스타일링**: TailwindCSS
- **빌드 도구**: Vite
- **API 통신**: Axios
- **SSR 지원**: 하이브리드 렌더링 (CSR + SSR)

## 아키텍처

이 프로젝트는 Feature-Sliced Design(FSD) 아키텍처를 따릅니다. FSD는 코드를 기능 중심으로 조직화하여 확장성과 유지보수성을 높이는 아키텍처 방법론입니다.

### 레이어 구조

FSD는 다음과 같은 레이어로 구성됩니다(상위 -> 하위):

1. **app**: 애플리케이션 진입점, 전역 설정, 라우터/스토어 초기화
2. **processes**: 여러 페이지에 걸친 사용자 시나리오 및 복합 비즈니스 로직
3. **pages**: 특정 경로에 매핑되는 UI 컴포넌트
4. **widgets**: 독립적인 UI 블록, 여러 features와 entities를 조합
5. **features**: 사용자 인터랙션 기반의 독립적인 기능 단위
6. **entities**: 핵심 비즈니스 도메인 모델 및 관련 로직/UI
7. **shared**: 특정 도메인/기능에 종속되지 않는 공통 유틸리티, UI 키트, 설정 등

각 레이어는 자신보다 아래에 있는 레이어만 의존할 수 있습니다(단방향 의존성).

### 의존성 규칙

1. 상위 레이어는 하위 레이어를 참조할 수 있지만, 하위 레이어는 상위 레이어를 참조할 수 없습니다.
   - 예: `features`는 `entities`를 참조할 수 있지만, `entities`는 `features`를 참조할 수 없습니다.

2. 동일 레이어 내에서도 슬라이스 간 직접 참조는 제한됩니다.
   - 예: `features/auth`는 `features/profile`을 직접 참조할 수 없습니다.

3. 모든 레이어는 `shared`를 참조할 수 있습니다.

## 개발 환경 설정

### 필수 도구

- Node.js 16 이상
- npm 또는 yarn
- Git

### 설정 단계

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

## 코드 구조 및 조직

### 프로젝트 구조

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
│   ├── product/    # 제품 엔티티
│   └── cart/       # 장바구니 엔티티
└── shared/         # 공유 코드
    ├── ui/         # UI 컴포넌트
    ├── api/        # API 클라이언트
    ├── lib/        # 유틸리티
    └── config/     # 상수 및 설정
```

### 슬라이스 구조

각 슬라이스(예: `entities/user`)는 다음과 같은 내부 구조를 가질 수 있습니다:

```
entities/user/
├── model/          # 도메인 모델 및 상태
│   ├── types.ts    # 타입 정의
│   ├── constants.ts # 상수 및 열거형
│   └── userStore.ts # 상태 관리
├── api/            # API 관련 코드 (선택적)
│   ├── types.ts    # API 타입
│   ├── repository.ts # API 요청 함수
│   └── mappers.ts  # 데이터 변환 함수
├── ui/             # UI 컴포넌트
│   └── UserCard.vue # 사용자 카드 컴포넌트
└── index.ts        # 공개 API
```

### 공개 API 패턴

각 모듈은 `index.ts` 파일을 통해 공개 API를 정의합니다. 이를 통해 내부 구현 세부 사항을 캡슐화하고 모듈 간 의존성을 명확히 합니다.

```typescript
// entities/user/index.ts 예시
// 도메인 모델
export type { User, UserState } from './model/types';
export { UserRole } from './model/constants';

// UI 모델
export type { UserCardProps, UserDisplayProps } from './model/ui-types';

// 스토어
export { useUserStore } from './model/userStore';

// UI 컴포넌트
export { default as UserCard } from './ui/UserCard.vue';
```

## 개발 워크플로우

### 새 기능 개발 프로세스

1. **요구사항 분석**: 기능의 요구사항을 명확히 이해합니다.
2. **도메인 모델 정의**: 필요한 엔티티와 타입을 정의합니다.
3. **API 통신 구현**: 필요한 API 요청 함수를 구현합니다.
4. **상태 관리 구현**: Pinia 스토어를 사용하여 상태 관리를 구현합니다.
5. **UI 컴포넌트 개발**: 필요한 UI 컴포넌트를 개발합니다.
6. **페이지 통합**: 개발된 기능을 페이지에 통합합니다.
7. **테스트 및 검증**: 기능이 요구사항을 충족하는지 확인합니다.

### 코드 작성 가이드라인

1. **타입 안전성**: TypeScript의 타입 시스템을 최대한 활용하여 타입 안전성을 확보합니다.
2. **컴포지션 API**: Vue 3의 Composition API를 사용하여 로직을 구성합니다.
3. **단일 책임 원칙**: 각 함수, 컴포넌트, 모듈은 하나의 책임만 가져야 합니다.
4. **명확한 네이밍**: 변수, 함수, 컴포넌트 이름은 그 목적과 역할을 명확히 표현해야 합니다.
5. **일관된 코드 스타일**: 프로젝트의 코드 스타일 가이드라인을 따릅니다.

## 상태 관리

이 프로젝트는 Pinia를 사용하여 상태 관리를 구현합니다.

### 스토어 구성

스토어는 도메인/기능별로 분리되어 있으며, 주로 다음 위치에 정의됩니다:
- 엔티티 상태: `entities/{entity}/model/{entity}Store.ts`
- 기능 상태: `features/{feature}/model/store.ts`

### 스토어 구현 패턴

```typescript
// entities/user/model/userStore.ts 예시
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from './types';

export const useUserStore = defineStore('user', () => {
  // 상태(state)
  const user = ref<User | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터(getters)
  const isAuthenticated = computed(() => !!user.value);
  const userDisplayName = computed(() => user.value?.nickname || '사용자');
  
  // 액션(actions)
  function setUser(userData: User) {
    user.value = userData;
  }
  
  function clearUser() {
    user.value = null;
  }
  
  async function fetchUser(userId: number) {
    // 구현...
  }
  
  return {
    // 상태
    user,
    loading,
    error,
    
    // 게터
    isAuthenticated,
    userDisplayName,
    
    // 액션
    setUser,
    clearUser,
    fetchUser
  };
});
```

### 스토어 사용 패턴

```vue
<script setup lang="ts">
import { useUserStore } from '@entities/user';

const userStore = useUserStore();

// 상태 접근
const isLoggedIn = computed(() => userStore.isAuthenticated);
const username = computed(() => userStore.userDisplayName);

// 액션 호출
function logout() {
  userStore.clearUser();
}
</script>
```

## API 통신

이 프로젝트는 Axios를 사용하여 API 통신을 구현합니다.

### API 인스턴스

중앙 API 인스턴스는 `shared/api/instance.ts`에 정의되어 있으며, 인터셉터를 사용하여 요청/응답 처리를 표준화합니다.

```typescript
// shared/api/instance.ts
import axios from 'axios';

export const apiInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: true
});

// 요청 인터셉터 (토큰 추가 등)
apiInstance.interceptors.request.use(/* ... */);

// 응답 인터셉터 (에러 처리 등)
apiInstance.interceptors.response.use(/* ... */);
```

### 리포지토리 패턴

API 요청은 리포지토리 패턴을 사용하여 구현됩니다. 각 엔티티는 자체 리포지토리를 가질 수 있습니다.

```typescript
// entities/product/api/repository.ts
import { apiInstance } from '@shared/api';
import { ProductRequest, ProductResponse } from './types';
import { mapToProduct, mapToProducts } from './mappers';

export const productRepository = {
  // 상품 목록 조회
  async getProducts() {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  },
  
  // 단일 상품 조회
  async getProduct(id: number) {
    const response = await apiInstance.get<ProductResponse.Detail>(`/api/products/${id}`);
    return mapToProduct(response.data);
  },
  
  // 기타 API 요청 함수...
};
```

### 데이터 매핑

API 응답 데이터는 도메인 모델로 매핑되어야 합니다. 이를 위해 매퍼 함수를 사용합니다.

```typescript
// entities/product/api/mappers.ts
import { Product } from '../model/types';
import { ProductResponse } from './types';

export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  price: response.price,
  // 기타 매핑...
});

export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    id: response.id,
    name: response.name,
    price: response.price,
    // 기타 매핑...
  }));
```

## 컴포넌트 개발

### 컴포넌트 설계 원칙

1. **단일 책임 원칙**: 각 컴포넌트는 하나의 책임만 가져야 합니다.
2. **UI와 로직 분리**: 표현(UI)과 로직을 분리합니다.
3. **재사용성**: 공통 UI 요소는 `shared/ui`에 배치합니다.
4. **접근성**: 모든 컴포넌트는 웹 접근성 표준을 준수해야 합니다.

### Vue 컴포넌트 작성 패턴

```vue
<template>
  <!-- 템플릿 마크업 -->
  <div class="user-card">
    <h3>{{ user.name }}</h3>
    <p>{{ user.email }}</p>
    <button @click="handleClick">{{ buttonText }}</button>
  </div>
</template>

<script setup lang="ts">
// 임포트
import { computed } from 'vue';
import type { User } from '@entities/user';

// Props 정의
interface Props {
  user: User;
  buttonText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  buttonText: '상세 보기'
});

// Emits 정의
const emit = defineEmits<{
  (e: 'click', userId: number): void;
}>();

// 컴포넌트 로직
const handleClick = () => {
  emit('click', props.user.id);
};
</script>

<style scoped>
/* 컴포넌트 스코프 스타일 */
.user-card {
  /* 스타일 정의 */
}
</style>
```

### 컴포넌트 조직화

- **엔티티 컴포넌트**: 특정 도메인 모델과 관련된 기본 UI 컴포넌트 (`entities/{entity}/ui/`)
- **기능 컴포넌트**: 특정 기능과 관련된 UI 컴포넌트 (`features/{feature}/ui/`)
- **위젯 컴포넌트**: 여러 기능과 엔티티를 조합한 복합 UI 블록 (`widgets/{widget}/ui/`)
- **공유 컴포넌트**: 도메인에 종속되지 않는 재사용 가능한 UI 컴포넌트 (`shared/ui/`)

## 라우팅

이 프로젝트는 Vue Router를 사용하여 라우팅을 구현합니다.

### 라우트 구성

라우트는 `app/router/index.ts`에 정의되어 있으며, 경로 상수는 `shared/config`에 정의되어 있습니다.

```typescript
// shared/config/index.ts
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  PROFILE: '/profile',
  // 기타 경로...
};

// app/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { ROUTES } from '@shared/config';
import HomePage from '@pages/home/ui/HomePage.vue';
import ProfilePage from '@pages/profile/ui/ProfilePage.vue';

const routes = [
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

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 라우트 가드 설정
router.beforeEach((to, from, next) => {
  // 인증 검사 등...
});

export default router;
```

### 라우트 가드

라우트 가드를 사용하여 인증 및 권한 검사를 구현합니다.

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

### 빌드 명령어

```bash
# 개발 빌드
npm run build:dev

# 프로덕션 빌드
npm run build:prod

# SSR 빌드
npm run build:ssr
```

### 배포 전 체크리스트

- 환경 변수 검증
- 타입 체크 실행 (`npm run type-check`)
- 린트 검사 실행 (`npm run lint`)
- 테스트 통과 여부 확인
- 빌드 결과물 검증

## 문제 해결 및 디버깅

### 일반적인 문제 해결 방법

1. **콘솔 오류 확인**: 브라우저 개발자 도구의 콘솔 탭에서 오류 메시지를 확인합니다.
2. **네트워크 요청 검사**: 개발자 도구의 네트워크 탭에서 API 요청/응답을 검사합니다.
3. **Vue Devtools 사용**: Vue Devtools 확장 프로그램을 사용하여 컴포넌트 상태와 Pinia 스토어를 검사합니다.
4. **로깅 추가**: 문제가 발생하는 부분에 로깅을 추가하여 실행 흐름을 추적합니다.

### 디버깅 팁

- **Vue 컴포넌트 디버깅**: `<script setup>` 내에서 `debugger` 문을 사용하여 브레이크포인트를 설정할 수 있습니다.
- **Pinia 스토어 디버깅**: Vue Devtools를 사용하여 Pinia 스토어의 상태 변화를 관찰할 수 있습니다.
- **API 요청 디버깅**: Axios 인터셉터를 사용하여 모든 요청/응답을 로깅할 수 있습니다.

## AI 개발자를 위한 가이드

AI 개발자가 이 프로젝트에 효과적으로 기여하기 위한 가이드라인입니다.

### 코드베이스 탐색 전략

1. **아키텍처 이해**: FSD 아키텍처의 레이어 구조와 의존성 규칙을 이해합니다.
2. **공개 API 파악**: 각 모듈의 `index.ts` 파일을 통해 공개 API를 파악합니다.
3. **도메인 모델 분석**: `entities` 레이어의 도메인 모델을 분석하여 비즈니스 개념을 이해합니다.
4. **기능 흐름 추적**: 특정 기능의 흐름을 UI 컴포넌트부터 API 호출까지 추적합니다.

### 코드 생성 가이드라인

1. **아키텍처 준수**: FSD 아키텍처의 레이어 구조와 의존성 규칙을 준수합니다.
2. **타입 안전성**: TypeScript의 타입 시스템을 최대한 활용하여 타입 안전성을 확보합니다.
3. **일관된 패턴**: 기존 코드의 패턴과 일관성을 유지합니다.
4. **캡슐화**: 내부 구현 세부 사항을 캡슐화하고 공개 API를 명확히 정의합니다.
5. **테스트 가능성**: 생성된 코드가 테스트 가능하도록 설계합니다.

### 일반적인 개발 작업 예시

1. **새 엔티티 추가**:
   - `entities/{newEntity}` 디렉토리 생성
   - 도메인 모델 정의 (`model/types.ts`)
   - 필요한 경우 API 리포지토리 구현 (`api/repository.ts`)
   - 공개 API 정의 (`index.ts`)

2. **새 기능 추가**:
   - `features/{newFeature}` 디렉토리 생성
   - 기능별 모델 및 스토어 구현 (`model/`)
   - UI 컴포넌트 개발 (`ui/`)
   - 공개 API 정의 (`index.ts`)

3. **기존 기능 수정**:
   - 관련 모듈의 공개 API 파악
   - 내부 구현 분석 및 수정
   - 의존성 규칙 준수 확인
   - 타입 안전성 유지

### 코드 분석 및 이해를 위한 팁

1. **공개 API 우선**: 각 모듈의 `index.ts` 파일부터 분석하여 공개 API를 파악합니다.
2. **타입 정의 분석**: 타입 정의를 통해 도메인 모델과 데이터 구조를 이해합니다.
3. **스토어 로직 분석**: Pinia 스토어를 분석하여 상태 관리 로직을 이해합니다.
4. **컴포넌트 계층 구조 파악**: 컴포넌트 간의 계층 구조와 데이터 흐름을 파악합니다.