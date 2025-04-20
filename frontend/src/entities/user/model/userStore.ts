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
  const userCredit = computed(() => user.value?.credit || 0);
  
  // 액션(actions)
  function setUser(userData: User) {
    // 기존 유저 데이터가 있고, credit 필드가 없으면 기본값 0 추가
    if (!userData.credit) {
      userData.credit = 0;
    }
    
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
      const { apiInstance } = await import('@shared/api');
      const response = await apiInstance.get(`/api/users/${userId}`);
      setUser(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }
  
  // 크레딧 충전 함수
  function chargeCredit(amount: number) {
    if (!user.value) return false;
    
    // 백엔드로 크레딧 충전 요청을 보내는 대신, 현재는 프론트엔드에서만 처리
    const updatedUser = {
      ...user.value,
      credit: (user.value.credit || 0) + amount
    };
    
    setUser(updatedUser);
    return true;
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
    userCredit,
    
    // 액션
    setUser,
    setLoading,
    setError,
    clearUser,
    fetchUser,
    chargeCredit,
    refreshUserFromStorage,
    reset  // reset 함수 추가
  };
});
