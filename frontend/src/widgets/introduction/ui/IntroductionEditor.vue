<template>
  <div class="introduction-editor">
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
              <div class="h-96 overflow-auto p-3">
                <MarkdownRenderer :content="formData.content" />
              </div>
            </div>
          </div>
        </div>
        <p v-if="errors.content" class="mt-1 text-sm text-red-600">{{ errors.content }}</p>
      </div>
      
      <!-- 외부 링크 섹션 -->
      <div>
        <div class="flex justify-between items-center mb-2">
          <label class="block text-sm font-medium text-gray-700">외부 링크</label>
          <button 
            type="button"
            @click="addLink"
            class="text-blue-600 hover:text-blue-800 text-sm font-medium"
          >
            + 링크 추가
          </button>
        </div>
        
        <div v-for="(link, index) in formData.externalLinks" :key="index" class="mb-4 p-4 border border-gray-200 rounded-md">
          <div class="flex justify-between mb-2">
            <h4 class="text-sm font-medium">링크 #{{ index + 1 }}</h4>
            <button 
              type="button"
              @click="() => removeLink(index)"
              class="text-red-600 hover:text-red-800 text-sm"
            >
              삭제
            </button>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label :for="`link-name-${index}`" class="block text-sm font-medium text-gray-700 mb-1">이름</label>
              <input
                :id="`link-name-${index}`"
                v-model="link.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            
            <div>
              <label :for="`link-url-${index}`" class="block text-sm font-medium text-gray-700 mb-1">URL</label>
              <input
                :id="`link-url-${index}`"
                v-model="link.url"
                type="url"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            
            <div>
              <label :for="`link-logo-${index}`" class="block text-sm font-medium text-gray-700 mb-1">로고 이미지</label>
              <div class="flex items-center space-x-2">
                <input
                  :id="`link-file-${index}`"
                  type="file"
                  :accept="allowedFileTypes"
                  @change="(e) => handleFileChange(e, index)"
                  class="hidden"
                />
                <div class="flex-1">
                  <div 
                    @click="() => triggerFileUpload(index)"
                    class="w-full flex items-center px-3 py-2 border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
                  >
                    <input
                      :id="`link-logo-${index}`"
                      v-model="link.logoUrl"
                      type="url"
                      class="flex-1 bg-transparent border-0 outline-none cursor-pointer"
                      placeholder="로고 이미지를 업로드하세요"
                      readonly
                    />
                    <button
                      type="button"
                      class="ml-2 text-sm text-blue-500 hover:text-blue-700"
                    >
                      파일 선택
                    </button>
                  </div>
                </div>
              </div>
              <div class="mt-2">
                <p v-if="uploadStatus[index]?.loading" class="text-sm text-blue-600">업로드 중...</p>
                <p v-if="uploadStatus[index]?.error" class="text-sm text-red-600">{{ uploadStatus[index].error }}</p>
                <p v-if="uploadStatus[index]?.success" class="text-sm text-green-600">
                  {{ uploadStatus[index].fileName }} 업로드 완료
                </p>
                <div v-if="link.logoUrl" class="mt-2">
                  <img :src="link.logoUrl" alt="로고 미리보기" class="h-10 object-contain" />
                </div>
              </div>
            </div>
            

          </div>
        </div>
        
        <p v-if="formData.externalLinks.length === 0" class="text-sm text-gray-500 italic mt-2">
          아직 추가된 링크가 없습니다. '링크 추가' 버튼을 눌러 링크를 추가해주세요.
        </p>
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
import { MarkdownRenderer } from '@shared/ui';
import { BoldIcon, ItalicIcon, HeadingIcon, ListIcon, CodeIcon, LinkEditorIcon, ImageIcon } from '@shared/ui';
import type { IntroductionDto, IntroductionUpdateRequest, IntroductionCreateRequest, ExternalLink } from '@entities/introduction';
import { uploadFile } from '@features/file';

const props = defineProps<{
  introduction: IntroductionDto | null;
  isCreate?: boolean;
}>();

const emit = defineEmits<{
  (e: 'save', data: IntroductionUpdateRequest | IntroductionCreateRequest): void;
  (e: 'cancel'): void;
}>();

// 폼 데이터
const formData = reactive({
  title: '',
  content: '',
  externalLinks: [] as ExternalLink[]
});

// 파일 업로드 트리거
const triggerFileUpload = (index: number) => {
  const input = document.getElementById(`link-file-${index}`) as HTMLInputElement;
  if (input) {
    input.click();
  }
};

