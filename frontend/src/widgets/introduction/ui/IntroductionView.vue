<template>
  <div class="introduction-view">
    <div class="flex justify-between items-start mb-6">
      <h2 class="text-2xl font-bold">{{ introduction?.title }}</h2>
      <button 
        v-if="canEdit" 
        @click="$emit('edit')" 
        class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition"
      >
        수정
      </button>
    </div>
    
    <!-- 마크다운 렌더링 -->
    <div class="prose prose-lg max-w-none mb-8">
      <MarkdownRenderer :content="introduction?.content || ''" />
    </div>
    
    <!-- 외부 링크 섹션 -->
    <div v-if="hasLinks" class="mt-8 pt-6 border-t border-gray-200">
      <h3 class="text-xl font-bold mb-4">링크</h3>
      <div class="flex flex-wrap gap-4">
        <a 
          v-for="link in introduction?.externalLinks" 
          :key="link.url" 
          :href="link.url" 
          target="_blank" 
          rel="noopener noreferrer"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition"
        >
          <img v-if="link.logoUrl" :src="link.logoUrl" alt="Logo" class="w-5 h-5" />
          <LinkIcon v-else class="w-5 h-5" />
          {{ link.name }}
        </a>
      </div>
    </div>
    
    <!-- 마지막 업데이트 정보 -->
    <div class="mt-8 text-sm text-gray-500">
      마지막 업데이트: {{ formatDate(introduction?.updatedAt) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MarkdownRenderer from '@shared/ui/MarkdownRenderer.vue';
import LinkIcon from '@shared/ui/icons/LinkIcon.vue';
import type { IntroductionDto } from '@entities/introduction';

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

// 날짜 포맷팅 함수
const formatDate = (dateString?: string): string => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
};
</script>
