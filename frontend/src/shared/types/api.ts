/**
 * API 응답 타입 (Axios 응답 래퍼)
 */
export interface AxiosApiResponse<T> {
  data: T;
  status: number;
  statusText: string;
  headers: any;
  config: any;
}

/**
 * API 오류 응답 타입
 */
export interface ApiErrorResponse {
  message: string;
  status: number;
  timestamp: string;
  errors?: Record<string, string>;
}
