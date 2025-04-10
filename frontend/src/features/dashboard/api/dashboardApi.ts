import { api } from '@shared/api';
import type { DashboardDto, DashboardUpdateRequest } from '@entities/dashboard';
import type { AxiosApiResponse } from '@shared/types';

const BASE_URL = '/api/dashboard';

/**
 * 대시보드 정보 조회 API
 */
export const getDashboard = (): Promise<AxiosApiResponse<DashboardDto>> => {
  return api.get(BASE_URL);
};

/**
 * 대시보드 정보 업데이트 API
 */
export const updateDashboard = (data: DashboardUpdateRequest): Promise<AxiosApiResponse<DashboardDto>> => {
  return api.put(BASE_URL, data);
};

/**
 * 대시보드 초기화 API
 */
export const initializeDashboard = (): Promise<AxiosApiResponse<DashboardDto>> => {
  return api.post(`${BASE_URL}/initialize`);
};
