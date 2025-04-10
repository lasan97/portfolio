import { APP_CONFIG } from '@shared/config';
import Cookies from 'universal-cookie';

/**
 * 쿠키를 설정하는 함수
 * path는 기본적으로 '/'로 설정, 만료일은 기본 7일
 */
export function setCookie(name: string, value: string, days = 7, path = '/'): void {
  if (typeof window === 'undefined') return;
  
  try {
    const cookies = new Cookies();
    
    // 쿠키 옵션 설정
    const options: any = {
      path: path,
      maxAge: days * 24 * 60 * 60, // 초 단위
      sameSite: 'lax'
    };
    
    // 로컬호스트가 아닌 경우 도메인 설정
    if (window.location.hostname !== 'localhost') {
      options.domain = window.location.hostname;
    }
    
    // HTTPS 환경에서는 secure 플래그 설정
    if (window.location.protocol === 'https:') {
      options.secure = true;
    }
    
    // 쿠키 설정
    cookies.set(name, value, options);
    
    // 쿠키가 제대로 설정되었는지 확인 (디버깅용)
    console.log(`쿠키 설정 시도: ${name}=${value.substring(0, 10)}...`, {
      hostname: window.location.hostname,
      options
    });
    console.log(`현재 쿠키 상태:`, cookies.getAll());
  } catch (error) {
    console.error('쿠키 설정 중 오류 발생:', error);
  }
}

/**
 * 쿠키를 가져오는 함수 (클라이언트/서버 모두에서 동작)
 */
export function getCookie(name: string, cookieString?: string): string | null {
  try {
    // 브라우저 환경
    if (typeof window !== 'undefined') {
      const cookies = new Cookies(cookieString);
      const value = cookies.get(name);
      
      // 디버깅 로그
      if (value) {
        console.log(`getCookie: ${name} 값 찾음 - ${typeof value === 'string' ? value.substring(0, 10) + '...' : '[객체]'}`);
      } else {
        console.log(`getCookie: ${name} 값 없음`);
      }
      
      return value || null;
    }
    
    // 서버 환경 또는 쿠키 문자열이 제공된 경우
    if (cookieString) {
      const cookies = new Cookies(cookieString);
      return cookies.get(name) || null;
    }
    
    return null;
  } catch (error) {
    console.error(`getCookie: ${name} 조회 중 오류 발생`, error);
    return null;
  }
}

/**
 * 쿠키를 삭제하는 함수
 */
export function deleteCookie(name: string, path = '/'): void {
  if (typeof window === 'undefined') return;
  
  try {
    const cookies = new Cookies();
    
    // 쿠키 삭제 (path 옵션 포함)
    cookies.remove(name, { path });
    
    // 삭제 확인 (디버깅용)
    console.log(`쿠키 삭제 시도: ${name}`);
    console.log(`삭제 후 쿠키 상태:`, cookies.getAll());
  } catch (error) {
    console.error(`쿠키 삭제 중 오류 발생:`, error);
  }
}

/**
 * 인증 토큰을 설정하는 함수 (쿠키 + 로컬스토리지)
 */
export function setAuthToken(token: string): boolean {
  try {
    // 쿠키에 저장 (SSR 지원) - 만료일을 30일로 증가
    setCookie(APP_CONFIG.tokenKey, token, 30);
    
    // 로컬스토리지에도 저장 (CSR 백업)
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(APP_CONFIG.tokenKey, token);
    }
    
    // 쿠키가 제대로 설정되었는지 확인 (디버깅용)
    const savedCookieToken = getCookie(APP_CONFIG.tokenKey);
    const savedLocalToken = typeof localStorage !== 'undefined' ? localStorage.getItem(APP_CONFIG.tokenKey) : null;
    
    // universal-cookie로 모든 쿠키 확인
    let allCookies = {};
    if (typeof window !== 'undefined') {
      const cookies = new Cookies();
      allCookies = cookies.getAll();
    }
    
    console.log('토큰 설정 결과:', { 
      설정한_토큰: token, 
      쿠키저장_토큰: savedCookieToken,
      로컬저장_토큰: savedLocalToken,
      모든쿠키: allCookies
    });
    
    return !!savedCookieToken || !!savedLocalToken;
  } catch (error) {
    console.error('토큰 저장 중 오류 발생:', error);
    return false;
  }
}

/**
 * 인증 토큰을 가져오는 함수 (쿠키 우선, 로컬스토리지 백업)
 */
export function getAuthToken(cookieString?: string): string | null {
  try {
    // 1. 쿠키에서 토큰 조회
    const cookieToken = getCookie(APP_CONFIG.tokenKey, cookieString);
    
    // 2. 쿠키에 없으면 로컬스토리지에서 조회
    const localStorageToken = typeof localStorage !== 'undefined' ? 
      localStorage.getItem(APP_CONFIG.tokenKey) : null;
    
    // 3. universal-cookie로 모든 쿠키 확인
    let allCookies = {};
    if (typeof window !== 'undefined') {
      const cookies = new Cookies(cookieString);
      allCookies = cookies.getAll();
    }
    
    // 4. 디버깅 로그
    console.log('토큰 조회:', { 
      쿠키_토큰: cookieToken, 
      로컬저장소_토큰: localStorageToken,
      모든쿠키: allCookies
    });
    
    // 5. 쿠키 토큰이 있으면 쿠키 토큰 반환, 없으면 로컬스토리지 토큰 반환
    const token = cookieToken || localStorageToken;
    
    // 6. 쿠키에 토큰이 없고 로컬스토리지에만 있는 경우, 쿠키 복원
    if (!cookieToken && localStorageToken && typeof window !== 'undefined') {
      console.log('쿠키 복원: 로컬스토리지에서 쿠키로 토큰 복사');
      setCookie(APP_CONFIG.tokenKey, localStorageToken, 30);
    }
    
    return token;
  } catch (error) {
    console.error('토큰 조회 중 오류 발생:', error);
    return null;
  }
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
    // 쿠키에서 삭제
    deleteCookie(APP_CONFIG.tokenKey);
    deleteCookie(APP_CONFIG.userStorageKey);
    
    // 로컬스토리지에서도 삭제
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(APP_CONFIG.tokenKey);
      localStorage.removeItem(APP_CONFIG.userStorageKey);
    }
    
    console.log('로그아웃 완료 - 인증 정보 삭제됨');
    return true;
  } catch (error) {
    console.error('로그아웃 중 오류 발생:', error);
    return false;
  }
}

/**
 * 사용자 정보를 저장하는 함수 (쿠키 전용)
 */
export function setUserInfo(userInfo: any): boolean {
  try {
    const userInfoStr = JSON.stringify(userInfo);
    
    // 쿠키에 저장 
    setCookie(APP_CONFIG.userStorageKey, userInfoStr);
    return true;
  } catch (error) {
    console.error('사용자 정보 저장 중 오류 발생:', error);
    return false;
  }
}

/**
 * 사용자 정보를 가져오는 함수 (쿠키 전용)
 */
export function getUserInfo(cookieString?: string): any | null {
  try {
    const cookieUserInfo = getCookie(APP_CONFIG.userStorageKey, cookieString);
    if (!cookieUserInfo) return null;
    
    return JSON.parse(cookieUserInfo);
  } catch (e) {
    console.error('쿠키의 사용자 정보 파싱 중 오류 발생:', e);
    return null;
  }
}
