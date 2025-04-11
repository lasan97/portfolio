<template>
  <div class="introduction-page container mx-auto px-4 py-8">
    
    <!-- 로딩 중 표시 -->
    <div v-if="initialLoading || loading" class="flex justify-center items-center py-10">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-900"></div>
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
      <div class="bg-white shadow-md rounded-lg p-8 text-center">
        <div class="mb-6">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-blue-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <div class="text-gray-700 text-2xl font-medium mb-2">자기소개가 준비 중입니다</div>
          <p class="text-gray-600 mb-6">현재 등록된 자기소개 정보가 없습니다. 조금만 기다려주세요!</p>
        </div>
        
        <button 
          v-if="isAdmin"
          @click="startCreating" 
          class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-full transition shadow-md"
        >
          자기소개 등록하기
        </button>
        <button 
          v-else
          @click="goToHome" 
          class="bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium py-2 px-6 rounded-full transition shadow-md"
        >
          홈으로 돌아가기
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user/model/userStore';
import { useIntroduction } from '@features/introduction';
import { IntroductionView, IntroductionEditor } from '@widgets/introduction';
import { type IntroductionDto, type IntroductionUpdateRequest, type IntroductionCreateRequest } from '@entities/introduction';

const authStore = useAuthStore();
const userStore = useUserStore();
const router = useRouter();
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

// 홈으로 돌아가기
const goToHome = () => {
  router.push('/');
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
