export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
}

export interface UserState {
  user: User | null;
  loading: boolean;
  error: string | null;
}
