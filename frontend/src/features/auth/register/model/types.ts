import { RegisterRequest } from '@/shared/types/common';

/**
 * 회원가입 폼 입력값 타입
 */
export interface RegisterFormData extends RegisterRequest {
  confirmPassword: string;
}

/**
 * 회원가입 유효성 검사 에러 타입
 */
export interface RegisterFormErrors {
  username?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  name?: string;
}
