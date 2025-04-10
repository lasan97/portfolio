<template>
  <div class="introduction-page container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">
      {{ getPageTitle }}
    </h1>
    
    <!-- 로딩 중 표시 -->
    <div v-if="initialLoading || loading" class="flex justify-center items-center py-10">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-900"></div>
    </div>
    
    <!-- 오류 표시 -->
    <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      <p>{{ error }}</p>
      <button @click="fetchIntroduction" class="mt-2 bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded">
        다시 시도
      </button>
    </div>
    
    <!-- 에디터 모드 -->
    <template v-else-if="isEditing || isCreating">
      <IntroductionEditor 
        :introduction="introduction" 
        :isCreate="isCreating"
        @save="saveIntroduction" 
        @cancel="cancelEditing" 
      />
    </template>
    
    <!-- 보기 모드 - 자기소개가 있는 경우 -->
    <template v-else-if="introduction">
      <IntroductionView 
        :introduction="introduction" 
        :canEdit="isAdmin" 
        @edit="startEditing" 
      />
    </template>
    
    <!-- 자기소개가 없는 경우 (초기 로딩이 완료된 후에만 표시) -->
    <template v-else-if="!initialLoading">
      <div class="bg-white shadow-md rounded-lg p-6 text-center">
        <div class="text-gray-700 text-xl font-medium mb-4">자기소개 정보가 없습니다</div>
        <p class="text-gray-600 mb-6">현재 등록된 자기소개 정보가 없습니다. 아래 버튼을 눌러 새로운 자기소개를 등록해주세요.</p>
        
        <button 
          v-if="isAdmin"
          @click="startCreating" 
          class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition"
        >
          자기소개 등록하기
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user/model/userStore';
import { useIntroduction } from '@features/introduction';
import { IntroductionView, IntroductionEditor } from '@widgets/introduction';
import { type IntroductionDto, type IntroductionUpdateRequest, type IntroductionCreateRequest } from '@entities/introduction';

const authStore = useAuthStore();
const userStore = useUserStore();
const { fetchIntroduction, updateIntroduction, createIntroduction, loading, error } = useIntroduction();

const introduction = ref<IntroductionDto | null>(null);
const isEditing = ref(false);
const isCreating = ref(false);
const initialLoading = ref(true); // 초기 로딩 상태 추가

// 현재 로그인한 사용자가 ADMIN 권한을 가지고 있는지 확인
const isAdmin = computed(() => {
  return userStore.user?.role?.includes('ADMIN') || false;
});

// 페이지 제목 계산
const getPageTitle = computed(() => {
  if (isCreating.value) return '자기소개 등록';
  if (isEditing.value) return '자기소개 수정';
  return '자기소개';
});

// 초기 데이터 로드
onMounted(async () => {
  await loadIntroduction();
  initialLoading.value = false; // 초기 로딩 완료 표시
});

// 자기소개 데이터 로드
const loadIntroduction = async () => {
  try {
    const result = await fetchIntroduction();
    introduction.value = result;
  } catch (err) {
    console.error('Introduction load error:', err);
    introduction.value = null;
  }
};

// 자기소개 수정 시작
const startEditing = () => {
  isEditing.value = true;
  isCreating.value = false;
};

// 자기소개 생성 시작
const startCreating = () => {
  isCreating.value = true;
  isEditing.value = false;
};

// 편집 취소
const cancelEditing = () => {
  isEditing.value = false;
  isCreating.value = false;
};

// 자기소개 저장 (수정 또는 생성)
const saveIntroduction = async (data: IntroductionUpdateRequest | IntroductionCreateRequest) => {
  try {
    if (isCreating.value) {
      const success = await createIntroduction(data as IntroductionCreateRequest);
      if (success) {
        isCreating.value = false;
        await loadIntroduction();
      }
    } else {
      const success = await updateIntroduction(data as IntroductionUpdateRequest);
      if (success) {
        isEditing.value = false;
        await loadIntroduction();
      }
    }
  } catch (err) {
    console.error('자기소개 저장 에러:', err);
  }
};
</script>
