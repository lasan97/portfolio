import apiInstance from '@/shared/api/instance';
import { ApiResponse, LoginRequest, LoginResponse } from '@/shared/types/common';

/**
 * 로그인 API 요청
 * @param data - 로그인 요청 데이터
 */
export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  try {
    const response = await apiInstance.post<ApiResponse<LoginResponse>>('/auth/login', data);
    return response.data.data as LoginResponse;
  } catch (error) {
    throw new Error('로그인에 실패했습니다. 사용자 이름과 비밀번호를 확인해주세요.');
  }
};
