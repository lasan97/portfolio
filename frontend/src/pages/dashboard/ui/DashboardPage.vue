<template>
  <div class="dashboard-page container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">{{ isEditing ? '대시보드 수정' : '대시보드' }}</h1>
    
    <!-- 로딩 중 표시 -->
    <div v-if="loading" class="flex justify-center items-center py-10">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-900"></div>
    </div>
    
    <!-- 오류 표시 -->
    <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      <p>{{ error }}</p>
      <button @click="fetchDashboard" class="mt-2 bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded">
        다시 시도
      </button>
    </div>
    
    <!-- 에디터 모드 -->
    <template v-else-if="isEditing">
      <DashboardEditor 
        :dashboard="dashboard" 
        @save="saveDashboard" 
        @cancel="isEditing = false" 
      />
    </template>
    
    <!-- 보기 모드 -->
    <template v-else>
      <DashboardView 
        :dashboard="dashboard" 
        :can-edit="isAdmin" 
        @edit="isEditing = true" 
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user/model/userStore';
import { useDashboard } from '@features/dashboard';
import { DashboardView, DashboardEditor } from '@widgets/dashboard';
import {type DashboardDto, DashboardUpdateRequest} from '@entities/dashboard';

const authStore = useAuthStore();
const userStore = useUserStore();
const { fetchDashboard, updateDashboard, loading, error } = useDashboard();

const dashboard = ref<DashboardDto | null>(null);
const isEditing = ref(false);

// 현재 로그인한 사용자가 ADMIN 권한을 가지고 있는지 확인
const isAdmin = computed(() => {
  console.log(userStore.user);
  console.log(userStore.user?.role);
  return userStore.user?.role?.includes('ADMIN') || false;
});

// 초기 데이터 로드
onMounted(async () => {
  const result = await fetchDashboard();
  if (result) {
    dashboard.value = result;
  }
});

// 대시보드 저장
const saveDashboard = async (updatedDashboard: DashboardUpdateRequest) => {
  const result = await updateDashboard(updatedDashboard);
  if (result) {
    dashboard.value = result;
    isEditing.value = false;
  }
};
</script>
