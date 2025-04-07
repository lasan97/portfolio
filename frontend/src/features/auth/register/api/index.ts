import apiInstance from '@/shared/api/instance';
import { ApiResponse, RegisterRequest } from '@/shared/types/common';

/**
 * 회원가입 API 요청
 * @param data - 회원가입 요청 데이터
 */
export const register = async (data: RegisterRequest): Promise<void> => {
  try {
    const response = await apiInstance.post<ApiResponse>('/auth/register', data);
    
    if (!response.data.success) {
      throw new Error(response.data.message || '회원가입에 실패했습니다.');
    }
  } catch (error) {
    throw new Error('회원가입에 실패했습니다. 다시 시도해주세요.');
  }
};
