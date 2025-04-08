/**
 * 환경변수 유틸리티
 * 
 * .env 파일에서 로드된 환경변수에 접근하기 위한 래퍼
 */

// 환경변수 가져오기
export function getEnv(key: string, defaultValue?: string): string {
  // Vite에서는 import.meta.env 를 사용하여 환경 변수에 접근합니다
  const value = import.meta.env[key];
  
  if (value !== undefined && value !== null) {
    return value as string;
  }
  
  // 기본값이 제공되지 않았으면 빈 문자열 반환
  if (defaultValue === undefined) {
    console.warn(`환경변수 "${key}"가 정의되지 않았습니다.`);
    return '';
  }
  
  return defaultValue;
}

// 주요 환경변수 정의
export const NODE_ENV = getEnv('MODE');
export const API_URL = getEnv('VITE_API_URL');
export const BASE_URL = getEnv('VITE_BASE_URL');

// 프로덕션 환경 여부
export const IS_PROD = NODE_ENV === 'production';
export const IS_DEV = NODE_ENV === 'development';

// 환경변수 객체 export
export default {
  NODE_ENV,
  API_URL,
  BASE_URL,
  IS_PROD,
  IS_DEV,
  getEnv,
};