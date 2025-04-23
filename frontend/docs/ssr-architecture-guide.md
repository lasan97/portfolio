# SSR (Server-Side Rendering) 가이드

이 문서는 Portfolio 프로젝트에서 구현된 SSR(Server-Side Rendering) 시스템에 대한 설명을 제공합니다.

## 목차
1. [SSR 개요](#ssr-개요)
2. [아키텍처](#아키텍처)
3. [SSR 활성화 방법](#ssr-활성화-방법)
4. [데이터 프리페칭](#데이터-프리페칭)
5. [하이드레이션 과정](#하이드레이션-과정)
6. [SSR 디버깅](#ssr-디버깅)

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

```
frontend/
├── server.js           # Express 서버
├── src/
│   ├── entry-server.ts # 서버 진입점
│   ├── entry-client.ts # 클라이언트 진입점
│   └── app/            # 공유 앱 코드
└── index.html          # HTML 템플릿
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
  
  // SSR 데이터 프리페칭
  static async ssrPrefetch({ route, store, cookie }) {
    const introductionStore = useIntroductionStore(store);
    await introductionStore.fetchIntroduction();
    return { success: true };
  },
  
  // 컴포넌트 설정...
});
```

## 하이드레이션 과정

하이드레이션은 서버에서 렌더링된 정적 HTML에 클라이언트 측 JavaScript가 연결되는 과정입니다:

1. 서버는 초기 HTML과 함께 `window.__INITIAL_STATE__`를 제공합니다.
2. `entry-client.ts`는 이 초기 상태를 Pinia 스토어에 적용합니다.
3. `app.mount('#app', true)`는 Vue 앱을 하이드레이션 모드로 마운트합니다.

```typescript
// entry-client.ts의 핵심 부분
const initialState = (window as any).__INITIAL_STATE__;

if (initialState) {
  pinia.state.value = initialState;
}

app.mount('#app', initialState ? true : undefined);
```

## SSR 디버깅

SSR 디버깅을 위한 방법:

1. **환경 변수 설정**: `.env.local` 파일에서 `VITE_SSR_DEBUG=true`로 설정하여 상세 로그를 활성화합니다.

2. **서버 로그 확인**: 개발 모드에서 SSR 서버는 요청 URL, SSR/CSR 렌더링 결정, 쿠키 및 헤더 정보 등을 로깅합니다.

```bash
# 디버깅 모드로 SSR 서버 실행
VITE_SSR_DEBUG=true npm run dev:ssr
```
