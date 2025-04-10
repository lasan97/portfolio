<template>
  <div class="dashboard-editor">
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- 제목 입력 필드 -->
      <div>
        <label for="title" class="block text-sm font-medium text-gray-700 mb-1">제목</label>
        <input
          id="title"
          v-model="formData.title"
          type="text"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          required
        />
        <p v-if="errors.title" class="mt-1 text-sm text-red-600">{{ errors.title }}</p>
      </div>
      
      <!-- 마크다운 에디터 -->
      <div>
        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">내용</label>
        <div class="border border-gray-300 rounded-md overflow-hidden">
          <!-- 에디터 툴바 -->
          <div class="flex flex-wrap gap-1 p-2 bg-gray-50 border-b border-gray-300">
            <button
              v-for="tool in editorTools"
              :key="tool.id"
              type="button"
              @click="applyMarkdown(tool.action)"
              class="p-1.5 rounded hover:bg-gray-200"
              :title="tool.tooltip"
            >
              <component :is="tool.icon" class="w-5 h-5" />
            </button>
          </div>
          
          <!-- 에디터 영역 -->
          <div class="flex flex-col md:flex-row">
            <!-- 입력 영역 -->
            <div class="w-full md:w-1/2 p-2">
              <textarea
                id="content"
                v-model="formData.content"
                class="w-full h-96 p-3 border-0 focus:outline-none focus:ring-0 resize-none font-mono"
                placeholder="마크다운 형식으로 내용을 작성하세요..."
                required
              ></textarea>
            </div>
            
            <!-- 미리보기 영역 -->
            <div class="w-full md:w-1/2 p-2 border-t md:border-t-0 md:border-l border-gray-300 bg-gray-50">
              <div class="h-96 overflow-auto p-3 prose prose-sm max-w-none">
                <MarkdownRenderer :content="formData.content" />
              </div>
            </div>
          </div>
        </div>
        <p v-if="errors.content" class="mt-1 text-sm text-red-600">{{ errors.content }}</p>
      </div>
      
      <!-- 외부 링크 입력 필드 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="githubUrl" class="block text-sm font-medium text-gray-700 mb-1">GitHub URL</label>
          <div class="flex">
            <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
              <GithubIcon class="w-5 h-5" />
            </span>
            <input
              id="githubUrl"
              v-model="formData.githubUrl"
              type="url"
              placeholder="https://github.com/username"
              class="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        
        <div>
          <label for="velogUrl" class="block text-sm font-medium text-gray-700 mb-1">Velog URL</label>
          <div class="flex">
            <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
              <VelogIcon class="w-5 h-5" />
            </span>
            <input
              id="velogUrl"
              v-model="formData.velogUrl"
              type="url"
              placeholder="https://velog.io/@username"
              class="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        
        <div>
          <label for="linkedinUrl" class="block text-sm font-medium text-gray-700 mb-1">LinkedIn URL</label>
          <div class="flex">
            <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
              <LinkedinIcon class="w-5 h-5" />
            </span>
            <input
              id="linkedinUrl"
              v-model="formData.linkedinUrl"
              type="url"
              placeholder="https://linkedin.com/in/username"
              class="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        
        <div>
          <label for="otherUrl" class="block text-sm font-medium text-gray-700 mb-1">기타 웹사이트</label>
          <div class="flex">
            <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
              <LinkIcon class="w-5 h-5" />
            </span>
            <input
              id="otherUrl"
              v-model="formData.otherUrl"
              type="url"
              placeholder="https://example.com"
              class="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </div>
      
      <!-- 버튼 그룹 -->
      <div class="flex justify-end gap-3 pt-4">
        <button
          type="button"
          @click="$emit('cancel')"
          class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
        >
          취소
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? '저장 중...' : '저장' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import MarkdownRenderer from '@shared/ui/MarkdownRenderer.vue';
import GithubIcon from '@shared/ui/icons/GithubIcon.vue';
import VelogIcon from '@shared/ui/icons/VelogIcon.vue';
import LinkedinIcon from '@shared/ui/icons/LinkedinIcon.vue';
import LinkIcon from '@shared/ui/icons/LinkIcon.vue';
import BoldIcon from '@shared/ui/icons/BoldIcon.vue';
import ItalicIcon from '@shared/ui/icons/ItalicIcon.vue';
import HeadingIcon from '@shared/ui/icons/HeadingIcon.vue';
import ListIcon from '@shared/ui/icons/ListIcon.vue';
import CodeIcon from '@shared/ui/icons/CodeIcon.vue';
import LinkEditorIcon from '@shared/ui/icons/LinkEditorIcon.vue';
import ImageIcon from '@shared/ui/icons/ImageIcon.vue';
import type { DashboardDto, DashboardUpdateRequest } from '@entities/dashboard';

const props = defineProps<{
  dashboard: DashboardDto | null;
}>();

const emit = defineEmits<{
  (e: 'save', data: DashboardUpdateRequest): void;
  (e: 'cancel'): void;
}>();

// 폼 데이터
const formData = reactive<DashboardUpdateRequest>({
  title: '',
  content: '',
  githubUrl: '',
  velogUrl: '',
  linkedinUrl: '',
  otherUrl: ''
});

// 에러 상태
const errors = reactive({
  title: '',
  content: ''
});

