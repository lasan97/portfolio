# FSD 개발 가이드

이 문서는 Feature-Sliced Design (FSD) 아키텍처를 사용하여 Vue 3 프론트엔드 애플리케이션을 개발하기 위한 가이드를 제공합니다.

## 목차
1. [FSD 아키텍처 개요](#fsd-아키텍처-개요)
2. [주요 개발 원칙](#주요-개발-원칙)
3. [새 기능 추가 가이드](#새-기능-추가-가이드)
4. [코드 예시](#코드-예시)
5. [Best Practices](#best-practices)

## FSD 아키텍처 개요

FSD(Feature-Sliced Design)는 프론트엔드 애플리케이션을 기능(feature) 단위로 분할하고, 각 기능을 여러 계층(layer)으로 구성하는 방법론입니다. 이 접근 방식은 코드 구조의 일관성, 가독성 및 확장성을 향상시킵니다.

### 주요 디렉토리 구조
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

## 주요 개발 원칙

### 1. 계층화 (Layering)
- 상위 레이어: `app` > `processes` > `pages` > `widgets` > `features` > `entities` > `shared`
- 각 계층은 자신보다 하위 계층에만 의존 가능 (단방향 의존성)
- 예: `features`는 `entities`와 `shared`를 임포트할 수 있지만, `pages`나 `widgets`를 임포트할 수 없음

### 2. 슬라이스 독립성 (Slice Independence)
- 각 슬라이스(기능)는 독립적이며 다른 슬라이스에 의존하지 않음
- 예: `features/auth/login`은 `features/auth/register`에 의존하지 않음
- 슬라이스 간 공유가 필요한 로직은 상위 슬라이스나 적절한 레이어로 이동

### 3. 공개 API (Public API)
- 각 모듈은 `index.ts` 파일을 통해서만 외부에 노출
- 직접적인 내부 파일 임포트 금지 (예: `@features/auth/login/ui/LoginForm.vue`)
- 올바른 예: `import { LoginForm } from '@features/auth/login'`

## 새 기능 추가 가이드

### 1. 기능 계층 결정
새 기능을 추가할 때 먼저 해당 기능이 속하는 계층(`features`, `entities`, `widgets` 등)을 결정합니다.

- **pages**: 라우트와 매핑되는 페이지 컴포넌트
- **processes**: 여러 기능이나 엔티티를 아우르는 비즈니스 프로세스
- **widgets**: 페이지 구성에 사용되는 독립적인 UI 블록
- **features**: 사용자 상호작용이 포함된 기능 단위
- **entities**: 비즈니스 로직과 데이터 모델
- **shared**: 재사용 가능한 유틸리티 및 UI 컴포넌트

### 2. 디렉토리 구조 생성
선택한 계층 내에 다음과 같은 구조로 디렉토리를 생성합니다:

```
features/auth/login/
├── api/                 # API 요청 관련 코드
│   ├── loginUser.ts     # 로그인 API 함수
│   └── index.ts         # API 내보내기
├── model/               # 상태 관리 코드
│   ├── loginStore.ts    # Pinia 스토어
│   └── index.ts         # 모델 내보내기
├── ui/                  # UI 컴포넌트
│   └── LoginForm.vue    # 로그인 폼 컴포넌트
├── lib/                 # (선택적) 유틸리티 함수
├── config/              # (선택적) 설정 파일
└── index.ts             # 공개 API
```

### 3. 모듈 공개 API 정의
모듈의 `index.ts` 파일에서 외부에 노출할 요소를 내보냅니다:

```typescript
// features/auth/login/index.ts
export { default as LoginForm } from './ui/LoginForm.vue';
export { useLoginStore } from './model/loginStore';
export * from './api';
```

## 코드 예시

### Composition API 사용 예시

```typescript
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@features/auth';

// 스토어 사용
const authStore = useAuthStore();

// 반응형 상태 정의
const email = ref('');
const password = ref('');
const errorMessage = ref('');

// 계산된 속성
const isFormValid = computed(() => 
  email.value.includes('@') && password.value.length >= 6
);

// 메서드
async function handleSubmit() {
  if (!isFormValid.value) {
    errorMessage.value = '유효하지 않은 입력입니다.';
    return;
  }
  
  try {
    await authStore.login({
      email: email.value,
      password: password.value
    });
  } catch (error: any) {
    errorMessage.value = error.message;
  }
}

// 생명주기 훅
onMounted(() => {
  // 컴포넌트 마운트 시 실행할 로직
});
</script>
```

### Pinia 스토어 구현 예시

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { loginUser } from '../api';
import type { LoginCredentials, User } from '@shared/types';

export const useAuthStore = defineStore('auth', () => {
  // 상태 (state)
  const user = ref<User | null>(null);
  const token = ref<string | null>(localStorage.getItem('token'));
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터 (getters)
  const isAuthenticated = computed(() => !!token.value);
  const userName = computed(() => user.value?.nickname || 'Guest');
  
  // 액션 (actions)
  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  }
  
  async function login(credentials: LoginCredentials) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await loginUser(credentials);
      user.value = response.data.user;
      setToken(response.data.token);
      return response;
    } catch (err: any) {
      error.value = err.message || '로그인 중 오류가 발생했습니다.';
      throw err;
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
    // 상태
    user,
    token,
    loading,
    error,
    
    // 게터
    isAuthenticated,
    userName,
    
    // 액션
    setToken,
    login,
    logout
  };
});
```

### API 요청 모듈 예시

```typescript
// features/auth/api/loginUser.ts
import { api } from '@shared/api';
import type { LoginCredentials, ApiResponse, AuthToken } from '@shared/types';

export async function loginUser(credentials: LoginCredentials): Promise<ApiResponse<AuthToken>> {
  return api.post('/auth/login', credentials);
}
```

```typescript
// features/auth/api/index.ts
export * from './loginUser';
```

## Best Practices

### 1. 올바른 계층 및 모듈 경계 유지
- 계층 간 의존성은 항상 하향식으로 유지 (상위 레이어 → 하위 레이어)
- 슬라이스 간 의존성을 최소화하고, 필요한 경우 공유 인터페이스를 사용

### 2. 효과적인 상태 관리
- Pinia 스토어를 도메인 또는 기능 단위로 분리
- 전역 상태와 로컬 상태의 경계를 명확히 구분
- Composition API와 Pinia를 함께 활용하여 효율적인 상태 관리 구현

### 3. 타입 안전성 확보
- TypeScript 타입을 모든 인터페이스, API 요청/응답, 스토어 상태에 적용
- 타입 정의는 해당 모듈 내부에 위치하거나 `shared/types`에 공유

### 4. 공개 API 관리
- 각 모듈은 명시적으로 필요한 요소만 내보내기
- 모듈의 내부 구현 세부 사항은 숨기기
- 공개 API는 안정적으로 유지하고, 변경 시 마이그레이션 경로 제공

### 5. 컴포넌트 설계
- 컴포넌트는 단일 책임 원칙을 따르고 응집도 높게 유지
- 프레젠테이션 컴포넌트와 컨테이너 컴포넌트를 분리
- 컴포넌트는 필요한 만큼만 상태와 로직을 포함

### 6. 환경 변수 사용
```typescript
// 올바른 사용 (Vite)
const apiUrl = import.meta.env.VITE_API_URL;
```

### 7. 경로 별칭 사용
```typescript
// 권장: 항상 경로 별칭 사용
import { Button } from '@shared/ui';

// 권장하지 않음: 상대 경로 사용
import { Button } from '../../shared/ui';
```

### 8. 문제 해결 가이드

#### Pinia 관련 오류
1. **defineStore 오류**:
   - Pinia 패키지가 올바르게 설치되었는지 확인
   - `createPinia`와 `app.use(pinia)` 호출이 있는지 확인

2. **구독 오류**:
   - `watchEffect` 또는 `watch`를 사용하여 스토어 상태 변화 감지
   - `storeToRefs`를 사용하여 반응형 참조 분해

3. **State 업데이트 안됨**:
   - ref/reactive를 사용하여 상태를 올바르게 초기화했는지 확인
   - 변이는 직접 수행하고 있는지 확인 (Vuex 모듈과 달리 커밋이 필요 없음)
   
4. **Reset 메서드 관련 오류**:
   - Setup 구문으로 작성된 Pinia 스토어는 자동 `$reset()` 지원 안함
   - 커스텀 reset 메서드 정의 필요:
   ```typescript
   // 상태 리셋 함수 구현
   function reset() {
     token.value = getAuthToken();
     loading.value = false;
     error.value = null;
     authenticated.value = !!token.value;
   }
   
   return {
     // ...
     reset  // reset 함수 추가
   };
   ```

#### SSR 관련 오류

1. **"window is not defined" 오류**:
   - 서버에서 브라우저 API 접근 시 발생
   - 모든 브라우저 API 사용 코드는 조건부 체크 필요:
   ```typescript
   if (typeof window !== 'undefined') {
     // 브라우저 API 사용 코드
   }
   ```

2. **로그인 상태가 새로고침 시 초기화**:
   - localStorage 대신 쿠키 기반 인증 사용
   - `auth.ts` 유틸리티에서 쿠키와 localStorage 모두 확인

3. **하이드레이션 불일치 오류**:
   - 서버와 클라이언트 렌더링 결과 차이로 발생
   - 비결정적 값(날짜, 랜덤 값 등) 사용 주의
   - 조건부 렌더링은 동일한 조건으로 서버/클라이언트 일치 필요#

