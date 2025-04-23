# 하이브리드 CSR/SSR 구현 가이드

이 문서는 Vue 3 + Vite 환경에서 하이브리드 CSR(Client-Side Rendering)/SSR(Server-Side Rendering) 구현 방법에 대한 가이드입니다.

## 개요

하이브리드 렌더링 방식은 다음과 같은 이점이 있습니다:

- SEO가 중요한 페이지(랜딩 페이지, 제품 페이지 등)는 SSR로 제공
- 인터랙티브하고 SEO가 덜 중요한 페이지(대시보드, 관리자 페이지 등)는 CSR로 제공
- 각 페이지의 특성에 맞는 최적의 렌더링 방식 선택 가능

## 구현 구조

하이브리드 CSR/SSR 구현은 다음 주요 컴포넌트로 구성됩니다:

1. **서버**
   - Express 서버가 요청 URL에 따라 SSR 또는 CSR 모드 결정
   - SSR 적용 경로: `/ssr`

2. **진입점 파일**
   - `entry-client.ts`: 클라이언트 측 하이드레이션 담당
   - `entry-server.ts`: 서버 측 렌더링 담당

## 작동 방식

1. 사용자가 요청을 보내면 서버는 요청 URL을 확인합니다.
2. `/ssr` 경로인 경우:
   - `entry-server.ts`를 사용하여 Vue 앱을 HTML 문자열로 렌더링
   - 필요한 컴포넌트의 데이터를 서버에서 미리 가져옴
   - 렌더링된 HTML과 초기 상태를 클라이언트에 전송
3. 다른 경로인 경우:
   - 빈 HTML 템플릿을 제공하여 CSR로 처리

## 사용 방법

### 특정 페이지를 SSR로 설정하기

라우터 설정 파일에서 `meta: { ssr: true }` 속성을 추가하여 특정 페이지를 SSR로 지정할 수 있습니다:

```typescript
const routes = [
  {
    path: '/ssr',
    name: 'ssr',
    component: SsrPage,
    meta: { ssr: true } // SSR로 처리할 페이지
  },
  // 다른 페이지들은 CSR로 처리됨
];
```

## 서버 사이드 데이터 페칭

SSR 페이지에서 서버 사이드 데이터 페칭을 구현하는 두 가지 방법:

### 1. `onServerPrefetch` 훅 사용

```typescript
import { onServerPrefetch } from 'vue';

// Composition API 사용 시
onServerPrefetch(async () => {
  await fetchSomeData();
});
```

### 2. `ssrPrefetch` 옵션 사용

```javascript
export default {
  name: 'SsrPage',

  // Options API 사용 시
  ssrPrefetch({ route, store }) {
    return store.someModule.fetchData();
  },

  setup() {
    // 컴포넌트 로직
  }
}
```

## 주의사항

1. **윈도우/문서 객체 접근**: SSR 환경에서는 `window`나 `document` 객체가 없으므로, 조건부로 접근해야 합니다.

```typescript
if (typeof window !== 'undefined') {
  // 클라이언트 측에서만 실행할 코드
}
```

2. **데이터 상태 관리**: SSR과 CSR 간의 상태 일관성을 유지하는 것이 중요합니다.

```typescript
// 클라이언트에서 서버 상태 재사용
if (window.__INITIAL_STATE__) {
  pinia.state.value = window.__INITIAL_STATE__;
}
```
