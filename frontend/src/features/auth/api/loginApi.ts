/**
 * GitHub OAuth 로그인 URL 생성
 */
export const getGithubLoginUrl = (): string => {
  const GITHUB_AUTH_URL = 'https://github.com/login/oauth/authorize';
  const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID;
  const redirectUri = `${import.meta.env.VITE_BASE_URL}/oauth/callback`;
  
  return `${GITHUB_AUTH_URL}?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email`;
};
