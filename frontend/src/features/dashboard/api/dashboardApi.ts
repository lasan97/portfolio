import { api } from '@shared/api';
import type { DashboardDto, DashboardUpdateRequest, DashboardCreateRequest } from '@entities/dashboard';
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
export const updateDashboard = (data: DashboardUpdateRequest): Promise<AxiosApiResponse<number>> => {
  return api.put(BASE_URL, data);
};

/**
 * 대시보드 생성 API
 */
export const createDashboard = (data: DashboardCreateRequest): Promise<AxiosApiResponse<number>> => {
  return api.post(BASE_URL, data);
};
