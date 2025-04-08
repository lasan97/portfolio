// 환경 변수 타입 정의
interface ImportMetaEnv {
  NODE_ENV: string;
  VUE_APP_API_URL: string;
  VUE_APP_BASE_URL: string;
}

// process.env에 대한 타입 확장
declare namespace NodeJS {
  interface ProcessEnv {
    NODE_ENV: 'development' | 'production' | 'test';
    VUE_APP_API_URL: string;
    VUE_APP_BASE_URL: string;
  }
}
