import { User } from '@entities/user';

export interface AuthState {
  token: string | null;
  loading: boolean;
  error: string | null;
}

export interface TokenResponse {
  token: string;
}
