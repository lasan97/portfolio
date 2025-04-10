/**
 * 유틸리티 헬퍼 함수 모음
 */
import Cookies from 'universal-cookie';

/**
 * 랜덤 ID 생성 함수
 * @returns 랜덤 ID 문자열
 */
export function generateRandomId(): string {
  return Math.random().toString(36).substring(2, 9);
}

/**
 * 디바운스 함수
 * @param fn - 실행할 함수
 * @param delay - 지연 시간 (ms)
 * @returns 디바운스된 함수
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay = 300
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | null = null;
  
  return function(...args: Parameters<T>): void {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    
    timeoutId = setTimeout(() => {
      fn(...args);
      timeoutId = null;
    }, delay);
  };
}

/**
 * 쓰로틀 함수
 * @param fn - 실행할 함수
 * @param limit - 제한 시간 (ms)
 * @returns 쓰로틀된 함수
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
  fn: T,
  limit = 300
): (...args: Parameters<T>) => void {
  let inThrottle = false;
  
  return function(...args: Parameters<T>): void {
    if (!inThrottle) {
      fn(...args);
      inThrottle = true;
      setTimeout(() => {
        inThrottle = false;
      }, limit);
    }
  };
}

/**
 * 쿠키 값 가져오기
 * @param name - 쿠키 이름
 * @returns 쿠키 값 또는 null
 */
export function getCookie(name: string): string | null {
  try {
    if (typeof window === 'undefined') {
      return null;
    }
    
    const cookies = new Cookies();
    return cookies.get(name) || null;
  } catch (error) {
    console.error('쿠키 조회 중 오류 발생:', error);
    return null;
  }
}

/**
 * 쿠키 설정하기
 * @param name - 쿠키 이름
 * @param value - 쿠키 값
 * @param days - 유효 기간 (일)
 */
export function setCookie(name: string, value: string, days = 7): void {
  try {
    if (typeof window === 'undefined') {
      return;
    }
    
    const cookies = new Cookies();
    
    const options: any = {
      path: '/',
      maxAge: days * 24 * 60 * 60, // 초 단위
      sameSite: 'lax'
    };
    
    // secure 속성은 HTTPS에서만 추가
    if (window.location.protocol === 'https:') {
      options.secure = true;
    }
    
    cookies.set(name, value, options);
  } catch (error) {
    console.error('쿠키 설정 중 오류 발생:', error);
  }
}

/**
 * 쿠키 삭제하기
 * @param name - 쿠키 이름
 */
export function deleteCookie(name: string): void {
  try {
    if (typeof window === 'undefined') {
      return;
    }
    
    const cookies = new Cookies();
    
    cookies.remove(name, { path: '/' });
  } catch (error) {
    console.error('쿠키 삭제 중 오류 발생:', error);
  }
}
