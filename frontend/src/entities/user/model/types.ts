import { UserRole } from "./constants";

export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  role?: UserRole; // ADMIN, USER 등 역할 정보
  credit: number; // 사용자 크레딧
}

export interface UserState {
  user: User | null;
  loading: boolean;
  error: string | null;
}

// 크레딧 관련 인터페이스
export interface CreditHistory {
  id: number;
  transactionType: string; // 'INCREASE' 또는 'DECREASE'
  amount: number;
  transactionDateTime: string;
}

export interface CreditInfo {
  id: number;
  amount: number;
  updatedAt: string;
}
