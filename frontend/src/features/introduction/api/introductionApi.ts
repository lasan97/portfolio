import { apiInstance } from '@shared/api';
import type { IntroductionDto, IntroductionUpdateRequest, IntroductionCreateRequest } from '../model/types';
import type { AxiosApiResponse } from '@shared/types';

const BASE_URL = '/api/introduction';

/**
 * 자기소개 정보 조회 API
 */
export const getIntroduction = (): Promise<AxiosApiResponse<IntroductionDto>> => {
  return apiInstance.get(BASE_URL);
};

/**
 * 자기소개 정보 업데이트 API
 */
export const updateIntroduction = (data: IntroductionUpdateRequest): Promise<AxiosApiResponse<number>> => {
  return apiInstance.put(BASE_URL, data);
};

/**
 * 자기소개 생성 API
 */
export const createIntroduction = (data: IntroductionCreateRequest): Promise<AxiosApiResponse<number>> => {
  return apiInstance.post(BASE_URL, data);
};
