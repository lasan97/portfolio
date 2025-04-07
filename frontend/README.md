# Feature-Sliced Design (FSD) 아키텍처

## 개요

본 프로젝트는 Feature-Sliced Design (FSD) 아키텍처와 TypeScript를 사용하여 구성되었습니다. FSD는 프론트엔드 애플리케이션을 기능(feature) 단위로 분할하고, 각 기능을 여러 계층(layer)으로 구성하는 방법론입니다. 이 접근 방식은 코드 가독성, 유지보수성, 확장성을 향상시킵니다.

## 시작하기

### 설치

```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run serve

# 프로덕션용 빌드
npm run build

# 린트 실행
npm run lint
```

### 환경 설정

본 프로젝트는 다음과 같은 환경 설정 파일을 사용합니다:

- `.env`: 환경 변수 기본값
  ```
  NODE_ENV=development
  VUE_APP_API_URL=http://localhost:8081/api  # 백엔드 API URL
  VUE_APP_BASE_URL=http://localhost:8080     # 프론트엔드 URL
  ```
- `vue.config.ts`: Vue CLI 설정

환경 변수는 애플리케이션 전체에서 `process.env.VUE_APP_*` 형식으로 접근할 수 있습니다. 
개발/프로덕션 환경에 따라 다른 설정을 사용하려면 `.env.development`와 `.env.production` 파일을 추가로 생성할 수 있습니다.

## 알려진 문제 해결

### Vue 3 'process is not defined' 오류

이 프로젝트에서는 Vue.js의 reactivity 관련 "process is not defined" 오류가 발생할 수 있습니다. 이 문제를 해결하기 위해 다음과 같은 조치를 취했습니다:

1. **Process 객체 폴백 구현**:
   - `/src/shared/shims/process.ts` 파일이 process 객체가 없을 때 기본 구현을 제공합니다.
   - 이 파일은 `main.ts`에서 가장 먼저 임포트됩니다.
   - 주의: TypeScript에서는 `NODE_ENV` 타입을 'development' | 'production' | 'test' 유니온 타입으로 선언해야 하며, 값 할당 시 `as const`를 사용해야 합니다.

2. **Webpack DefinePlugin 설정**:
   - `vue.config.ts`에 webpack.DefinePlugin을 설정하여 process.env 객체를 명시적으로 정의했습니다.

3. **환경 변수 설정**:
   - `.env` 파일에 필요한 환경 변수를 정의했습니다.

만약 여전히 오류가 발생한다면 아래 명령을 실행해보세요:

```bash
npm cache clean --force
rm -rf node_modules
npm install
```

TypeScript 오류가 발생한다면:
- `/src/shared/shims/process.ts` 파일에서 타입 어서션이 올바르게 되어 있는지 확인하세요.
- ESLint 규칙에 맞게 `as const`를 사용했는지 확인하세요.

## 주요 원칙

1. **계층화 (Layering)**: 애플리케이션을 여러 책임 계층으로 분리
2. **슬라이스 독립성 (Slice Independence)**: 각 슬라이스는 독립적이며 다른 슬라이스에 의존하지 않음
3. **공개 API (Public API)**: 각 모듈은 `index.ts` 파일을 통해서만 외부에 노출
4. **단방향 의존성 (Unidirectional Dependencies)**: 하위 계층은 상위 계층에 의존할 수 없음

## 디렉토리 구조

```
src/
├── app/            # 애플리케이션 초기화, 전역 스타일, 프로바이더
│   ├── providers/  # 앱 프로바이더 (라우터, 스토어 등)
│   ├── styles/     # 전역 스타일
│   └── index.ts    # 앱 컴포넌트 내보내기
├── processes/      # 여러 기능 간의 프로세스 (인증 흐름 등)
├── pages/          # 페이지 컴포넌트
├── widgets/        # 복합 UI 블록 (헤더, 푸터, 사이드바 등)
├── features/       # 사용자 상호작용 기능 (로그인 폼, 제품 필터 등)
├── entities/       # 비즈니스 엔티티 (사용자, 제품, 주문 등)
└── shared/         # 공유 유틸리티, 라이브러리, UI 키트
    ├── api/        # API 클라이언트, 요청 유틸리티
    ├── config/     # 환경 변수, 앱 상수
    ├── lib/        # 외부 라이브러리 및 유틸리티
    ├── shims/      # 폴리필 및 타입 정의
    └── ui/         # UI 컴포넌트 (버튼, 입력 필드 등)
```

## 세그먼트

각 기능 슬라이스는 다음과 같은 세그먼트로 구성될 수 있습니다:

1. **ui**: 컴포넌트 및 시각적 요소
2. **model**: 상태 관리 로직 (Vuex 스토어, 컴포저블)
3. **api**: 외부 API와의 통신
4. **lib**: 유틸리티 함수
5. **config**: 설정 파일
6. **types**: TypeScript 타입 정의

## Vue 3 + TypeScript 사용 예시

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
    },
    count: {
      type: Number,
      default: 0
    }
  }
});
</script>
```

### Composition API 사용

```typescript
<script setup lang="ts">
import { ref, computed } from 'vue';

// 타입 정의
interface User {
  id: number;
  name: string;
  email: string;
}

// 상태 정의
const user = ref<User | null>(null);
const isAuthenticated = computed(() => user.value !== null);

// 메서드 정의
const setUser = (newUser: User) => {
  user.value = newUser;
};

// 외부로 노출
defineExpose({
  setUser
});
</script>
```

## 참고 자료

- [Feature-Sliced Design 공식 웹사이트](https://feature-sliced.design/)
- [Vue 3 + TypeScript 문서](https://v3.vuejs.org/guide/typescript-support.html)
- [Vue 환경 변수 가이드](https://cli.vuejs.org/guide/mode-and-env.html)