// 로딩 상태
const loading = ref(false);

// 에디터 도구 설정
const editorTools = [
  {
    id: 'heading',
    icon: HeadingIcon,
    tooltip: '제목',
    action: (textarea: HTMLTextAreaElement) => {
      const selection = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
      const replacement = selection ? `# ${selection}` : '# 제목';
      return { replacement, selectionStart: replacement.length };
    }
  },
  {
    id: 'bold',
    icon: BoldIcon,
    tooltip: '굵게',
    action: (textarea: HTMLTextAreaElement) => {
      const selection = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
      const replacement = selection ? `**${selection}**` : '**굵은 텍스트**';
      return { replacement, selectionStart: selection ? textarea.selectionStart + 2 : 2, selectionEnd: replacement.length - 2 };
    }
  },
  {
    id: 'italic',
    icon: ItalicIcon,
    tooltip: '기울임',
    action: (textarea: HTMLTextAreaElement) => {
      const selection = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
      const replacement = selection ? `*${selection}*` : '*기울임 텍스트*';
      return { replacement, selectionStart: selection ? textarea.selectionStart + 1 : 1, selectionEnd: replacement.length - 1 };
    }
  },
  {
    id: 'list',
    icon: ListIcon,
    tooltip: '목록',
    action: (textarea: HTMLTextAreaElement) => {
      const selection = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
      let replacement;
      
      if (selection) {
        // 선택된 텍스트를 각 줄에 리스트 포맷 적용
        replacement = selection
          .split('\n')
          .map(line => line.trim() ? `- ${line}` : line)
          .join('\n');
      } else {
        replacement = '- 목록 항목';
      }
      
      return { replacement, selectionStart: replacement.length };
    }
  },
  {
    id: 'code',
    icon: CodeIcon,
    tooltip: '코드',
    action: (textarea: HTMLTextAreaElement) => {
      const selection = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
      let replacement;
      
      if (selection.includes('\n')) {
        // 여러 줄 코드 블록
        replacement = `\`\`\`\n${selection}\n\`\`\``;
      } else {
        // 인라인 코드
        replacement = selection ? `\`${selection}\`` : '`코드`';
      }
      
      return { replacement, selectionStart: selection ? textarea.selectionStart + 1 : 1, selectionEnd: replacement.length - 1 };
    }
  },
  {
    id: 'link',
    icon: LinkEditorIcon,
    tooltip: '링크',
    action: (textarea: HTMLTextAreaElement) => {
      const selection = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
      const replacement = selection ? `[${selection}](https://example.com)` : '[링크 텍스트](https://example.com)';
      
      // 링크 텍스트를 선택 상태로 유지
      const selectionStart = selection ? textarea.selectionStart : 1;
      const selectionEnd = selection ? textarea.selectionStart + selection.length + 2 : 11;
      
      return { replacement, selectionStart, selectionEnd };
    }
  },
  {
    id: 'image',
    icon: ImageIcon,
    tooltip: '이미지',
    action: (textarea: HTMLTextAreaElement) => {
      const replacement = '![이미지 설명](https://example.com/image.jpg)';
      return { replacement, selectionStart: 2, selectionEnd: 8 };
    }
  }
];

// 마크다운 적용 함수
const applyMarkdown = (action: Function) => {
  const textarea = document.getElementById('content') as HTMLTextAreaElement;
  if (!textarea) return;
  
  const originalSelectionStart = textarea.selectionStart;
  const originalSelectionEnd = textarea.selectionEnd;
  
  // 액션 실행
  const result = action(textarea);
  
  // 수정된 텍스트 적용
  const startPos = textarea.selectionStart;
  const endPos = textarea.selectionEnd;
  const newText = textarea.value.substring(0, startPos) + result.replacement + textarea.value.substring(endPos);
  
  formData.content = newText;
  
  // 커서 위치 설정을 위해 nextTick 사용
  setTimeout(() => {
    textarea.focus();
    if (result.selectionStart !== undefined && result.selectionEnd !== undefined) {
      textarea.setSelectionRange(
        startPos + result.selectionStart,
        startPos + result.selectionEnd
      );
    } else if (result.selectionStart !== undefined) {
      textarea.setSelectionRange(
        startPos + result.selectionStart,
        startPos + result.selectionStart
      );
    } else {
      textarea.setSelectionRange(
        startPos + result.replacement.length,
        startPos + result.replacement.length
      );
    }
  }, 0);
};

// 폼 제출 핸들러
const handleSubmit = () => {
  // 유효성 검사
  errors.title = formData.title.trim() ? '' : '제목을 입력해주세요.';
  errors.content = formData.content.trim() ? '' : '내용을 입력해주세요.';
  
  if (errors.title || errors.content) {
    return;
  }
  
  loading.value = true;
  emit('save', formData);
};

// 초기 데이터 로드
onMounted(() => {
  if (props.dashboard) {
    formData.title = props.dashboard.title;
    formData.content = props.dashboard.content;
    formData.githubUrl = props.dashboard.githubUrl || '';
    formData.velogUrl = props.dashboard.velogUrl || '';
    formData.linkedinUrl = props.dashboard.linkedinUrl || '';
    formData.otherUrl = props.dashboard.otherUrl || '';
  }
});
</script>
