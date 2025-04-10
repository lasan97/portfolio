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
    console.warn('쿠키 디버깅 설정 실패: cookie 속성을 찾을 수 없습니다.');
    return;
  }
  
  // 새 descriptor 정의
  Object.defineProperty(document, 'cookie', {
    get: function() {
      return originalDesc.get?.call(this);
    },
    set: function(val) {
      console.log('쿠키 변경 감지:', {
        설정값: val,
        현재시간: new Date().toISOString(),
        스택: new Error().stack
      });
      
      // auth_token 관련 쿠키인 경우 추가 로깅
      if (val.includes(`${APP_CONFIG.tokenKey}=`)) {
        console.warn('인증 토큰 쿠키 변경 감지!', {
          쿠키설정: val,
          현재인증상태: getAuthToken() ? '로그인됨' : '로그아웃됨',
          universal쿠키: cookiesInstance.getAll()
        });
      } 
      
      // 만료되는 쿠키인 경우 추가 로깅
      if (val.includes('expires=Thu, 01 Jan 1970')) {
        console.warn('쿠키 삭제 감지!', {
          삭제쿠키: val.split('=')[0],
          스택: new Error().stack?.split('\n').slice(1, 4).join('\n')
        });
      }
      
      return originalDesc.set?.call(this, val);
    },
    configurable: true
  });
  
  console.log('쿠키 디버깅 설정 완료');
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
    console.log('페이지 로드 완료 - 쿠키 상태:', {
      현재쿠키: cookiesInstance.getAll(),
      인증토큰: getAuthToken(),
      인증토큰쿠키: cookiesInstance.get(APP_CONFIG.tokenKey) !== undefined,
      페이지URL: window.location.href
    });
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
        
        // 이전에 토큰이 있었는데 지금 없으면 경고
        if (lastState.hasAuthToken && !currentAuthToken) {
          console.warn('⚠️ 페이지 새로고침 후 인증 토큰 쿠키가 사라짐!', {
            이전상태: lastState,
            현재쿠키: cookiesInstance.getAll()
          });
        }
      } catch (e) {
        console.error('쿠키 상태 비교 중 오류:', e);
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
  
  console.log('쿠키 지속성 모니터링 설정 완료');
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
      console.log('쿠키 복원: 토큰이 쿠키에서 누락됨, 복원 중...');
      
      // 쿠키 복원 (30일 만료, 루트 경로)
      cookiesInstance.set(APP_CONFIG.tokenKey, token, {
        path: '/',
        maxAge: 30 * 24 * 60 * 60, // 30일 (초 단위)
        sameSite: 'lax',
        secure: window.location.protocol === 'https:'
      });
      
      console.log('쿠키 복원 완료:', cookiesInstance.getAll());
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
  
  console.log('쿠키 자동 복원 설정 완료');
}