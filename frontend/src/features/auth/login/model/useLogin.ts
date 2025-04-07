import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api';
import { LoginFormData, UseLoginReturn } from './types';
import { APP_CONFIG, ROUTES } from '@/shared/config';
import { setCookie } from '@/shared/lib/helpers';

/**
 * 로그인 로직을 관리하는 컴포저블
 * @returns 로그인 함수와 상태 객체
 */
export function useLogin(): UseLoginReturn {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const router = useRouter();

  /**
   * 로그인 시도 함수
   * @param data - 로그인 폼 데이터
   */
  const loginUser = async (data: LoginFormData) => {
    loading.value = true;
    error.value = null;

    try {
      const response = await login(data);
      
      // 토큰 저장
      localStorage.setItem(APP_CONFIG.tokenKey, response.token);
      
      // 사용자 정보 저장
      localStorage.setItem(APP_CONFIG.userStorageKey, JSON.stringify(response.user));
      
      // Remember Me 기능 처리
      if (data.rememberMe) {
        setCookie(APP_CONFIG.tokenKey, response.token, 30); // 30일 동안 유지
      }
      
      // 홈 페이지로 리다이렉트
      router.push(ROUTES.HOME);
      
      return response;
    } catch (err) {
      if (err instanceof Error) {
        error.value = err.message;
      } else {
        error.value = '로그인 중 오류가 발생했습니다.';
      }
      throw err;
    } finally {
      loading.value = false;
    }
  };

  return {
    login: loginUser,
    loading,
    error
  };
}
