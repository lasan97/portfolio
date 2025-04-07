import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { RegisterFormData } from './types';
import { register } from '../api';
import { ROUTES } from '@/shared/config';

/**
 * 회원가입 로직을 관리하는 컴포저블
 */
export function useRegister() {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const router = useRouter();

  /**
   * 회원가입 시도 함수
   * @param data - 회원가입 폼 데이터
   */
  const registerUser = async (data: RegisterFormData) => {
    loading.value = true;
    error.value = null;

    try {
      await register({
        username: data.username,
        email: data.email,
        password: data.password,
        name: data.name
      });
      
      // 로그인 페이지로 리다이렉트
      router.push(ROUTES.LOGIN);
    } catch (err) {
      if (err instanceof Error) {
        error.value = err.message;
      } else {
        error.value = '회원가입 중 오류가 발생했습니다.';
      }
      throw err;
    } finally {
      loading.value = false;
    }
  };

  return {
    register: registerUser,
    loading,
    error
  };
}
