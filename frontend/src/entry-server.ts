import { createSSRApp } from 'vue';
import { renderToString } from 'vue/server-renderer';
import { App, router, pinia } from './app';

export { router };

export async function render(url: string, context?: any, manifest?: any) {
  // 서버에서 전달된 쿠키 정보
  const cookieString = context?.headers?.cookie || context?.cookie || '';
  
  // 디버깅용 로그
  if (import.meta.env.VITE_SSR_DEBUG === 'true') {
    console.log('[SSR] 쿠키 문자열:', cookieString);
  }
  
  // 라우터 설정
  await router.push(url);
  await router.isReady();

  // SSR 앱 인스턴스 생성
  const app = createSSRApp(App);
  app.use(pinia);
  app.use(router);

  // 필요한 경우 데이터 프리페칭
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

  // 프리로드 링크 생성 (프로덕션 모드에서 사용)
  let preloadLinks = '';
  if (manifest) {
    // manifest를 사용하여 현재 경로에 필요한 리소스를 프리로드
    const matchedComponents = router.currentRoute.value.matched;
    const matchedComponentsNames = matchedComponents.map(
      (record) => record.components?.default?.name || ''
    );

    const componentsToPreload = [];
    for (const component of matchedComponentsNames) {
      if (component && manifest[component]) {
        componentsToPreload.push(manifest[component]);
      }
    }

    // 매니페스트를 기반으로 프리로드 링크 생성
    preloadLinks = componentsToPreload
      .map((file) => {
        return `<link rel="modulepreload" href="${file}">`;
      })
      .join('');
  }

  return { appHtml, preloadLinks, initialState };
}
