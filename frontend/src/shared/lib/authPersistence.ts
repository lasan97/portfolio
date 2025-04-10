//
// 특수 스토리지 기반 인증 메커니즘 (쿠키 문제를 우회하기 위함)
//
import Cookies from 'universal-cookie';

// 인증 토큰 저장소 키
const TOKEN_KEY = 'auth_token';
const TOKEN_EXPIRY_KEY = 'auth_token_expiry';
const USER_KEY = 'user_data';

/**
 * 인증 토큰과 만료 시간 저장
 */
export function saveToken(token: string, expiryDays = 30): void {
  // 모든 저장소에 토큰 저장
  try {
    if (typeof window === 'undefined') return;
    
    // 1. localStorage (지속적)
    localStorage.setItem(TOKEN_KEY, token);
    
    // 2. sessionStorage (세션 유지)
    sessionStorage.setItem(TOKEN_KEY, token);
    
    // 3. 쿠키 (SSR 호환) - universal-cookie 사용
    const cookies = new Cookies();
    
    // 쿠키 옵션 설정
    const options: any = {
      path: '/',
      maxAge: expiryDays * 24 * 60 * 60, // 초 단위
      sameSite: 'lax'
    };
    
    // HTTPS 환경에서는 secure 플래그 설정
    if (window.location.protocol === 'https:') {
      options.secure = true;
    }
    
    cookies.set(TOKEN_KEY, token, options);
    
    // 만료일 계산 및 저장
    const expiryDate = new Date();
    expiryDate.setDate(expiryDate.getDate() + expiryDays);
    localStorage.setItem(TOKEN_EXPIRY_KEY, expiryDate.toISOString());
    
    console.log('토큰 저장 완료', {
      토큰: token.substring(0, 10) + '...',
      만료일: expiryDate.toLocaleString(),
      저장소: ['localStorage', 'sessionStorage', 'cookie']
    });
  } catch (error) {
    console.error('토큰 저장 중 오류 발생:', error);
  }
}

/**
 * 모든 저장소에서 인증 토큰 가져오기
 */
export function getToken(): string | null {
  try {
    if (typeof window === 'undefined') return null;
    
    // 1. 쿠키에서 검색 (universal-cookie 사용)
    const cookies = new Cookies();
    const cookieToken = cookies.get(TOKEN_KEY);
    
    // 2. sessionStorage에서 검색
    const sessionToken = sessionStorage.getItem(TOKEN_KEY);
    
    // 3. localStorage에서 검색
    const localToken = localStorage.getItem(TOKEN_KEY);
    
    // 토큰 동기화 확인
    if (localToken && (localToken !== cookieToken || localToken !== sessionToken)) {
      console.log('토큰 불일치 감지, 동기화 중...');
      saveToken(localToken);
    }
    
    // 토큰 만료 확인
    if (localToken) {
      const expiryStr = localStorage.getItem(TOKEN_EXPIRY_KEY);
      if (expiryStr) {
        const expiry = new Date(expiryStr);
        if (expiry < new Date()) {
          console.log('토큰 만료됨, 삭제 중...');
          clearToken();
          return null;
        }
      }
    }
    
    // 찾은 첫 번째 유효한 토큰 반환
    return cookieToken || sessionToken || localToken || null;
  } catch (error) {
    console.error('토큰 조회 중 오류 발생:', error);
    return null;
  }
}

/**
 * 모든 저장소에서 토큰 삭제
 */
export function clearToken(): void {
  try {
    if (typeof window === 'undefined') return;
    
    // localStorage에서 삭제
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    
    // sessionStorage에서 삭제
    sessionStorage.removeItem(TOKEN_KEY);
    
    // 쿠키에서 삭제 (universal-cookie 사용)
    const cookies = new Cookies();
    cookies.remove(TOKEN_KEY, { path: '/' });
    
    console.log('모든 저장소에서 토큰 삭제 완료');
  } catch (error) {
    console.error('토큰 삭제 중 오류 발생:', error);
  }
}

