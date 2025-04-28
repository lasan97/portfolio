import { apiInstance } from '@shared/api';
import type { User, CreditHistory, CreditInfo } from '../model/types';

/**
 * 사용자 관련 API 요청을 처리하는 repository
 */
export const userRepository = {
  /**
   * 사용자 정보를 가져옵니다.
   * @param {number} userId 사용자 ID
   * @returns {Promise<User>} 사용자 정보
   */
  async fetchUser(userId: number): Promise<User> {
    const response = await apiInstance.get(`/api/users/${userId}`);
    return response.data.data;
  },

  /**
   * 현재 로그인한 사용자 정보를 가져옵니다.
   * @returns {Promise<User>} 현재 사용자 정보
   */
  async fetchCurrentUser(): Promise<User> {
    const response = await apiInstance.get('/api/users/me');
    return response.data.data;
  },

  /**
   * 사용자 크레딧 관련 API
   */
  credit: {
    /**
     * 현재 사용자의 크레딧 정보를 가져옵니다.
     * @returns {Promise<CreditInfo>} 크레딧 정보
     */
    async fetchCurrent(): Promise<CreditInfo> {
      const response = await apiInstance.get('/api/users/credits/me');
      return response.data;
    },

    /**
     * 사용자의 크레딧을 충전합니다.
     * @param {number} amount 충전할 금액
     * @returns {Promise<void>}
     */
    async increase(amount: number): Promise<void> {
      await apiInstance.post('/api/users/credits', {
        amount: amount
      });
    },

    /**
     * 사용자의 크레딧 히스토리를 페이지별로 가져옵니다.
     * @param {number} page 페이지 번호 (기본값: 0)
     * @param {number} size 페이지당 항목 수 (기본값: 10)
     * @returns {Promise<any>} 페이지 정보 및 크레딧 히스토리 목록
     */
    async fetchHistory(page = 0, size = 10): Promise<{
      content: CreditHistory[],
      totalPages: number,
      totalElements: number,
      last: boolean,
      number: number,
      size: number
    }> {
      const response = await apiInstance.get(`/api/users/credits/histoies/page?page=${page}&size=${size}`);
      return response.data;
    }
  }
};
