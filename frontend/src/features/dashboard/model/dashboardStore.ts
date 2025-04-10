import { ref } from 'vue';
import { defineStore } from 'pinia';
import { getDashboard, updateDashboard as updateDashboardApi } from '../api';
import type { DashboardDto, DashboardUpdateRequest } from '@entities/dashboard';

/**
 * 대시보드 관련 상태 및 로직을 관리하는 Pinia 스토어
 */
export const useDashboardStore = defineStore('dashboard', () => {
  // 상태 (state)
  const dashboard = ref<DashboardDto | null>(null);
  const loading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // 액션 (actions)
  /**
   * 대시보드 정보 가져오기
   */
  const fetchDashboard = async (): Promise<DashboardDto | null> => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await getDashboard();
      dashboard.value = response.data;
      return dashboard.value;
    } catch (err: any) {
      error.value = err.response?.data?.message || '대시보드 정보를 불러오는 중 오류가 발생했습니다.';
      return null;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 대시보드 정보 업데이트
   */
  const updateDashboard = async (dashboardData: DashboardUpdateRequest): Promise<DashboardDto | null> => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await updateDashboardApi(dashboardData);
      dashboard.value = response.data;
      return dashboard.value;
    } catch (err: any) {
      error.value = err.response?.data?.message || '대시보드 정보를 업데이트하는 중 오류가 발생했습니다.';
      return null;
    } finally {
      loading.value = false;
    }
  };

  return {
    dashboard,
    loading,
    error,
    fetchDashboard,
    updateDashboard
  };
});

/**
 * 대시보드 관련 비즈니스 로직을 위한 Composition API 훅
 */
export function useDashboard() {
  const store = useDashboardStore();
  
  return {
    dashboard: store.dashboard,
    loading: store.loading,
    error: store.error,
    fetchDashboard: store.fetchDashboard,
    updateDashboard: store.updateDashboard
  };
}