/**
 * 사용자 정보 저장
 */
export function saveUserData(userData: any): void {
  try {
    if (typeof window === 'undefined') return;
    
    const userDataStr = JSON.stringify(userData);
    localStorage.setItem(USER_KEY, userDataStr);
    console.log('사용자 정보 저장 완료');
  } catch (error) {
    console.error('사용자 정보 저장 중 오류 발생:', error);
  }
}

/**
 * 사용자 정보 가져오기
 */
export function getUserData(): any | null {
  try {
    if (typeof window === 'undefined') return null;
    
    const userDataStr = localStorage.getItem(USER_KEY);
    if (userDataStr) {
      return JSON.parse(userDataStr);
    }
    return null;
  } catch (error) {
    console.error('사용자 정보 가져오기 중 오류 발생:', error);
    return null;
  }
}

/**
 * 사용자 정보 삭제
 */
export function clearUserData(): void {
  try {
    if (typeof window === 'undefined') return;
    
    localStorage.removeItem(USER_KEY);
    console.log('사용자 정보 삭제 완료');
  } catch (error) {
    console.error('사용자 정보 삭제 중 오류 발생:', error);
  }
}

/**
 * 페이지 로딩 시 토큰 검사 및 동기화
 */
export function verifyAndSyncAuthState(): void {
  try {
    if (typeof window === 'undefined') return;
    
    const token = getToken();
    if (token) {
      // 모든 저장소에 토큰 동기화
      saveToken(token);
      console.log('인증 상태 동기화 완료');
    }
  } catch (error) {
    console.error('인증 상태 동기화 중 오류 발생:', error);
  }
}

/**
 * 인증 상태 확인
 */
export function isAuth(): boolean {
  return !!getToken();
}

/**
 * 새로고침 후 인증 상태 복원
 */
export function recoverAuthState(): void {
  if (typeof window === 'undefined') return;
  
  try {
    // 세션스토리지의 백업 토큰 확인 (새로고침 전 저장)
    const backupToken = sessionStorage.getItem('auth_token_backup');
    
    // 현재 쿠키의 토큰 확인 (universal-cookie 사용)
    const cookies = new Cookies();
    const cookieToken = cookies.get(TOKEN_KEY);
    
    // 로컬스토리지의 토큰 확인
    const localToken = localStorage.getItem(TOKEN_KEY);
    
    console.log('인증 상태 복원 검사:', {
      백업토큰: !!backupToken,
      쿠키토큰: !!cookieToken,
      로컬토큰: !!localToken
    });
    
    // 쿠키에 토큰이 없지만 다른 저장소에 있는 경우 복원
    if (!cookieToken && (backupToken || localToken)) {
      const tokenToRestore = backupToken || localToken;
      if (tokenToRestore) {
        console.log('새로고침 후 인증 토큰 복원 중...');
        saveToken(tokenToRestore);
        
        // 백업 토큰 삭제
        if (backupToken) {
          sessionStorage.removeItem('auth_token_backup');
        }
      }
    }
  } catch (error) {
    console.error('인증 상태 복원 중 오류 발생:', error);
  }
}

/**
 * 페이지 새로고침 전 인증 상태 백업
 */
export function backupAuthState(): void {
  if (typeof window === 'undefined') return;
  
  try {
    const token = getToken();
    if (token) {
      // 세션스토리지에 임시 백업 (새로고침 간에만 유지)
      sessionStorage.setItem('auth_token_backup', token);
      console.log('인증 상태 백업 완료');
    }
  } catch (error) {
    console.error('인증 상태 백업 중 오류 발생:', error);
  }
}

// 페이지 로드 시 자동 실행
if (typeof window !== 'undefined') {
  // 페이지 로드 시 인증 상태 복원
  window.addEventListener('load', recoverAuthState);
  
  // 페이지 언로드 전 인증 상태 백업
  window.addEventListener('beforeunload', backupAuthState);
}
