import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { getDashboard, updateDashboard as updateDashboardApi, createDashboard as createDashboardApi } from '../api';
import type { DashboardDto, DashboardUpdateRequest, DashboardCreateRequest } from '@entities/dashboard';

/**
 * 대시보드 관련 상태 및 로직을 관리하는 Pinia 스토어
 */
export const useDashboardStore = defineStore('dashboard', () => {
  // 상태 (state)
  const dashboard = ref<DashboardDto | null>(null);
  const loading = ref<boolean>(false);
  const error = ref<string | null>(null);
  const hasDashboard = computed(() => dashboard.value !== null);

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
      console.error('Dashboard fetch error:', err);
      if (err.response?.status === 404) {
        // 대시보드가 없는 경우 null로 설정하고 오류로 처리하지 않음
        dashboard.value = null;
        error.value = null;
      } else {
        error.value = err.response?.data?.message || '대시보드 정보를 불러오는 중 오류가 발생했습니다.';
      }
      return null;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 대시보드 정보 업데이트
   */
  const updateDashboard = async (dashboardData: DashboardUpdateRequest): Promise<boolean> => {
    loading.value = true;
    error.value = null;
    
    try {
      await updateDashboardApi(dashboardData);
      // 업데이트 후 대시보드 정보 다시 가져오기
      await fetchDashboard();
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || '대시보드 정보를 업데이트하는 중 오류가 발생했습니다.';
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 대시보드 생성
   */
  const createDashboard = async (dashboardData: DashboardCreateRequest): Promise<boolean> => {
    loading.value = true;
    error.value = null;
    
    try {
      await createDashboardApi(dashboardData);
      // 생성 후 대시보드 정보 다시 가져오기
      await fetchDashboard();
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || '대시보드를 생성하는 중 오류가 발생했습니다.';
      return false;
    } finally {
      loading.value = false;
    }
  };

  return {
    dashboard,
    loading,
    error,
    hasDashboard,
    fetchDashboard,
    updateDashboard,
    createDashboard
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
    hasDashboard: store.hasDashboard,
    fetchDashboard: store.fetchDashboard,
    updateDashboard: store.updateDashboard,
    createDashboard: store.createDashboard
  };
}
