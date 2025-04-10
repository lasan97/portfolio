<template>
  <div class="dashboard-page container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">
      {{ getPageTitle }}
    </h1>
    
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
    <template v-else-if="isEditing || isCreating">
      <DashboardEditor 
        :dashboard="dashboard" 
        :isCreate="isCreating"
        @save="saveDashboard" 
        @cancel="cancelEditing" 
      />
    </template>
    
    <!-- 보기 모드 - 대시보드가 있는 경우 -->
    <template v-else-if="dashboard">
      <DashboardView 
        :dashboard="dashboard" 
        :canEdit="isAdmin" 
        @edit="startEditing" 
      />
    </template>
    
    <!-- 대시보드가 없는 경우 -->
    <template v-else>
      <div class="bg-white shadow-md rounded-lg p-6 text-center">
        <div class="text-gray-700 text-xl font-medium mb-4">대시보드 정보가 없습니다</div>
        <p class="text-gray-600 mb-6">현재 등록된 대시보드 정보가 없습니다. 아래 버튼을 눌러 새로운 대시보드를 등록해주세요.</p>
        
        <button 
          v-if="isAdmin"
          @click="startCreating" 
          class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition"
        >
          대시보드 등록하기
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user/model/userStore';
import { useDashboard } from '@features/dashboard';
import { DashboardView, DashboardEditor } from '@widgets/dashboard';
import { type DashboardDto, type DashboardUpdateRequest, type DashboardCreateRequest } from '@entities/dashboard';

const authStore = useAuthStore();
const userStore = useUserStore();
const { fetchDashboard, updateDashboard, createDashboard, loading, error } = useDashboard();

const dashboard = ref<DashboardDto | null>(null);
const isEditing = ref(false);
const isCreating = ref(false);

// 현재 로그인한 사용자가 ADMIN 권한을 가지고 있는지 확인
const isAdmin = computed(() => {
  return userStore.user?.role?.includes('ADMIN') || false;
});

// 페이지 제목 계산
const getPageTitle = computed(() => {
  if (isCreating.value) return '대시보드 등록';
  if (isEditing.value) return '대시보드 수정';
  return '대시보드';
});

// 초기 데이터 로드
onMounted(async () => {
  await loadDashboard();
});

// 대시보드 데이터 로드
const loadDashboard = async () => {
  try {
    const result = await fetchDashboard();
    dashboard.value = result;
  } catch (err) {
    console.error('Dashboard load error:', err);
    dashboard.value = null;
  }
};

// 대시보드 수정 시작
const startEditing = () => {
  isEditing.value = true;
  isCreating.value = false;
};

// 대시보드 생성 시작
const startCreating = () => {
  isCreating.value = true;
  isEditing.value = false;
};

// 편집 취소
const cancelEditing = () => {
  isEditing.value = false;
  isCreating.value = false;
};

// 대시보드 저장 (수정 또는 생성)
const saveDashboard = async (data: DashboardUpdateRequest | DashboardCreateRequest) => {
  try {
    console.log('저장할 데이터:', data);
    
    if (isCreating.value) {
      const success = await createDashboard(data as DashboardCreateRequest);
      if (success) {
        isCreating.value = false;
        await loadDashboard();
      }
    } else {
      const success = await updateDashboard(data as DashboardUpdateRequest);
      if (success) {
        isEditing.value = false;
        await loadDashboard();
      }
    }
  } catch (err) {
    console.error('대시보드 저장 에러:', err);
  }
};
</script>
