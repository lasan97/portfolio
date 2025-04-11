// 통합된 인증 유틸리티 - universal-cookie 기반
import { APP_CONFIG } from '@shared/config';
import Cookies from 'universal-cookie';

// 인증 토큰 관련 상수
const TOKEN_KEY = APP_CONFIG.tokenKey;
const USER_INFO_KEY = APP_CONFIG.userStorageKey;
const TOKEN_EXPIRY_DAYS = 30;

/**
 * 인증 토큰 저장 (쿠키, localStorage, sessionStorage)
 */
export function setAuthToken(token: string): boolean {
  try {
    if (typeof window === 'undefined') return false;
    
    // 1. 쿠키에 저장
    const cookies = new Cookies();
    
    // 쿠키 옵션 설정
    const options: any = {
      path: '/',
      maxAge: TOKEN_EXPIRY_DAYS * 24 * 60 * 60, // 초 단위
      sameSite: 'lax'
    };
    
    // HTTPS 환경에서는 secure 플래그 설정
    if (window.location.protocol === 'https:') {
      options.secure = true;
    }
    
    cookies.set(TOKEN_KEY, token, options);
    
    // 2. localStorage에 저장 (백업)
    localStorage.setItem(TOKEN_KEY, token);
    
    // 3. sessionStorage에 저장 (세션 백업)
    sessionStorage.setItem(TOKEN_KEY, token);
    
    return true;
  } catch (error) {
    return false;
  }
}

/**
 * 인증 토큰 가져오기 (쿠키 우선, 그 다음 localStorage, sessionStorage)
 */
export function getAuthToken(cookieString?: string): string | null {
  try {
    // 1. 쿠키에서 토큰 조회
    let cookieToken = null;
    
    // 서버에서 cookieString이 전달된 경우
    if (cookieString) {
      const cookies = new Cookies(cookieString);
      cookieToken = cookies.get(TOKEN_KEY);
    } 
    // 클라이언트에서 document.cookie 사용
    else if (typeof window !== 'undefined') {
      const cookies = new Cookies();
      cookieToken = cookies.get(TOKEN_KEY);
    }
    
    // 쿠키에 토큰이 있으면 반환
    if (cookieToken) return cookieToken;
    
    // 2. 쿠키에 없으면 localStorage, sessionStorage 확인 (클라이언트만)
    if (typeof window !== 'undefined') {
      // localStorage 확인
      const localStorageToken = localStorage.getItem(TOKEN_KEY);
      if (localStorageToken) {
        // localStorage에 있지만 쿠키에 없으면 쿠키에도 복원
        setAuthToken(localStorageToken);
        return localStorageToken;
      }
      
      // sessionStorage 확인
      const sessionStorageToken = sessionStorage.getItem(TOKEN_KEY);
      if (sessionStorageToken) {
        // sessionStorage에 있지만 쿠키에 없으면 쿠키에도 복원
        setAuthToken(sessionStorageToken);
        return sessionStorageToken;
      }
    }
    
    // 토큰을 찾을 수 없음
    return null;
  } catch (error) {
    return null;
  }
}

/**
 * 모든 저장소에서 인증 정보 삭제
 */
export function logout(): boolean {
  try {
    // 브라우저 환경이 아니면 무시
    if (typeof window === 'undefined') return false;
    
    // 1. 쿠키에서 삭제
    const cookies = new Cookies();
    cookies.remove(TOKEN_KEY, { path: '/' });
    cookies.remove(USER_INFO_KEY, { path: '/' });
    
    // 2. localStorage에서 삭제
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_INFO_KEY);
    
    // 3. sessionStorage에서 삭제
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_INFO_KEY);
    
    // 4. 특수한 경우를 위해 document.cookie를 직접 조작
    if (typeof document !== 'undefined') {
      try {
        // 도메인 속성 없이 삭제
        document.cookie = `${TOKEN_KEY}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
        document.cookie = `${USER_INFO_KEY}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
        
        // 도메인 속성과 함께 삭제 (일부 환경에서 필요)
        if (window.location.hostname !== 'localhost') {
          document.cookie = `${TOKEN_KEY}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`;
          document.cookie = `${USER_INFO_KEY}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`;
        }
      } catch (e) {
        // 오류 무시
      }
    }
    
    return true;
  } catch (error) {
    return false;
  }
}

