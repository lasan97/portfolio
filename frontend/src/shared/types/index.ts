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
