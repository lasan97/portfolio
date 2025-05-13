<template>
  <div class="introduction-view bg-gradient-to-br from-blue-50 to-indigo-100 min-h-screen py-8">
    <div class="max-w-4xl mx-auto">
      <!-- 메인 카드 -->
      <div class="bg-white rounded-2xl shadow-xl overflow-hidden backdrop-blur-sm bg-white/95">
        <!-- 헤더 섹션 -->
        <div class="relative bg-gradient-to-r from-blue-600 to-indigo-600 px-8 py-10">
          <div class="absolute inset-0 bg-black opacity-10"></div>
          <div class="relative">
            <div class="flex justify-between items-start">
              <div class="flex items-center space-x-4">
                <div class="w-16 h-16 rounded-full overflow-hidden bg-white/20 backdrop-blur-sm p-1">
                  <img 
                    src="https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/public/profile.jpg" 
                    alt="프로필 사진" 
                    class="w-full h-full object-cover rounded-full"
                  />
                </div>
                <div>
                  <h1 class="text-3xl md:text-4xl font-bold text-white">{{ introduction?.title }}</h1>
                  <p class="text-blue-100 text-lg mt-2">개인 소개</p>
                </div>
              </div>
              
              <button 
                v-if="canEdit" 
                @click="$emit('edit')" 
                class="bg-white/20 backdrop-blur-sm hover:bg-white/30 text-white font-medium py-3 px-6 rounded-xl transition-all duration-200 transform hover:scale-105 shadow-lg"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 inline mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                수정
              </button>
            </div>
          </div>
        </div>
        
        <!-- 컨텐츠 섹션 -->
        <div class="p-8 md:p-10">
          <!-- 마크다운 렌더링 -->
          <div class="prose prose-lg max-w-none">
            <MarkdownRenderer :content="introduction?.content || ''" />
          </div>
          
          <!-- 외부 링크 섹션 -->
          <div v-if="hasLinks" class="mt-12 p-6 bg-gray-50 rounded-2xl">
            <div class="flex items-center mb-6">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
              </svg>
              <h3 class="text-xl font-bold text-gray-800">관련 링크</h3>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <a 
                v-for="link in introduction?.externalLinks" 
                :key="link.url" 
                :href="link.url" 
                target="_blank" 
                rel="noopener noreferrer"
                class="group flex items-center gap-3 p-4 bg-white rounded-xl border border-gray-200 hover:border-blue-300 hover:shadow-md transition-all duration-200 transform hover:-translate-y-1"
              >
                <div class="p-2 bg-blue-50 rounded-lg group-hover:bg-blue-100 transition-colors">
                  <img v-if="link.logoUrl" :src="link.logoUrl" alt="Logo" class="w-6 h-6" />
                  <LinkIcon v-else class="w-6 h-6 text-blue-600" />
                </div>
                <span class="font-medium text-gray-700 group-hover:text-blue-600 transition-colors">{{ link.name }}</span>
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-400 group-hover:text-blue-500 ml-auto transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                </svg>
              </a>
            </div>
          </div>
          
          <!-- 마지막 업데이트 정보 -->
          <div class="mt-8 pt-6 border-t border-gray-200">
            <div class="flex items-center text-sm text-gray-500">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              마지막 업데이트: {{ formatDateWithOptions(introduction?.updatedAt) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { LinkIcon, MarkdownRenderer } from '@shared/ui';
import type { IntroductionDto } from '@features/introduction';
import {formatDate} from "@shared/lib";

const props = defineProps<{
  introduction: IntroductionDto | null;
  canEdit: boolean;
}>();

const emit = defineEmits<{
  (e: 'edit'): void;
}>();

// 링크 존재 여부 확인
const hasLinks = computed(() => {
  return props.introduction?.externalLinks && props.introduction.externalLinks.length > 0;
});
// 날짜 포맷팅에 사용할 옵션
const dateFormatOptions: Intl.DateTimeFormatOptions = {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit'
};

// 날짜 포맷팅 함수 (옵션 적용)
const formatDateWithOptions = (dateString: string | undefined) => {
  return formatDate(dateString, dateFormatOptions);
};
</script>
