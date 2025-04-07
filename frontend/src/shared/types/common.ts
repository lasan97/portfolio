/**
 * 공통 타입 정의
 */

/**
 * API 응답의 기본 구조
 */
export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

/**
 * 페이지네이션 응답 구조
 */
export interface PaginatedResponse<T = unknown> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * 페이지네이션 요청 파라미터
 */
export interface PaginationParams {
  page: number;
  size: number;
  sort?: string;
}

/**
 * 사용자 기본 정보
 */
export interface User {
  id: number;
  username: string;
  email: string;
  name?: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 로그인 요청 데이터
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 로그인 응답 데이터
 */
export interface LoginResponse {
  token: string;
  user: User;
}

/**
 * 회원가입 요청 데이터
 */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  name?: string;
}
