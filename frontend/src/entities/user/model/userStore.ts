import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from './types';
import { getUserInfo, setUserInfo } from '@shared/lib';

export const useUserStore = defineStore('user', () => {
  // 상태(state) - 쿠키에서 초기값 가져오기
  const user = ref<User | null>(getUserInfo() || null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터(getters)
  const isAuthenticated = computed(() => !!user.value);
  const userDisplayName = computed(() => user.value?.nickname || user.value?.email || '사용자');
  
  // 액션(actions)
  function setUser(userData: User) {
    user.value = userData;
    // 쿠키에 저장
    setUserInfo(userData);
  }
  
  function setLoading(isLoading: boolean) {
    loading.value = isLoading;
  }
  
  function setError(errorMessage: string | null) {
    error.value = errorMessage;
  }
  
  function clearUser() {
    user.value = null;
  }
  
  async function fetchUser(userId: number) {
    setLoading(true);
    setError(null);
    
    try {
      const { api } = await import('@shared/api');
      const response = await api.get(`/api/users/${userId}`);
      setUser(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }
  
  // 쿠키에서 사용자 정보를 업데이트
  function refreshUserFromStorage() {
    const storedUser = getUserInfo();
    if (storedUser) {
      user.value = storedUser;
      return true;
    }
    return false;
  }
  
  // 상태 리셋 함수 구현
  function reset() {
    user.value = getUserInfo() || null;
    loading.value = false;
    error.value = null;
  }
  
  return {
    // 상태
    user,
    loading,
    error,
    
    // 게터
    isAuthenticated,
    userDisplayName,
    
    // 액션
    setUser,
    setLoading,
    setError,
    clearUser,
    fetchUser,
    refreshUserFromStorage,
    reset  // reset 함수 추가
  };
});
