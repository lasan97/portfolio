# SSR 구현 가이드

이 문서는 Vue 3 + Vite 환경에서 SSR(Server-Side Rendering) 및 하이브리드 렌더링을 구현하는 방법에 대한 가이드입니다.

## 목차

1. [SSR 개요](#ssr-개요)
2. [SSR 구현 아키텍처](#ssr-구현-아키텍처)
3. [하이브리드 CSR/SSR 구현](#하이브리드-csrssr-구현)
4. [데이터 페칭 전략](#데이터-페칭-전략)
5. [개발 및 배포 가이드](#개발-및-배포-가이드)
6. [성능 최적화](#성능-최적화)
7. [주의사항](#주의사항)
8. [문제 해결](#문제-해결)

## SSR 개요

### SSR이란?

SSR(Server-Side Rendering)은 서버에서 웹 페이지를 완전히 렌더링한 후 클라이언트에 전송하는 기술입니다. 이는 초기 페이지 로드 시 서버에서 페이지의 HTML을 생성하여 클라이언트에 제공하는 방식으로 작동합니다.

### SSR의 장점

- **향상된 초기 로딩 속도**: 사용자는 JavaScript 번들이 로드되기 전에도 콘텐츠를 볼 수 있습니다.
- **개선된 SEO**: 검색 엔진은 클라이언트 렌더링보다 서버 렌더링된 콘텐츠를 더 잘 인덱싱합니다.
- **소셜 미디어 최적화**: 소셜 미디어 플랫폼에서 공유할 때 메타 태그가 올바르게 표시됩니다.
- **성능 향상**: 특히 저사양 기기나 느린 네트워크 환경에서 사용자 경험이 개선됩니다.

### SSR vs CSR

|                 | 서버 사이드 렌더링 (SSR) | 클라이언트 사이드 렌더링 (CSR) |
|-----------------|-------------------------|------------------------------|
| 초기 로드 시간    | 더 빠름                  | 더 느림                      |
| SEO             | 우수함                   | 제한적                        |
| 서버 부하        | 더 높음                  | 더 낮음                      |
| 인터랙티브 지연   | 약간 있음                | 없음                         |
| 개발 복잡성      | 더 복잡함                | 덜 복잡함                    |

## SSR 구현 아키텍처

### 프로젝트 구조

```
frontend/
├── src/
│   ├── entry-client.ts     # 클라이언트 진입점
│   ├── entry-server.ts     # 서버 진입점
│   ├── app/                # 애플리케이션 코드
│   ├── ...
├── server.js               # Express SSR 서버
├── index.html              # HTML 템플릿
└── vite.config.ts          # Vite 설정
```

### 핵심 컴포넌트

1. **entry-client.ts**: 클라이언트 측 하이드레이션을 담당합니다.
   ```typescript
   import { createApp } from 'vue';
   import { App, router, pinia } from './app';

   // 서버에서 전달된 초기 상태가 있으면 적용
   if (window.__INITIAL_STATE__) {
     pinia.state.value = window.__INITIAL_STATE__;
   }

   // 라우터 준비 후 앱 마운트
   router.isReady().then(() => {
     createApp(App)
       .use(pinia)
       .use(router)
       .mount('#app');
   });
   ```

2. **entry-server.ts**: 서버 측 렌더링을 담당합니다.
   ```typescript
   import { createSSRApp } from 'vue';
   import { renderToString } from 'vue/server-renderer';
   import { App, router, pinia } from './app';

   export async function render(url) {
     // 라우터 설정
     await router.push(url);
     await router.isReady();

     // 앱 생성
     const app = createSSRApp(App);
     app.use(pinia);
     app.use(router);

     // 렌더링 수행
     const appHtml = await renderToString(app);
     const state = pinia.state.value;

     return { appHtml, state };
   }
   ```

3. **server.js**: Express 서버에서 SSR을 처리합니다.
   ```javascript
   const express = require('express');
   const { createServer: createViteServer } = require('vite');
   
   async function createServer() {
     const app = express();
     const vite = await createViteServer({ server: { middlewareMode: true } });
     
     app.use(vite.middlewares);
     
     app.use('*', async (req, res) => {
       const url = req.originalUrl;
       
       try {
         // HTML 템플릿 로드
         let template = fs.readFileSync('index.html', 'utf-8');
         template = await vite.transformIndexHtml(url, template);
         
         // 서버 진입점 가져오기
         const { render } = await vite.ssrLoadModule('/src/entry-server.ts');
         
         // 앱 렌더링
         const { appHtml, state } = await render(url);
         
         // HTML 응답 생성
         const html = template
           .replace('<div id="app"></div>', `<div id="app">${appHtml}</div>`)
           .replace('<!--initial-state-->', `<script>window.__INITIAL_STATE__=${JSON.stringify(state)}</script>`);
         
         res.status(200).set({ 'Content-Type': 'text/html' }).end(html);
       } catch (e) {
         vite.ssrFixStacktrace(e);
         console.error(e);
         res.status(500).end(e.message);
       }
     });
     
     app.listen(3000);
   }
   
   createServer();
   ```

4. **index.html**: 서버와 클라이언트 모두에서 사용되는 HTML 템플릿입니다.
   ```html
   <!DOCTYPE html>
   <html lang="ko">
   <head>
     <meta charset="UTF-8">
     <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <title>Vue SSR 애플리케이션</title>
     <!--preload-links-->
   </head>
   <body>
     <div id="app"></div>
     <!--initial-state-->
     <script type="module" src="/src/entry-client.ts"></script>
   </body>
   </html>
   ```

## 하이브리드 CSR/SSR 구현

하이브리드 접근 방식은 일부 페이지는 SSR로, 다른 페이지는 CSR로 처리할 수 있게 해줍니다.

### 라우트 기반 선택적 SSR

```javascript
// server.js에서
const SSR_ROUTES = ['/ssr', '/about', '/product'];

app.use('*', async (req, res) => {
  const url = req.originalUrl;
  
  // SSR 적용 여부 결정
  const shouldSSR = SSR_ROUTES.some(route => url.startsWith(route));
  
  if (shouldSSR) {
    // SSR 로직 실행
    // ...
  } else {
    // CSR을 위한 빈 HTML 템플릿 제공
    // ...
  }
});
```

### 라우터 메타데이터 활용

```typescript
// router/index.ts에서
const routes = [
  {
    path: '/',
    component: HomePage
    // SSR 적용하지 않음 (기본값)
  },
  {
    path: '/ssr',
    component: SsrPage,
    meta: { ssr: true } // SSR 적용
  }
];

// server.js에서 활용
const shouldSSR = router.currentRoute.value.meta.ssr;
```

## 데이터 페칭 전략

### 서버에서 데이터 프리페칭

#### 방법 1: `onServerPrefetch` 훅 사용

```typescript
<script setup>
import { ref, onMounted, onServerPrefetch } from 'vue';

const items = ref([]);
const loading = ref(true);

async function fetchData() {
  loading.value = true;
  items.value = await fetchItems();
  loading.value = false;
}

// 서버에서 데이터 미리 가져오기
onServerPrefetch(async () => {
  await fetchData();
});

// 클라이언트에서 데이터 누락 시 다시 가져오기
onMounted(() => {
  if (items.value.length === 0) {
    fetchData();
  }
});
</script>
```

#### 방법 2: 컴포넌트 옵션 사용

```typescript
<script>
export default {
  name: 'ProductPage',
  
  ssrPrefetch({ route, store }) {
    // 제품 ID 가져오기
    const productId = route.params.id;
    // 데이터 가져오기
    return store.product.fetchProduct(productId);
  }
}
</script>
```

### 스토어 상태 하이드레이션

```typescript
// entry-server.ts
export async function render(url) {
  // ...
  const initialState = pinia.state.value;
  return { appHtml, initialState };
}

// entry-client.ts
const initialState = window.__INITIAL_STATE__;
if (initialState) {
  pinia.state.value = initialState;
}
```

## 개발 및 배포 가이드

### 개발 환경 설정

```bash
# 개발 서버 실행 (CSR 모드)
npm run dev

# SSR 개발 서버 실행
npm run dev:ssr
```

### 빌드 및 프로덕션 배포

```bash
# 클라이언트 및 서버 번들 빌드
npm run build:ssr

# 프로덕션 SSR 서버 실행
npm run serve:ssr
```

### 패키지 스크립트 설정

```json
{
  "scripts": {
    "dev": "vite",
    "dev:ssr": "node server",
    "build": "vite build",
    "build:ssr": "npm run build:client && npm run build:server",
    "build:client": "vite build --outDir dist/client",
    "build:server": "vite build --ssr src/entry-server.ts --outDir dist/server",
    "serve:ssr": "cross-env NODE_ENV=production node server"
  }
}
```

## 성능 최적화

### 캐싱 전략

```javascript
// 간단한 인메모리 캐시
const cache = new Map();
const cacheTTL = 60 * 1000; // 1분

app.use('*', async (req, res) => {
  const url = req.originalUrl;
  
  // 캐시 확인
  const cacheEntry = cache.get(url);
  if (cacheEntry && Date.now() - cacheEntry.timestamp < cacheTTL) {
    return res.send(cacheEntry.html);
  }
  
  // 렌더링 수행
  const html = await renderPage(url);
  
  // 캐시 저장
  cache.set(url, { html, timestamp: Date.now() });
  
  res.send(html);
});
```

### 코드 분할

```typescript
// 동적 임포트를 사용한 라우트 수준 코드 분할
const routes = [
  {
    path: '/',
    component: () => import('@pages/home')
  },
  {
    path: '/profile',
    component: () => import('@pages/profile')
  }
];
```

### Prefetch/Preload 최적화

```typescript
// SSR 진입점에서 필요한 자원 프리로드
export async function render(url, manifest) {
  // ...
  
  // 현재 경로에 필요한 자원 결정
  const matched = router.currentRoute.value.matched;
  const preloadLinks = [];
  
  // 매니페스트에서 필요한 리소스 찾기
  for (const component of matched) {
    if (component.name && manifest[component.name]) {
      preloadLinks.push(`<link rel="modulepreload" href="${manifest[component.name]}">`);
    }
  }
  
  return { appHtml, preloadLinks, state };
}
```

## 주의사항

1. **window 및 document 접근**: 서버에서는 window나 document 객체가 없으므로, 이에 의존하는 코드는 조건부로 실행해야 합니다.

```typescript
if (typeof window !== 'undefined') {
  // 클라이언트 측에서만 실행할 코드
}
```

2. **데이터 페칭**: 데이터 페칭은 서버에서 발생한 후 클라이언트에 전달되어야 합니다.

3. **상태 관리**: Pinia 스토어 상태가 서버에서 클라이언트로 전달되도록 설정해야 합니다.

## 문제 해결

### 일반적인 문제 및 해결 방법

#### 1. 하이드레이션 불일치 오류

**증상**: 콘솔에 "Hydration error"와 관련된 경고가 표시됩니다.
**원인**: 서버에서 렌더링된 HTML과 클라이언트 측 Virtual DOM이 일치하지 않습니다.

**해결 방법**:
- 서버와 클라이언트 간 조건부 렌더링 일치 확인
- 날짜, 랜덤 값 등 비결정적 값 사용 주의
- 컴포넌트에서 `v-if`와 `v-show` 올바르게 사용

#### 2. 브라우저 API 접근 오류

**증상**: 서버에서 "window is not defined" 등의 오류가 발생합니다.
**원인**: 서버에서 실행 중일 때 브라우저 API를 접근하려고 시도합니다.

**해결 방법**:
```typescript
// 브라우저 환경 확인 후 접근
const isBrowser = typeof window !== 'undefined';
if (isBrowser) {
  // 브라우저 API 사용
  window.localStorage.getItem('token');
}
```

#### 3. 외부 라이브러리 호환성 문제

**증상**: 특정 라이브러리가 SSR 환경에서 작동하지 않습니다.
**원인**: 라이브러리가 브라우저 API에 직접 의존하고 있습니다.

**해결 방법**:
- SSR 호환 라이브러리 사용
- 라이브러리를 동적으로 임포트하여 클라이언트에서만 로드
```typescript
onMounted(async () => {
  // 클라이언트에서만 로드
  const { Chart } = await import('chart.js');
  // 차트 초기화
});
```

#### 4. 데이터 페칭 이슈

**증상**: 서버에서 데이터를 가져오지만 클라이언트에 전달되지 않습니다.
**원인**: Pinia 상태가 클라이언트로 올바르게 전달되지 않았습니다.

**해결 방법**:
- `__INITIAL_STATE__` 올바르게 전달되는지 확인
- 비동기 데이터 페칭이 완료될 때까지 기다리는지 확인

```typescript
// entry-server.ts
export async function render(url) {
  // 라우터 설정
  await router.push(url);
  
  // 비동기 데이터 페칭 대기
  await Promise.all(router.currentRoute.value.matched.map(component => {
    if (component.components.default.ssrPrefetch) {
      return component.components.default.ssrPrefetch({
        route: router.currentRoute.value,
        store: pinia
      });
    }
  }).filter(Boolean));
  
  // 이제 렌더링 수행
  // ...
}
```

## 확장 및 개선 방안

- **코드 분할**: 경로 기반 코드 분할을 통해 성능 최적화
- **프리페치**: 링크에 마우스 오버 시 데이터 프리페치로 인터랙션 속도 향상
- **성능 모니터링**: 서버 및 클라이언트 렌더링 성능 모니터링 추가
- **스트리밍 SSR**: 대규모 페이지를 위한 스트리밍 렌더링 구현

## 참고 자료

- [Vue.js SSR 가이드](https://v3.vuejs.org/guide/ssr/introduction.html)
- [Vite SSR 문서](https://vitejs.dev/guide/ssr.html)
- [Pinia SSR 문서](https://pinia.vuejs.org/ssr/)
