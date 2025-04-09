import { createApp } from 'vue';
import { App, router, pinia } from './app';
import './shared/assets/styles/main.css';

// 서버에서 전달된 초기 상태가 있는지 확인
const initialState = (window as any).__INITIAL_STATE__;

// 초기 상태가 있으면 Pinia에 적용
if (initialState) {
  pinia.state.value = initialState;
}

// 클라이언트 측 앱 생성
const app = createApp(App);

// 스토어와 라우터 사용
app.use(pinia);
app.use(router);

// 초기 라우트가 해결되면 앱 마운트
router.isReady().then(() => {
  // SSR에서 hydrate 모드로 마운트하거나 CSR에서 일반 마운트
  app.mount('#app', initialState ? true : undefined);
});