// 파일 변경 처리
const handleFileChange = async (event: Event, index: number) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  
  if (!file) return;
  
  // 이미지 파일인지 확인
  if (!file.type.startsWith('image/')) {
    uploadStatus[index] = {
      loading: false,
      error: '이미지 파일만 업로드 가능합니다.',
      success: false,
      fileName: file.name
    };
    target.value = '';
    return;
  }
  
  // 업로드 상태 초기화
  uploadStatus[index] = {
    loading: true,
    error: null,
    success: false,
    fileName: file.name
  };
  
  try {
    // 파일 업로드 API 호출
    const response = await uploadFile(file);
    
    // 로고 URL 업데이트
    formData.externalLinks[index].logoUrl = response.fileUrl;
    
    // 업로드 성공 상태 설정
    uploadStatus[index] = {
      loading: false,
      error: null,
      success: true,
      fileName: response.fileName
    };
  } catch (error) {
    console.error('파일 업로드 오류:', error);
    
    // 업로드 실패 상태 설정
    uploadStatus[index] = {
      loading: false,
      error: '파일 업로드에 실패했습니다. 다시 시도해주세요.',
      success: false,
      fileName: file.name
    };
  }
  
  // 입력 필드 초기화 (같은 파일 다시 선택 가능하도록)
  target.value = '';
};

// 에러 상태
const errors = reactive({
  title: '',
  content: ''
});

// 파일 입력 허용 타입
const allowedFileTypes = 'image/jpeg,image/png,image/gif,image/svg+xml,image/webp';

// 로딩 상태
const loading = ref(false);

// 파일 입력 참조
const fileInputs = ref<HTMLInputElement[]>([]);

// 업로드 상태 관리 
interface UploadStatusItem {
  loading: boolean;
  error: string | null;
  success: boolean;
  fileName: string | null;
}

const uploadStatus = reactive<Record<number, UploadStatusItem>>({});

// 외부 링크 추가
const addLink = () => {
  const newIndex = formData.externalLinks.length;
  formData.externalLinks.push({
    name: '',
    url: '',
    logoUrl: ''
  });
  
  // 새 링크에 대한 업로드 상태 초기화
  uploadStatus[newIndex] = {
    loading: false,
    error: null,
    success: false,
    fileName: null
  };
};

// 외부 링크 제거
const removeLink = (index: number) => {
  formData.externalLinks.splice(index, 1);
  
  // 업로드 상태도 정리
  const newUploadStatus: Record<number, UploadStatusItem> = {};
  Object.entries(uploadStatus).forEach(([key, value]) => {
    const keyNum = parseInt(key);
    if (keyNum < index) {
      newUploadStatus[keyNum] = value;
    } else if (keyNum > index) {
      newUploadStatus[keyNum - 1] = value;
    }
  });
  
  // 상태 업데이트
  Object.keys(uploadStatus).forEach(key => {
    delete uploadStatus[parseInt(key)];
  });
  
  Object.entries(newUploadStatus).forEach(([key, value]) => {
    uploadStatus[parseInt(key)] = value;
  });
};

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
  
  // 외부 링크 유효성 검사
  let hasLinkError = false;
  
  if (formData.externalLinks) {
    formData.externalLinks.forEach((link) => {
      if (!link.name?.trim() || !link.url?.trim()) {
        hasLinkError = true;
      }
    });
  }
  
  if (errors.title || errors.content || hasLinkError) {
    return;
  }
  
  loading.value = true;
  emit('save', formData);
};

// 초기 데이터 로드
onMounted(() => {
  if (props.introduction) {
    formData.title = props.introduction.title;
    formData.content = props.introduction.content;
    
    // externalLinks 배열 초기화
    formData.externalLinks = props.introduction.externalLinks ? 
      [...props.introduction.externalLinks] : [];
      
    // 기존 링크에 대한 업로드 상태 초기화
    formData.externalLinks.forEach((link, index) => {
      uploadStatus[index] = {
        loading: false,
        error: null,
        success: Boolean(link.logoUrl), // 로고 URL이 있으면 성공 상태로 표시
        fileName: link.logoUrl ? link.logoUrl.split('/').pop() || '기존 로고' : null
      };
    });
  } else {
    // 새로운 자기소개 생성 시 기본값 설정
    formData.title = '';
    formData.content = '';
    formData.externalLinks = [];
  }
});
</script>
