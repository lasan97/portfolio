<template>
  <div class="introduction-page bg-gradient-to-br from-slate-50 to-blue-50 min-h-screen">
    <!-- 로딩 중 표시 -->
    <div v-if="initialLoading || loading" class="flex justify-center items-center py-20">
      <div class="relative">
        <div class="animate-spin rounded-full h-16 w-16 border-4 border-blue-200 border-t-blue-600"></div>
        <div class="absolute inset-0 rounded-full bg-blue-50 opacity-20"></div>
      </div>
    </div>

    <!-- 에디터 모드 -->
    <template v-else-if="isEditing || isCreating">
      <div class="py-8">
        <IntroductionEditor 
          :introduction="introduction" 
          :isCreate="isCreating"
          @save="saveIntroduction" 
          @cancel="cancelEditing" 
        />
      </div>
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
      <div class="py-20">
        <div class="max-w-md mx-auto bg-white rounded-2xl shadow-xl p-8 text-center transform hover:scale-105 transition-all duration-300">
          <div class="mb-8">
            <!-- 애니메이션 효과가 있는 아이콘 -->
            <div class="relative mx-auto w-24 h-24 mb-6">
              <div class="absolute inset-0 bg-blue-100 rounded-full animate-ping opacity-20"></div>
              <div class="absolute inset-2 bg-blue-50 rounded-full animate-pulse"></div>
              <div class="absolute inset-4 bg-blue-600 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
            </div>
            
            <h2 class="text-2xl font-bold text-gray-800 mb-3">자기소개가 준비 중입니다</h2>
            <p class="text-gray-600 mb-6">현재 등록된 자기소개 정보가 없습니다. 조금만 기다려주세요!</p>
          </div>

          <button 
            v-if="isAdmin"
            @click="startCreating" 
            class="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-medium py-3 px-6 rounded-xl transition-all duration-200 transform hover:scale-105 shadow-lg"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 inline mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            자기소개 등록하기
          </button>
          <button 
            v-else
            @click="goToHome" 
            class="w-full bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium py-3 px-6 rounded-xl transition-all duration-200 shadow-md"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 inline mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user';
import { useIntroduction } from '@features/introduction';
import { IntroductionView, IntroductionEditor } from '@widgets/introduction';
import { type IntroductionDto, type IntroductionUpdateRequest, type IntroductionCreateRequest } from '@features/introduction';
import {UserRole} from "@entities/user";

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
  return userStore.user?.role === UserRole.ADMIN || false;
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
