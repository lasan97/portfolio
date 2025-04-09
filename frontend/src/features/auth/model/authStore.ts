import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useUserStore } from '@entities/user/model/userStore';
import { processOAuthCallback } from '../api';
import type { TokenResponse } from './types';

export const useAuthStore = defineStore('auth', () => {
  // 상태(state)
  const token = ref<string | null>(localStorage.getItem('token'));
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터(getters)
  const isAuthenticated = computed(() => !!token.value);
  
  // 액션(actions)
  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  }
  
  function setLoading(isLoading: boolean) {
    loading.value = isLoading;
  }
  
  function setError(errorMessage: string | null) {
    error.value = errorMessage;
  }
  
  function clearAuth() {
    token.value = null;
    localStorage.removeItem('token');
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
      
      // 사용자 정보 저장
      userStore.setUser(response.user);
      
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
    } catch (err: any) {
      setError(err.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
      clearAuth();
      userStore.clearUser();
    } finally {
      setLoading(false);
    }
  }
  
  function logout() {
    const userStore = useUserStore();
    clearAuth();
    userStore.clearUser();
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
    logout
  };
});
