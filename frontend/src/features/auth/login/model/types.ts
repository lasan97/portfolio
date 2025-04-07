import { Ref } from 'vue';
import { LoginRequest, LoginResponse } from '@/shared/types/common';

/**
 * 로그인 폼 입력값 타입
 */
export interface LoginFormData extends LoginRequest {
  rememberMe?: boolean;
}

/**
 * 로그인 유효성 검사 에러 타입
 */
export interface LoginFormErrors {
  username?: string;
  password?: string;
}

/**
 * 로그인 컴포저블 반환 타입
 */
export interface UseLoginReturn {
  login: (data: LoginFormData) => Promise<LoginResponse>;
  loading: Ref<boolean>;
  error: Ref<string | null>;
}
