// 인증 상태를 로컬스토리지에 저장하는 마지막 방어선 도입
import { APP_CONFIG } from '@shared/config';
import Cookies from 'universal-cookie';

/**
 * 페이지 로드 시 토큰이 있다면 유효한지 검증하여 자동 로그인 시도
 */
export async function initAuth(): Promise<boolean> {
  try {
    // 로컬스토리지에서 토큰 가져오기
    const token = localStorage.getItem(APP_CONFIG.tokenKey);
    
    // 토큰이 없으면 인증되지 않음
    if (!token) {
      console.log('토큰이 없어 자동 로그인 건너뜀');
      return false;
    }
    
    // API 요청 시 인증 헤더 추가
    const { api } = await import('@shared/api');
    
    // 인증 확인 API 호출 (토큰 유효성 검증)
    const response = await api.get('/api/users/me', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    
    // 응답 성공 시 인증된 것으로 판단
    if (response && response.data) {
      console.log('자동 로그인 성공', response.data);
      
      // 쿠키와 로컬스토리지에 토큰 다시 저장 (갱신)
      setAuthInAllStorages(token);
      
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('자동 로그인 중 오류 발생:', error);
    return false;
  }
}

/**
 * 모든 스토리지에 인증 정보 저장 (쿠키, 로컬스토리지)
 */
export function setAuthInAllStorages(token: string): void {
  try {
    // 1. 로컬스토리지에 저장
    localStorage.setItem(APP_CONFIG.tokenKey, token);
    
    // 2. 쿠키에 저장 (universal-cookie 사용)
    if (typeof window !== 'undefined') {
      const cookies = new Cookies();
      
      // 쿠키 옵션 설정
      const options: any = {
        path: '/',
        maxAge: 30 * 24 * 60 * 60, // 초 단위 (30일)
        sameSite: 'lax'
      };
      
      // HTTPS 환경에서는 secure 플래그 설정
      if (window.location.protocol === 'https:') {
        options.secure = true;
      }
      
      cookies.set(APP_CONFIG.tokenKey, token, options);
    }
    
    // 3. 세션스토리지에도 저장 (탭 닫힐 때까지만 유지)
    sessionStorage.setItem(APP_CONFIG.tokenKey, token);
    
    console.log('인증 정보가 모든 스토리지에 저장됨');
  } catch (error) {
    console.error('인증 정보 저장 중 오류 발생:', error);
  }
}

/**
 * 모든 스토리지에서 인증 정보 삭제 (쿠키, 로컬스토리지)
 */
export function clearAuthFromAllStorages(): void {
  try {
    // 1. 로컬스토리지에서 삭제
    localStorage.removeItem(APP_CONFIG.tokenKey);
    localStorage.removeItem(APP_CONFIG.userStorageKey);
    
    // 2. 쿠키에서 삭제 (universal-cookie 사용)
    if (typeof window !== 'undefined') {
      const cookies = new Cookies();
      cookies.remove(APP_CONFIG.tokenKey, { path: '/' });
      cookies.remove(APP_CONFIG.userStorageKey, { path: '/' });
    }
    
    // 3. 세션스토리지에서 삭제
    sessionStorage.removeItem(APP_CONFIG.tokenKey);
    sessionStorage.removeItem(APP_CONFIG.userStorageKey);
    
    console.log('인증 정보가 모든 스토리지에서 삭제됨');
  } catch (error) {
    console.error('인증 정보 삭제 중 오류 발생:', error);
  }
}

/**
 * 모든 스토리지에서 토큰을 검색하여 반환
 */
export function getAuthFromAllStorages(): string | null {
  try {
    let cookieToken = null;
    
    // 1. 쿠키에서 검색 (universal-cookie 사용)
    if (typeof window !== 'undefined') {
      const cookies = new Cookies();
      cookieToken = cookies.get(APP_CONFIG.tokenKey);
    }
    
    // 2. 로컬스토리지에서 검색
    const localStorageToken = localStorage.getItem(APP_CONFIG.tokenKey);
    
    // 3. 세션스토리지에서 검색
    const sessionStorageToken = sessionStorage.getItem(APP_CONFIG.tokenKey);
    
    // 가능한 첫 번째 토큰 반환
    const token = cookieToken || localStorageToken || sessionStorageToken;
    
    // 토큰을 찾았지만 다른 저장소에는 없는 경우 싱크 맞추기
    if (token) {
      if (!cookieToken || !localStorageToken || !sessionStorageToken) {
        console.log('토큰 저장소 간 불일치 발견, 동기화 중...');
        setAuthInAllStorages(token);
      }
    }
    
    return token;
  } catch (error) {
    console.error('인증 토큰 검색 중 오류 발생:', error);
    return null;
  }
}

/**
 * 인증된 상태인지 확인
 */
export function isAuthenticated(): boolean {
  return !!getAuthFromAllStorages();
}

/**
 * API 요청 시 사용할 인증 헤더 생성
 */
export function getAuthHeader(): Record<string, string> | undefined {
  const token = getAuthFromAllStorages();
  
  if (token) {
    return {
      Authorization: `Bearer ${token}`
    };
  }
  
  return undefined;
}
