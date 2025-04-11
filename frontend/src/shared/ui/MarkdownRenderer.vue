<template>
  <div class="markdown-body" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import 'github-markdown-css/github-markdown.css';

const props = defineProps<{
  content: string;
}>();

// 마크다운 렌더링 설정 - marked v9+ 버전에 맞게 구성
const markedOptions = {
  mangle: false,
  headerPrefix: 'heading-',
  gfm: true,
  breaks: true
};

// 마크다운 파서 설정
marked.use(markedOptions);

// 마크다운 렌더링
const renderedContent = computed(() => {
  if (!props.content) return '';
  try {
    // 마크다운을 HTML로 변환하고 XSS 방지를 위해 정화
    const rawHtml = marked.parse(props.content);
    
    // DOMPurify 설정 - 필요한 HTML 태그와 속성을 보존
    return DOMPurify.sanitize(rawHtml, {
      ADD_ATTR: ['class', 'id', 'target'], 
      ADD_TAGS: ['iframe'], 
      ALLOW_DATA_ATTR: true
    });
  } catch (error) {
    console.error('마크다운 렌더링 오류:', error);
    return '<p class="text-red-500">마크다운 렌더링 중 오류가 발생했습니다.</p>';
  }
});
</script>

<style>
/* 추가 스타일이 필요한 경우 여기에 작성하세요 */
.markdown-body {
  background-color: #ffffff;
  padding: 1rem;
}
</style>
