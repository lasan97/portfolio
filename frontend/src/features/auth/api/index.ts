import { api } from '@/shared/api';
import { TokenResponse } from '../model/types';

/**
 * GitHub OAuth 콜백 코드로 로그인/회원가입 처리
 */
export const processOAuthCallback = async (code: string): Promise<TokenResponse> => {
  const response = await api.post<TokenResponse>('/oauth2/token', null, {
    params: { code }
  });
  return response.data;
};

export * from './loginApi';
