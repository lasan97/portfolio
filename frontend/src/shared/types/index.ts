export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl?: string;
}

export interface AuthToken {
  token: string;
  userId: number;
  email: string;
  nickname: string;
}

// SSR 타입 익스포트
export * from './ssr';
