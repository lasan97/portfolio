/**
 * SSR 관련 타입 정의
 */

// 서버 사이드 렌더링 컨텍스트
export interface SSRContext {
  // 요청 쿠키 문자열
  cookie?: string;
  // 요청 URL
  url?: string;
  // 추가 매니페스트 정보
  manifest?: any;
}

// SSR 렌더링 결과
export interface SSRRenderResult {
  // 렌더링된
  appHtml: string;
  // 프리로드 링크
  preloadLinks: string;
  // 초기 상태
  initialState: any;
}
