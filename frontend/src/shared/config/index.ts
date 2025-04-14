/**
 * 페이지네이션 기본 설정
 */
export const DEFAULT_PAGE_SIZE = 10;

/**
 * 앱 전역 설정
 */
export const APP_CONFIG = {
  appName: 'H-Martin',
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
  PROFILE: '/profile',
  NOT_FOUND: '/404',
  OAUTH_CALLBACK: '/oauth2/callback',
  SSR: '/ssr',
  INTRODUCTION: '/introduction',
  PRODUCTS: '/products',
  PRODUCT_DETAIL: '/products/:id',
  CHECKOUT: '/checkout',
  ORDER_COMPLETE: '/order-complete'
};

/**
 * 사용자 역할
 */
export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER'
}