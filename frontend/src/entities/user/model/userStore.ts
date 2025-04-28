import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User, CreditHistory, CreditInfo } from './types';
import { getUserInfo, setUserInfo } from '@shared/lib';
import { userRepository } from '../api/userRepository';

export const useUserStore = defineStore('user', () => {
  // 상태(state) - 쿠키에서 초기값 가져오기
  const user = ref<User | null>(getUserInfo() || null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const creditHistory = ref<CreditHistory[]>([]);
  const creditHistoryLoading = ref(false);
  
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
      const userData = await userRepository.fetchUser(userId);
      setUser(userData);
    } catch (err: any) {
      setError(err.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }
  
  // 현재 사용자의 크레딧 정보를 가져오는 함수
  async function fetchCurrentCredit() {
    if (!user.value) return false;
    
    setLoading(true);
    setError(null);
    
    try {
      // API 호출 
      const creditInfo = await userRepository.credit.fetchCurrent();
      
      // 사용자 정보 업데이트
      if (user.value) {
        const updatedUser = {
          ...user.value,
          credit: creditInfo.amount
        };
        setUser(updatedUser);
      }
      
      return creditInfo;
    } catch (err: any) {
      setError(err.response?.data?.message || '크레딧 정보를 가져오는데 실패했습니다.');
      return false;
    } finally {
      setLoading(false);
    }
  }
  
  // 크레딧 충전 함수 (백엔드 API 연결)
  async function chargeCredit(amount: number) {
    if (!user.value) return false;
    
    setLoading(true);
    setError(null);
    
    try {
      // 백엔드로 크레딧 충전 요청
      await userRepository.credit.increase(amount);
      
      // 충전 후 최신 크레딧 정보 가져오기
      await fetchCurrentCredit();
      
      return true;
    } catch (err: any) {
      setError(err.response?.data?.message || '크레딧 충전에 실패했습니다.');
      return false;
    } finally {
      setLoading(false);
    }
  }
  
  // 크레딧 히스토리 가져오기
  async function fetchCreditHistory(page = 0, size = 10, append = false) {
    if (!user.value) return false;
    
    creditHistoryLoading.value = true;
    
    try {
      // API 호출
      const historyData = await userRepository.credit.fetchHistory(page, size);
      
      // append가 true이면 기존 데이터에 추가, 아니면 교체
      if (append && historyData.content.length > 0) {
        creditHistory.value = [...creditHistory.value, ...historyData.content];
      } else {
        creditHistory.value = historyData.content;
      }
      
      return historyData;
    } catch (err: any) {
      console.error('크레딧 히스토리를 가져오는데 실패했습니다:', err);
      return false;
    } finally {
      creditHistoryLoading.value = false;
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
    creditHistory.value = [];
    creditHistoryLoading.value = false;
  }
  
  return {
    // 상태
    user,
    loading,
    error,
    creditHistory,
    creditHistoryLoading,
    
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
    fetchCurrentCredit,
    chargeCredit,
    fetchCreditHistory,
    refreshUserFromStorage,
    reset
  };
});
