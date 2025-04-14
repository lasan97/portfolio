/**
 * GitHub OAuth 로그인 URL 생성
 */
export const getGithubLoginUrl = (): string => {
    // 서버사이드 렌더링 중에는 window가 없으므로 체크
    const origin = typeof window !== 'undefined' ? window.location.origin : '';
    const GITHUB_AUTH_URL = 'https://github.com/login/oauth/authorize';
    const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID;
    const redirectUri = encodeURIComponent(`${origin}/oauth2/callback`);
    return `${GITHUB_AUTH_URL}?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email%20read:user`;
};
