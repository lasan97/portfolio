import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useUserStore } from '@entities/user/model/userStore';
import { processOAuthCallback } from '../api';
import { setAuthToken, getAuthToken, logout as logoutAuth, isAuthenticated as checkAuth, recoverAuthState, syncAuthState } from '@shared/lib';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
  // 라우터 (직접 import 대신 필요할 때 가져오기)
  let router: any = null;
  try {
    // SSR 환경에서는 오류가 발생할 수 있으므로 try-catch로 감싸기
    const vueRouter = import('vue-router');
    if (typeof window !== 'undefined') {
      router = useRouter();
    }
  } catch (e) {
    // 라우터를 사용할 수 없는 경우 조용히 무시
  }
  
  // 상태(state)
  const token = ref<string | null>(getAuthToken());
  const loading = ref(false);
  const error = ref<string | null>(null);
  // 인증 상태를 내부적으로 관리
  const authenticated = ref(!!token.value);
  
  // 게터(getters)
  const isAuthenticated = computed(() => authenticated.value);
  
  // 액션(actions)
  function setToken(newToken: string) {
    token.value = newToken;
    
    // 새로운 유틸리티를 사용하여 토큰 저장
    setAuthToken(newToken);
    
    // 내부 상태 업데이트
    authenticated.value = true;
  }
  
  function setLoading(isLoading: boolean) {
    loading.value = isLoading;
  }
  
  function setError(errorMessage: string | null) {
    error.value = errorMessage;
  }
  
  function clearAuth() {
    token.value = null;
    
    // 새로운 유틸리티를 사용하여 토큰 삭제
    logoutAuth();
    
    // 내부 상태 업데이트
    authenticated.value = false;
  }
  
  async function handleOAuthCallback(code: string) {
    const userStore = useUserStore();
    
    setLoading(true);
    setError(null);
    
    try {
      // 백엔드에 OAuth 콜백 코드 전송하여 로그인/회원가입 처리
      const response = await processOAuthCallback(code);
      
      // 토큰 저장
      setToken(response.token);

      return response;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'OAuth 로그인 처리 중 오류가 발생했습니다.';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  }
  
  async function handleTokenFromCallback(newToken: string) {
    setToken(newToken);
    await fetchCurrentUser();
  }
  
  async function fetchCurrentUser() {
    const userStore = useUserStore();
    
    setLoading(true);
    setError(null);
    
    try {
      const { api } = await import('@shared/api');
      const response = await api.get('/api/users/me');
      userStore.setUser(response.data);
      // 사용자 정보를 성공적으로 가져왔으면 인증됨으로 표시
      authenticated.value = true;
    } catch (err: any) {
      setError(err.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
      clearAuth();
      userStore.clearUser();
      // 인증 실패
      authenticated.value = false;
    } finally {
      setLoading(false);
    }
  }
  
  function logout() {
    const userStore = useUserStore();
    
    // 인증 상태 및 사용자 정보 초기화
    clearAuth();
    userStore.clearUser();
    
    // 홈페이지로 리다이렉트 (선택사항)
    if (router) {
      router.push('/');
    } else if (typeof window !== 'undefined') {
      window.location.href = '/';
    }
  }
  
  // 초기화 함수 (명시적으로 호출 가능)
  async function initialize() {
    if (typeof window === 'undefined') {
      // 서버 환경에서는 토큰이 있으면 인증된 것으로 간주
      if (token.value) {
        authenticated.value = true;
      }
      return;
    }
    
    // 클라이언트 환경에서는 인증 상태 복원 시도
    try {
      // 인증 상태 복원
      recoverAuthState();
      
      // 복원된 토큰으로 상태 업데이트
      const persistedToken = getAuthToken();
      if (persistedToken) {
        token.value = persistedToken;
        authenticated.value = true;
        
        // 사용자 정보 가져오기
        await fetchCurrentUser();
      }
    } catch (error) {
      clearAuth();
    }
  }
  
  // 컴포넌트 마운트 시 자동 초기화
  if (typeof window !== 'undefined') {
    // 페이지 로드 시 인증 상태 초기화
    setTimeout(initialize, 0);
    
    // 주기적 토큰 검사 (5분마다)
    setInterval(() => {
      syncAuthState();
    }, 5 * 60 * 1000);
  } else {
    // 서버 환경에서는 토큰이 있으면 인증된 것으로 간주
    if (token.value) {
      authenticated.value = true;
    }
  }
  
  // 상태 리셋 함수 구현
  function reset() {
    token.value = getAuthToken();
    loading.value = false;
    error.value = null;
    authenticated.value = !!token.value;
  }
  
  return {
    // 상태
    token,
    loading,
    error,
    
    // 게터
    isAuthenticated,
    
    // 액션
    setToken,
    setLoading,
    setError,
    clearAuth,
    handleOAuthCallback,
    handleTokenFromCallback,
    fetchCurrentUser,
    logout,
    reset,  // reset 함수 추가
    initialize  // 초기화 함수 추가
  };
});
