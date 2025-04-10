import { createApp } from 'vue';
import { App, router, pinia } from './app';
import './shared/assets/styles/main.css';
import { getAuthToken } from '@shared/lib/auth';
import { setupCookieDebug, monitorCookiePersistence, setupCookieRestoration } from '@shared/lib/cookieDebug';
import { recoverAuthState, backupAuthState, verifyAndSyncAuthState } from '@shared/lib/authPersistence';

// 서버에서 전달된 초기 상태가 있는지 확인
const initialState = (window as any).__INITIAL_STATE__;

// 개발 모드에서만 쿠키 디버깅 활성화
if (import.meta.env.DEV) {
  setupCookieDebug();
  monitorCookiePersistence();
}

// 쿠키 자동 복원 메커니즘 활성화 (프로덕션/개발 모두)
setupCookieRestoration();

// 새로고침 전 인증 상태 백업 및 새로고침 후 복원
window.addEventListener('beforeunload', backupAuthState);
window.addEventListener('load', () => {
  recoverAuthState();
  verifyAndSyncAuthState();
});

// 쿠키 디버깅 정보 출력
console.log('클라이언트 초기화 시작', {
  hasInitialState: !!initialState,
  cookieState: document.cookie,
  authToken: getAuthToken()
});

// 초기 상태가 있으면 Pinia에 적용
if (initialState) {
  pinia.state.value = initialState;
  console.log('초기 상태 복원 완료');
}

// 클라이언트 측 앱 생성
const app = createApp(App);

// 스토어와 라우터 사용
app.use(pinia);
app.use(router);

// 로딩 표시 관련 DOM 엘리먼트
const appElement = document.getElementById('app');
const loadingElement = document.getElementById('app-loading');

// CSS 파일 로드 완료 여부 추적
const cssLoaded = () => {
  // 모든 스타일시트 로드 여부 확인
  return Array.from(document.styleSheets).some(sheet => {
    try {
      // 스타일시트 규칙에 접근(CORS 이슈 방지)
      if (sheet.cssRules) return true;
    } catch (e) {
      const error = e as Error;
      // CORS 오류는 무시
      if (error.name !== 'SecurityError') console.error(e);
    }
    return false;
  });
};

// 초기 라우트가 해결되면 앱 마운트
router.isReady().then(() => {
  // SSR에서 hydrate 모드로 마운트하거나 CSR에서 일반 마운트
  app.mount('#app', initialState ? true : undefined);
  
  // 마운트 후 쿠키 상태 확인 (디버깅용)
  console.log('앱 마운트 완료', { 
    currentCookies: document.cookie,
    authToken: getAuthToken() 
  });
  
  // CSS와 앱이 로드되면 애니메이션 적용
  if (appElement && loadingElement) {
    // CSS가 이미 로드되었거나 로딩이 완료될 때까지 최대 300ms 대기
    const maxWait = 300;
    const startTime = Date.now();
    
    const checkCssAndShow = () => {
      if (cssLoaded() || Date.now() - startTime > maxWait) {
        // 로딩 애니메이션 숨기기
        loadingElement.style.display = 'none';
        // 앱 표시
        appElement.classList.remove('app-initializing');
      } else {
        // 다시 확인
        requestAnimationFrame(checkCssAndShow);
      }
    };
    
    // CSS 로드 확인 시작
    requestAnimationFrame(checkCssAndShow);
  }
});
