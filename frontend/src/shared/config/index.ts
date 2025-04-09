// 환경 변수 및 앱 설정
export * from './env';

/**
 * 페이지네이션 기본 설정
 */
export const DEFAULT_PAGE_SIZE = 10;

/**
 * 앱 전역 설정
 */
export const APP_CONFIG = {
  appName: '포트폴리오 FSD',
  version: '1.0.0',
  apiTimeout: 10000,
  tokenKey: 'auth_token',
  userStorageKey: 'user_info'
};

/**
 * 라우트 경로
 */
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  PROFILE: '/profile',
  NOT_FOUND: '/404',
  OAUTH_CALLBACK: '/oauth/callback'
};

/**
 * 사용자 역할
 */
export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER'
}