<template>
  <div class="dashboard-view">
    <div class="flex justify-between items-start mb-6">
      <h2 class="text-2xl font-bold">{{ dashboard?.title }}</h2>
      <button 
        v-if="canEdit" 
        @click="$emit('edit')" 
        class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition"
      >
        편집
      </button>
    </div>
    
    <!-- 마크다운 렌더링 -->
    <div class="prose prose-lg max-w-none mb-8">
      <MarkdownRenderer :content="dashboard?.content || ''" />
    </div>
    
    <!-- 외부 링크 섹션 -->
    <div v-if="hasLinks" class="mt-8 pt-6 border-t border-gray-200">
      <h3 class="text-xl font-bold mb-4">링크</h3>
      <div class="flex flex-wrap gap-4">
        <a 
          v-if="dashboard?.githubUrl" 
          :href="dashboard.githubUrl" 
          target="_blank" 
          rel="noopener noreferrer"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition"
        >
          <GithubIcon class="w-5 h-5" />
          GitHub
        </a>
        
        <a 
          v-if="dashboard?.velogUrl" 
          :href="dashboard.velogUrl" 
          target="_blank" 
          rel="noopener noreferrer"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition"
        >
          <VelogIcon class="w-5 h-5" />
          Velog
        </a>
        
        <a 
          v-if="dashboard?.linkedinUrl" 
          :href="dashboard.linkedinUrl" 
          target="_blank" 
          rel="noopener noreferrer"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition"
        >
          <LinkedinIcon class="w-5 h-5" />
          LinkedIn
        </a>
        
        <a 
          v-if="dashboard?.otherUrl" 
          :href="dashboard.otherUrl" 
          target="_blank" 
          rel="noopener noreferrer"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition"
        >
          <LinkIcon class="w-5 h-5" />
          웹사이트
        </a>
      </div>
    </div>
    
    <!-- 마지막 업데이트 정보 -->
    <div class="mt-8 text-sm text-gray-500">
      마지막 업데이트: {{ formatDate(dashboard?.updatedAt) }}
      <span v-if="dashboard?.updatedBy"> by {{ dashboard.updatedBy }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MarkdownRenderer from '@shared/ui/MarkdownRenderer.vue';
import GithubIcon from '@shared/ui/icons/GithubIcon.vue';
import VelogIcon from '@shared/ui/icons/VelogIcon.vue'; 
import LinkedinIcon from '@shared/ui/icons/LinkedinIcon.vue';
import LinkIcon from '@shared/ui/icons/LinkIcon.vue';
import type { DashboardDto } from '@entities/dashboard';

const props = defineProps<{
  dashboard: DashboardDto | null;
  canEdit: boolean;
}>();

const emit = defineEmits<{
  (e: 'edit'): void;
}>();

// 링크 존재 여부 확인
const hasLinks = computed(() => {
  return !!(
    props.dashboard?.githubUrl || 
    props.dashboard?.velogUrl || 
    props.dashboard?.linkedinUrl || 
    props.dashboard?.otherUrl
  );
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
