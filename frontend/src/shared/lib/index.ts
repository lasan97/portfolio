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

// 통합된 인증 유틸리티 export (universal-cookie 기반)
export * from './auth-utils';
