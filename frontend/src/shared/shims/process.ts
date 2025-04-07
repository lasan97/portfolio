// process.ts - process 객체가 존재하지 않을 때 폴백 제공
interface ProcessEnv {
  [key: string]: string | undefined;
  NODE_ENV: 'development' | 'production' | 'test';
}

interface Process {
  env: ProcessEnv;
}

// 브라우저 환경에서 process 객체가 없을 경우를 대비한 폴백 구현
declare global {
  interface Window {
    process?: Process;
  }
}

// process 객체가 없으면 기본값 설정
if (typeof window !== 'undefined' && !window.process) {
  window.process = {
    env: {
      NODE_ENV: 'development' as const, // as const 사용
      // 다른 필요한 환경 변수들 추가
      VUE_APP_API_URL: 'http://localhost:8081/api',
      VUE_APP_BASE_URL: 'http://localhost:8080'
    }
  } as Process; // 전체 객체에 대한 타입 캐스팅
}

export {};
