import { useRouter } from 'vue-router';
import { APP_CONFIG, ROUTES } from '@/shared/config';
import { deleteCookie } from '@/shared/lib/helpers';

/**
 * 로그아웃 로직을 관리하는 컴포저블
 */
export function useLogout() {
  const router = useRouter();

  /**
   * 로그아웃 함수
   */
  const logout = () => {
    // 토큰 제거
    localStorage.removeItem(APP_CONFIG.tokenKey);
    
    // 사용자 정보 제거
    localStorage.removeItem(APP_CONFIG.userStorageKey);
    
    // 쿠키 제거
    deleteCookie(APP_CONFIG.tokenKey);
    
    // 로그인 페이지로 리다이렉트
    router.push(ROUTES.LOGIN);
  };

  return {
    logout
  };
}
