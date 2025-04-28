import { apiInstance } from '@shared/api';
import { TokenResponse } from '../model/types';
import type { User } from '@entities/user';

/**
 * 인증 관련 API 요청을 처리하는 repository
 */
export const authRepository = {
  /**
   * GitHub OAuth 로그인 URL 생성
   * @returns {string} GitHub OAuth 인증 URL
   */
  getGithubLoginUrl(): string {
    const GITHUB_AUTH_URL = 'https://github.com/login/oauth/authorize';
    const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID;
    const redirectUri = `${import.meta.env.VITE_BASE_URL}/oauth/callback`;
    
    return `${GITHUB_AUTH_URL}?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email`;
  },

  /**
   * GitHub OAuth 콜백 코드로 로그인/회원가입 처리
   * @param {string} code - OAuth 콜백 코드
   * @returns {Promise<TokenResponse>} 토큰 응답
   */
  async processOAuthCallback(code: string): Promise<TokenResponse> {
    const response = await apiInstance.post<TokenResponse>('/oauth2/token', null, {
      params: { code }
    });
    return response.data;
  },

  /**
   * 현재 로그인한 사용자 정보를 가져옵니다.
   * @returns {Promise<User>} 현재 사용자 정보
   */
  async fetchCurrentUser(): Promise<User> {
    const response = await apiInstance.get('/api/users/me');
    return response.data;
  }
};
