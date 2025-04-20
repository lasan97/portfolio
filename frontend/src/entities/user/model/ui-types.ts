import { User } from './types';

// UI 모델 - 사용자 카드 컴포넌트용 모델
export interface UserCardProps {
  user: User;
}

// UI 모델 - 사용자 프로필 표시용 모델
export interface UserDisplayProps {
  id: number;
  nickname: string;
  email: string;
  profileImageUrl?: string | null;
  role?: string;
}