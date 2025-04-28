import { apiInstance } from '@shared/api';
import type { CreditHistory, CreditInfo } from '../model/types';

/**
 * 사용자 크레딧 관련 API 함수들
 */

/**
 * 현재 사용자의 크레딧 정보를 가져옵니다.
 * @returns {Promise<CreditInfo>} 크레딧 정보
 */
export const fetchCurrentCredit = async (): Promise<CreditInfo> => {
  const response = await apiInstance.get('/api/users/credits/me');
  return response.data;
};

/**
 * 사용자의 크레딧을 충전합니다.
 * @param {number} amount 충전할 금액
 * @returns {Promise<void>}
 */
export const increaseCredit = async (amount: number): Promise<void> => {
  await apiInstance.post('/api/users/credits', {
    amount: amount
  });
};

/**
 * 사용자의 크레딧 히스토리를 페이지별로 가져옵니다.
 * @param {number} page 페이지 번호 (기본값: 0)
 * @param {number} size 페이지당 항목 수 (기본값: 10)
 * @returns {Promise<any>} 페이지 정보 및 크레딧 히스토리 목록
 */
export const fetchCreditHistory = async (page = 0, size = 10): Promise<{
  content: CreditHistory[],
  totalPages: number,
  totalElements: number,
  last: boolean,
  number: number,
  size: number
}> => {
  const response = await apiInstance.get(`/api/users/credits/histoies/page?page=${page}&size=${size}`);
  return response.data;
};
