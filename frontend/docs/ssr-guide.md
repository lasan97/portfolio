# SSR (Server-Side Rendering) 가이드

이 문서는 Portfolio 프로젝트에서 구현된 SSR(Server-Side Rendering) 시스템에 대한 상세 설명과 사용 방법을 제공합니다.

## 목차
1. [SSR 개요](#ssr-개요)
2. [아키텍처](#아키텍처)
3. [SSR 구현 방식](#ssr-구현-방식)
4. [SSR 활성화 방법](#ssr-활성화-방법)
5. [데이터 프리페칭](#데이터-프리페칭)
6. [하이드레이션 과정](#하이드레이션-과정)
7. [SSR 디버깅](#ssr-디버깅)
8. [성능 최적화](#성능-최적화)
9. [관련 문서](#관련-문서)

## SSR 개요

SSR(Server-Side Rendering)은 서버에서 초기 HTML을 생성하여 클라이언트로 전송하는 방식입니다. 이를 통해 다음과 같은 이점을 얻을 수 있습니다:

- **초기 로딩 속도 개선**: 사용자는 JavaScript가 로드되기 전에 콘텐츠를 볼 수 있습니다.
- **SEO 향상**: 검색 엔진이 JavaScript를 실행하지 않아도 콘텐츠를 인덱싱할 수 있습니다.
- **메타데이터 제어**: 초기 HTML에 메타 태그를 포함할 수 있어 소셜 공유 시 미리보기가 올바르게 표시됩니다.

이 프로젝트는 특정 페이지에 대해서만 SSR을 적용하는 하이브리드 방식을 사용합니다.

## 아키텍처

SSR 아키텍처는 다음과 같은 주요 컴포넌트로 구성됩니다:

1. **Express 서버**: SSR 요청을 처리하고 HTML을 생성합니다. (`server.js`)
2. **entry-server.ts**: 서버 측 렌더링을 담당하는 진입점입니다.
3. **entry-client.ts**: 클라이언트 측 하이드레이션을 담당하는 진입점입니다.
4. **manifest.json**: 프로덕션 빌드 시 생성되는 자산 맵핑 파일입니다.

```
frontend/
├── server.js           # Express 서버
├── src/
│   ├── entry-server.ts # 서버 진입점
│   ├── entry-client.ts # 클라이언트 진입점
│   └── app/            # 공유 앱 코드
├── dist/
│   ├── client/         # 클라이언트 빌드 결과물
│   └── server/         # 서버 빌드 결과물
└── index.html          # HTML 템플릿
```

## SSR 구현 방식

이 프로젝트는 Vue 3와 Vite를 기반으로 한 커스텀 SSR 구현을 사용합니다:

1. 서버는 들어오는 요청을 받아 현재 URL을 분석합니다.
2. 라우터를 통해 해당 URL에 맞는 컴포넌트를 결정합니다.
3. 해당 컴포넌트에 `meta: { ssr: true }` 설정이 있는지 확인합니다.
4. SSR이 필요한 경우, 컴포넌트의 `ssrPrefetch` 훅을 실행하여 필요한 데이터를 미리 가져옵니다.
5. 앱을 HTML 문자열로 렌더링합니다.
6. 초기 상태(Pinia 스토어)를 직렬화하여 HTML에 포함시킵니다.
7. 렌더링된 HTML과 초기 상태를 클라이언트로 전송합니다.
8. 클라이언트에서는 하이드레이션 과정을 통해 이벤트 리스너를 연결하고 상호작용이 가능한 앱으로 전환합니다.

현재 코드에서 주요 구현 방식:

```javascript
// 서버 측 렌더링 로직 (entry-server.ts)
export async function render(url: string, context?: any, manifest?: any) {
  // 서버에서 전달된 쿠키 정보
  const cookieString = context?.headers?.cookie || context?.cookie || '';

  // 라우터 설정
  await router.push(url);
  await router.isReady();

  // SSR 앱 인스턴스 생성
  const app = createSSRApp(App);
  app.use(pinia);
  app.use(router);

  // 데이터 프리페칭
  const matchedComponents = router.currentRoute.value.matched.flatMap(
    record => Object.values(record.components || {})
  );

  // SSR 전용 데이터 프리페칭 처리
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

  return { appHtml, preloadLinks, initialState };
}
```

## SSR 활성화 방법

특정 페이지에 SSR을 적용하려면 다음과 같이 라우트 설정에 `ssr: true` 메타데이터를 추가하세요:

```typescript
// src/app/router/index.ts
const routes: Array<RouteRecordRaw> = [
  {
    path: '/introduction',
    name: 'introduction',
    component: IntroductionPage,
    meta: { ssr: true } // SSR 활성화
  },
  // 다른 라우트...
]
```

현재 프로젝트에서는 다음 페이지들이 SSR로 설정되어 있습니다:
- `/introduction` - 소개 페이지
- `/ssr` - SSR 데모 페이지

## 데이터 프리페칭

SSR 페이지에서 초기 데이터를 가져오려면 컴포넌트에 `ssrPrefetch` 정적 메소드를 구현하세요:

```typescript
// 컴포넌트 예시
export default defineComponent({
  name: 'IntroductionPage',
  components: { /* ... */ },

  // SSR 데이터 프리페칭
  static async ssrPrefetch({ route, store, cookie }) {
    // 쿠키 정보를 이용한 API 호출
    const introductionStore = useIntroductionStore(store);
    await introductionStore.fetchIntroduction();

    return { success: true };
  },

  // 컴포넌트 설정...
});
```

프로젝트에서 SSR 데이터 프리페칭 예시:

```typescript
// 서버에서만 실행되는 비동기 데이터 가져오기
static async ssrPrefetch({ route, store, cookie }) {
  try {
    // cookie 문자열을 사용하여 인증된 요청 수행
    const introStore = useIntroductionStore(store);

    // 인증 토큰 확인 (쿠키에서)
    if (cookie) {
      // 쿠키 문자열로부터 인증 토큰 추출 (필요한 경우)
      const authToken = getAuthToken(cookie);

      if (authToken) {
        // 서버 측에서 인증된 API 요청
        await introStore.fetchIntroduction();
      }
    }

    return { success: true };
  } catch (error) {
    console.error('SSR 데이터 프리페칭 오류:', error);
    return { error: true };
  }
}
```

## 하이드레이션 과정

하이드레이션은 서버에서 렌더링된 정적 HTML에 클라이언트 측 JavaScript가 연결되는 과정입니다:

1. 서버는 초기 HTML과 함께 `window.__INITIAL_STATE__`를 제공합니다.
2. `entry-client.ts`는 이 초기 상태를 Pinia 스토어에 적용합니다.
3. `app.mount('#app', true)`는 Vue 앱을 하이드레이션 모드로 마운트합니다.
4. Vue는 기존 DOM 노드를 재사용하면서 이벤트 리스너를 연결합니다.

```typescript
// entry-client.ts의 핵심 부분
const initialState = (window as any).__INITIAL_STATE__;

// 초기 상태가 있으면 Pinia에 적용
if (initialState) {
  pinia.state.value = initialState;
}

// SSR에서 hydrate 모드로 마운트
app.mount('#app', initialState ? true : undefined);
```

## SSR 디버깅

SSR 디버깅을 위해 다음과 같은 도구를 사용할 수 있습니다:

1. **환경 변수 설정**: `.env.local` 파일에서 `VITE_SSR_DEBUG=true`로 설정하여 상세 로그를 활성화합니다.

2. **서버 로그 확인**: 개발 모드에서 SSR 서버는 다음과 같은 정보를 로깅합니다:
   - 요청 URL
   - SSR/CSR 렌더링 결정
   - 쿠키 및 헤더 정보
   - 렌더링 오류

3. **클라이언트-서버 불일치 확인**: 하이드레이션 과정에서 오류가 발생하면 콘솔에서 "Hydration mismatch" 경고를 확인하세요.

일반적인 SSR 디버깅 프로세스:

```bash
# 디버깅 모드로 SSR 서버 실행
VITE_SSR_DEBUG=true npm run dev:ssr
```

## 성능 최적화

SSR 성능을 최적화하기 위한 방법:

1. **선택적 SSR**: 모든 페이지가 아닌 필요한 페이지만 SSR을 사용합니다.

2. **데이터 요청 최소화**: `ssrPrefetch`에서 필요한 데이터만 가져오세요.

3. **컴포넌트 캐싱**: 동일한 컴포넌트를 여러 번 렌더링할 경우 결과를 캐싱하는 것을 고려하세요.

4. **최적화된 빌드**: 프로덕션 모드에서는 `npm run build:ssr`를 사용하여 최적화된 빌드를 생성하세요.

5. **CSS 최적화**: 중요한 CSS는 인라인으로 포함하고, 나머지는 비동기 로드를 고려하세요.

## 관련 문서

더 자세한 정보는 다음 문서를 참조하세요:

- [하이브리드 렌더링 가이드](./hybrid-ssr-csr.md) - CSR과 SSR 함께 사용하기
- [Vite와 Pinia 가이드](./vite-pinia-guide.md) - Vite와 Pinia 통합 방법
