import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from './types';

export const useUserStore = defineStore('user', () => {
  // 상태(state)
  const user = ref<User | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 게터(getters)
  const isAuthenticated = computed(() => !!user.value);
  
  // 액션(actions)
  function setUser(userData: User) {
    user.value = userData;
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
  
  return {
    // 상태
    user,
    loading,
    error,
    
    // 게터
    isAuthenticated,
    
    // 액션
    setUser,
    setLoading,
    setError,
    clearUser,
    fetchUser
  };
});
