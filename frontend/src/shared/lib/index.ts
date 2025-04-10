// 공통 유틸리티 함수들
export const parseJwt = (token: string) => {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
};

export const getGithubLoginUrl = () => {
  // 서버사이드 렌더링 중에는 window가 없으므로 체크
  const origin = typeof window !== 'undefined' ? window.location.origin : '';
  const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID;
  const redirectUri = encodeURIComponent(`${origin}/oauth2/callback`);
  return `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email%20read:user`;
};

// 인증 관련 유틸리티 export
export * from './auth';

// 쿠키 디버깅 유틸리티 export
export * from './cookieDebug';

// 인증 지속성 매커니즘 export
export * from './authPersistence';
