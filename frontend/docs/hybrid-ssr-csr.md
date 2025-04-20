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

3. **컴포넌트**
   - SSR이 필요한 컴포넌트: `onServerPrefetch` 훅 또는 `ssrPrefetch` 옵션 사용
   - CSR 컴포넌트: 일반 Vue 컴포넌트로 작성

## 작동 방식

1. 사용자가 요청을 보내면 서버는 요청 URL을 확인합니다.
2. `/ssr` 경로인 경우:
   - `entry-server.ts`를 사용하여 Vue 앱을 HTML 문자열로 렌더링
   - 필요한 컴포넌트의 데이터를 서버에서 미리 가져옴
   - 렌더링된 HTML과 초기 상태를 클라이언트에 전송
3. 다른 경로인 경우:
   - 빈 HTML 템플릿을 제공하여 CSR로 처리
4. 클라이언트에서는 `entry-client.ts`가:
   - SSR 모드: 서버에서 제공한 초기 상태를 사용하여 하이드레이션
   - CSR 모드: 일반적인 SPA 렌더링 수행

## 사용 방법

### 개발 모드

개발 중에는 일반적인 SPA 방식으로 작업할 수 있습니다:

```bash
npm run dev
```

### 하이브리드 모드 빌드 및 실행

하이브리드 CSR/SSR 모드로 애플리케이션을 빌드하고 실행하려면:

```bash
# 클라이언트 및 서버 번들 빌드
npm run build:ssr

# 하이브리드 서버 실행
npm run serve:hybrid
```

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
  // 서버에서만 실행되는 데이터 페칭 로직
  await fetchSomeData();
});
```

### 2. `ssrPrefetch` 옵션 사용

```javascript
export default {
  name: 'SsrPage',

  // Options API 사용 시
  ssrPrefetch({ route, store }) {
    // 서버에서 실행될 데이터 페칭 로직
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

2. **외부 라이브러리**: 브라우저 API에 의존하는 라이브러리는 서버에서 오류를 일으킬 수 있습니다. 이를 방지하기 위해:

```typescript
// 브라우저에서만 임포트
let someLibrary;
if (typeof window !== 'undefined') {
  someLibrary = require('some-browser-only-library');
}
```

3. **데이터 상태 관리**: SSR과 CSR 간의 상태 일관성을 유지하는 것이 중요합니다.

```typescript
// 클라이언트에서 서버 상태 재사용
if (window.__INITIAL_STATE__) {
  pinia.state.value = window.__INITIAL_STATE__;
}
```

4. **인증 상태 처리**: 쿠키 기반 인증을 사용하여 SSR 환경에서도 인증 상태를 유지합니다.

## 성능 최적화 팁

1. **선택적 SSR**: 모든 페이지가 SSR을 필요로 하지 않습니다. SEO가 중요한 페이지에만 적용하세요.

2. **캐싱 전략**: 자주 변경되지 않는 페이지의 SSR 결과를 캐싱하여 서버 부하를 줄입니다.

```javascript
// 간단한 메모리 캐시 예시
const ssrCache = new Map();

app.use('*', async (req, res) => {
  const url = req.originalUrl;

  // 캐시된 결과가 있으면 사용
  if (ssrCache.has(url)) {
    return res.send(ssrCache.get(url));
  }

  // SSR 렌더링 수행
  const html = await renderPage(url);

  // 결과 캐싱 (TTL 설정 가능)
  ssrCache.set(url, html);

  res.send(html);
});
```

3. **코드 분할**: 라우트 기반 코드 분할을 통해 클라이언트 번들 크기를 최적화합니다.

## 문제 해결

### 일반적인 문제

1. **하이드레이션 불일치**: 서버와 클라이언트의 렌더링 결과가 다른 경우 발생합니다.
   - 해결: 조건부 렌더링에 `v-if`만 사용하고, 데이터 비동기 처리를 올바르게 처리하세요.

2. **서버에서의 API 호출 실패**: 환경 변수나 API 엔드포인트 설정 문제일 수 있습니다.
   - 해결: 서버와 클라이언트 환경에서 모두 작동하는 API 클라이언트를 구성하세요.

3. **`window is not defined` 오류**: 서버에서 브라우저 API에 접근하려고 할 때 발생합니다.
   - 해결: 모든 브라우저 API 접근을 조건부 코드로 래핑하세요.

## 참고 자료

### 프로젝트 내부 문서
- [SSR 가이드](./ssr-guide.md) - 서버 사이드 렌더링 구현 상세 정보
- [Vite와 Pinia 가이드](./vite-pinia-guide.md) - Vite와 Pinia 통합 방법

### 외부 문서
- [Vue.js SSR 가이드](https://v3.vuejs.org/guide/ssr/introduction.html)
- [Vite SSR 문서](https://vitejs.dev/guide/ssr.html)
- [Pinia SSR 문서](https://pinia.vuejs.org/ssr/)
