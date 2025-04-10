/**
 * 쿠키 관련 문제 디버깅을 위한 유틸리티
 */
import { getAuthToken } from './auth';
import { APP_CONFIG } from '@shared/config';
import Cookies from 'universal-cookie';

/**
 * document.cookie 프로퍼티에 디버깅 래퍼 추가
 * - 쿠키가 변경될 때마다 로그 출력
 */
export function setupCookieDebug(): void {
  if (typeof document === 'undefined') return;
  
  // universal-cookie 인스턴스 생성
  const cookiesInstance = new Cookies();
  
  // 원본 cookie descriptor 가져오기
  const originalDesc = Object.getOwnPropertyDescriptor(Document.prototype, 'cookie') || 
                     Object.getOwnPropertyDescriptor(HTMLDocument.prototype, 'cookie');
  
  if (!originalDesc) {
    return;
  }
  
  // 새 descriptor 정의
  Object.defineProperty(document, 'cookie', {
    get: function() {
      return originalDesc.get?.call(this);
    },
    set: function(val) {
      // auth_token 관련 쿠키인 경우에도 로깅 제거
      return originalDesc.set?.call(this, val);
    },
    configurable: true
  });
}

/**
 * 페이지 새로고침/이동 시 쿠키 유지 상태 모니터링
 */
export function monitorCookiePersistence(): void {
  if (typeof window === 'undefined') return;
  
  // universal-cookie 인스턴스 생성
  const cookiesInstance = new Cookies();
  
  // 페이지 로드 시 쿠키 상태 확인
  const checkCookies = () => {
    // 콘솔 로그 제거됨
  };
  
  // 페이지 언로드 시 쿠키 상태 기록
  const storeBeforeUnload = () => {
    const authTokenExists = cookiesInstance.get(APP_CONFIG.tokenKey) !== undefined;
    sessionStorage.setItem('lastCookieState', JSON.stringify({
      hasCookies: Object.keys(cookiesInstance.getAll()).length > 0,
      hasAuthToken: authTokenExists,
      timestamp: new Date().toISOString()
    }));
  };
  
  // 페이지 로드 시 이전 상태와 비교
  const compareCookieStates = () => {
    const lastStateStr = sessionStorage.getItem('lastCookieState');
    if (lastStateStr) {
      try {
        const lastState = JSON.parse(lastStateStr);
        const currentAuthToken = cookiesInstance.get(APP_CONFIG.tokenKey) !== undefined;
        
        // 이전 로그 제거됨
      } catch (e) {
        // 오류 로그 제거됨
      }
    }
  };
  
  // 이벤트 리스너 등록
  window.addEventListener('load', () => {
    checkCookies();
    compareCookieStates();
  });
  
  window.addEventListener('beforeunload', storeBeforeUnload);
  
  // 최초 실행 시 현재 상태 확인
  checkCookies();
}

/**
 * 새로고침 시 쿠키 복원 메커니즘 설정
 */
export function setupCookieRestoration(): void {
  if (typeof window === 'undefined' || typeof localStorage === 'undefined') return;
  
  // universal-cookie 인스턴스 생성
  const cookiesInstance = new Cookies();
  
  // 페이지 로드 시 로컬스토리지에서 쿠키 복원
  const restoreFromLocalStorage = () => {
    const token = localStorage.getItem(APP_CONFIG.tokenKey);
    const hasTokenCookie = cookiesInstance.get(APP_CONFIG.tokenKey) !== undefined;
    
    // 로컬스토리지에 토큰이 있지만 쿠키에 없는 경우 복원
    if (token && !hasTokenCookie) {
      // 쿠키 복원 (30일 만료, 루트 경로)
      cookiesInstance.set(APP_CONFIG.tokenKey, token, {
        path: '/',
        maxAge: 30 * 24 * 60 * 60, // 30일 (초 단위)
        sameSite: 'lax',
        secure: window.location.protocol === 'https:'
      });
    }
  };
  
  // 페이지 로드 시 실행
  window.addEventListener('load', restoreFromLocalStorage);
  
  // 5초마다 자동 검사 (선택적)
  const interval = setInterval(restoreFromLocalStorage, 5000);
  
  // 페이지 언로드 시 인터벌 정리
  window.addEventListener('beforeunload', () => {
    clearInterval(interval);
  });
  
  // 최초 실행
  restoreFromLocalStorage();
}