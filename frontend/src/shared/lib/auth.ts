import { APP_CONFIG } from '@shared/config';

/**
 * 쿠키를 설정하는 함수
 */
export function setCookie(name: string, value: string, days = 7, path = '/'): void {
  const expires = new Date(Date.now() + days * 864e5).toUTCString();
  document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires}; path=${path}; SameSite=Lax`;
}

/**
 * 쿠키를 가져오는 함수 (클라이언트/서버 모두에서 동작)
 */
export function getCookie(name: string, cookieString?: string): string | null {
  // 브라우저 환경
  if (typeof document !== 'undefined' && !cookieString) {
    cookieString = document.cookie;
  }
  
  // 쿠키 문자열이 없으면 null 반환
  if (!cookieString) return null;
  
  const cookies = cookieString.split('; ');
  const cookie = cookies.find(c => c.startsWith(`${name}=`));
  
  return cookie ? decodeURIComponent(cookie.split('=')[1]) : null;
}

/**
 * 쿠키를 삭제하는 함수
 */
export function deleteCookie(name: string, path = '/'): void {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=${path}; SameSite=Lax`;
}

/**
 * 인증 토큰을 설정하는 함수 (localStorage와 쿠키 모두 사용)
 */
export function setAuthToken(token: string): boolean {
  try {
    // localStorage에 저장 (기존 방식과의 호환성 유지)
    localStorage.setItem(APP_CONFIG.tokenKey, token);
    
    // 쿠키에도 저장 (SSR 지원)
    setCookie(APP_CONFIG.tokenKey, token);
    
    return true;
  } catch (error) {
    console.error('토큰 저장 중 오류 발생:', error);
    return false;
  }
}

/**
 * 인증 토큰을 가져오는 함수 (localStorage 또는 쿠키)
 */
export function getAuthToken(cookieString?: string): string | null {
  // 먼저 쿠키에서 확인
  const cookieToken = getCookie(APP_CONFIG.tokenKey, cookieString);
  if (cookieToken) return cookieToken;
  
  // 쿠키에 없으면 localStorage 확인 (클라이언트에서만)
  if (typeof localStorage !== 'undefined') {
    try {
      return localStorage.getItem(APP_CONFIG.tokenKey);
    } catch (error) {
      console.error('localStorage 접근 중 오류 발생:', error);
    }
  }
  
  return null;
}

/**
 * 사용자가 인증되었는지 확인하는 함수
 */
export function isAuthenticated(cookieString?: string): boolean {
  return !!getAuthToken(cookieString);
}

/**
 * 로그아웃 함수
 */
export function logout(): boolean {
  try {
    // localStorage에서 삭제
    localStorage.removeItem(APP_CONFIG.tokenKey);
    localStorage.removeItem(APP_CONFIG.userStorageKey);
    
    // 쿠키에서도 삭제
    deleteCookie(APP_CONFIG.tokenKey);
    
    return true;
  } catch (error) {
    console.error('로그아웃 중 오류 발생:', error);
    return false;
  }
}

/**
 * 사용자 정보를 저장하는 함수
 */
export function setUserInfo(userInfo: any): boolean {
  try {
    const userInfoStr = JSON.stringify(userInfo);
    
    // localStorage에 저장
    localStorage.setItem(APP_CONFIG.userStorageKey, userInfoStr);
    
    // 쿠키에도 저장 (단, 중요 정보는 제외)
    const safeUserInfo = {
      id: userInfo.id,
      name: userInfo.name,
      role: userInfo.role
    };
    setCookie(APP_CONFIG.userStorageKey, JSON.stringify(safeUserInfo));
    
    return true;
  } catch (error) {
    console.error('사용자 정보 저장 중 오류 발생:', error);
    return false;
  }
}

/**
 * 사용자 정보를 가져오는 함수
 */
export function getUserInfo(cookieString?: string): any | null {
  // 먼저 쿠키에서 확인
  const cookieUserInfo = getCookie(APP_CONFIG.userStorageKey, cookieString);
  if (cookieUserInfo) {
    try {
      return JSON.parse(cookieUserInfo);
    } catch (e) {
      console.error('쿠키의 사용자 정보 파싱 중 오류 발생:', e);
    }
  }
  
  // 쿠키에 없으면 localStorage 확인 (클라이언트에서만)
  if (typeof localStorage !== 'undefined') {
    try {
      const localUserInfo = localStorage.getItem(APP_CONFIG.userStorageKey);
      return localUserInfo ? JSON.parse(localUserInfo) : null;
    } catch (error) {
      console.error('localStorage의 사용자 정보 접근 중 오류 발생:', error);
    }
  }
  
  return null;
}
