/**
 * 유틸리티 헬퍼 함수 모음
 */

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
  const nameEQ = name + "=";
  const ca = document.cookie.split(';');
  
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') c = c.substring(1, c.length);
    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
  }
  
  return null;
}

/**
 * 쿠키 설정하기
 * @param name - 쿠키 이름
 * @param value - 쿠키 값
 * @param days - 유효 기간 (일)
 */
export function setCookie(name: string, value: string, days = 7): void {
  let expires = "";
  
  if (days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    expires = "; expires=" + date.toUTCString();
  }
  
  document.cookie = name + "=" + value + expires + "; path=/";
}

/**
 * 쿠키 삭제하기
 * @param name - 쿠키 이름
 */
export function deleteCookie(name: string): void {
  setCookie(name, '', -1);
}
