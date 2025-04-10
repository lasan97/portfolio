export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  role?: string; // ADMIN, USER 등 역할 정보
}

export interface UserState {
  user: User | null;
  loading: boolean;
  error: string | null;
}