/**
 * 사용자가 인증되었는지 확인
 */
export function isAuthenticated(cookieString?: string): boolean {
  return !!getAuthToken(cookieString);
}

/**
 * 사용자 정보를 저장하는 함수
 */
export function setUserInfo(userInfo: any): boolean {
  try {
    if (typeof window === 'undefined') return false;
    
    const userInfoStr = JSON.stringify(userInfo);
    
    // 1. 쿠키에 저장
    const cookies = new Cookies();
    cookies.set(USER_INFO_KEY, userInfoStr, {
      path: '/',
      maxAge: TOKEN_EXPIRY_DAYS * 24 * 60 * 60,
      sameSite: 'lax',
      secure: window.location.protocol === 'https:'
    });
    
    // 2. localStorage에 저장 (백업)
    localStorage.setItem(USER_INFO_KEY, userInfoStr);
    
    return true;
  } catch (error) {
    return false;
  }
}

/**
 * 사용자 정보를 가져오는 함수
 */
export function getUserInfo(cookieString?: string): any | null {
  try {
    // 쿠키에서 정보 조회
    let cookies;
    if (cookieString) {
      cookies = new Cookies(cookieString);
    } else if (typeof window !== 'undefined') {
      cookies = new Cookies();
    } else {
      return null;
    }
    
    const cookieUserInfo = cookies.get(USER_INFO_KEY);
    if (cookieUserInfo) {
      return typeof cookieUserInfo === 'string' 
        ? JSON.parse(cookieUserInfo) 
        : cookieUserInfo;
    }
    
    // 쿠키에 없으면 localStorage에서 확인 (클라이언트만)
    if (typeof window !== 'undefined') {
      const localStorageUserInfo = localStorage.getItem(USER_INFO_KEY);
      if (localStorageUserInfo) {
        return JSON.parse(localStorageUserInfo);
      }
    }
    
    return null;
  } catch (e) {
    return null;
  }
}

/**
 * 인증 상태 유지에 도움이 되는 유틸리티
 */
export function syncAuthState(): void {
  if (typeof window === 'undefined') return;
  
  try {
    const token = getAuthToken();
    if (token) {
      // 쿠키, localStorage, sessionStorage에 동기화
      setAuthToken(token);
    }
  } catch (error) {
    // 오류 무시
  }
}

/**
 * 페이지 새로고침 전 인증 상태 백업
 */
export function backupAuthState(): void {
  if (typeof window === 'undefined') return;
  
  try {
    const token = getAuthToken();
    if (token) {
      // 세션스토리지에 임시 백업 (새로고침 간에만 유지)
      sessionStorage.setItem('auth_token_backup', token);
    }
  } catch (error) {
    // 오류 무시
  }
}

/**
 * 새로고침 후 인증 상태 복원
 */
export function recoverAuthState(): void {
  if (typeof window === 'undefined') return;
  
  try {
    // 세션스토리지의 백업 토큰 확인 (새로고침 전 저장)
    const backupToken = sessionStorage.getItem('auth_token_backup');
    
    if (backupToken) {
      // 쿠키에 토큰이 없는 경우 복원
      const cookies = new Cookies();
      const cookieToken = cookies.get(TOKEN_KEY);
      
      if (!cookieToken) {
        setAuthToken(backupToken);
      }
      
      // 백업 토큰 삭제
      sessionStorage.removeItem('auth_token_backup');
    }
  } catch (error) {
    // 오류 무시
  }
}
