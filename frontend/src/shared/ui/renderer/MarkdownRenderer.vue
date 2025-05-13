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

// 마크다운 렌더링 설정
const setupMarked = () => {
  // GitHub 스타일의 마크다운 옵션 설정
  marked.setOptions({
    gfm: true,
    breaks: true
  });

  // 커스텀 렌더러 설정 (이모지 지원)
  const renderer = new marked.Renderer();
  
  // 이모지 텍스트를 유니코드로 변환하는 맵
  const emojiMap: { [key: string]: string } = {
    ':computer:': '💻',
    ':rocket:': '🚀',
    ':file_cabinet:': '🗃️',
    ':cloud:': '☁️',
    ':books:': '📚',
    ':hammer_and_wrench:': '🛠️',
    ':gear:': '⚙️',
    ':database:': '🗄️',
    ':package:': '📦',
    ':globe_with_meridians:': '🌐'
  };

  // 텍스트 렌더러 오버라이드 (이모지 처리)
  const originalText = renderer.text.bind(renderer);
  renderer.text = function(text: string) {
    let result = text;
    
    // 이모지 처리
    for (const [emoji, unicode] of Object.entries(emojiMap)) {
      result = result.replace(new RegExp(emoji.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), unicode);
    }
    
    // 직접 유니코드 이모지도 그대로 유지
    return originalText(result);
  };

  // 강조 텍스트 (볼드/이탤릭) 개선
  const originalStrong = renderer.strong.bind(renderer);
  renderer.strong = function(text: string) {
    return `<strong class="font-bold text-gray-900">${text}</strong>`;
  };

  const originalEm = renderer.em.bind(renderer);
  renderer.em = function(text: string) {
    return `<em class="italic text-gray-800">${text}</em>`;
  };

  // 헤딩 스타일 개선
  const originalHeading = renderer.heading.bind(renderer);
  renderer.heading = function(text: string, level: 1 | 2 | 3 | 4 | 5 | 6, raw: string) {
    const escaped = text.toLowerCase().replace(/[^\w]+/g, '-');
    const id = `heading-${escaped}`;
    
    const styles: Record<number, string> = {
      1: 'text-3xl font-bold mb-6 mt-8 text-gray-900 border-b border-gray-200 pb-2',
      2: 'text-2xl font-bold mb-4 mt-6 text-gray-900',
      3: 'text-xl font-bold mb-3 mt-5 text-gray-800',
      4: 'text-lg font-bold mb-2 mt-4 text-gray-800',
      5: 'text-base font-bold mb-2 mt-3 text-gray-700',
      6: 'text-sm font-bold mb-2 mt-2 text-gray-700'
    };
    
    const styleClass = styles[level] || '';
    return `<h${level} id="${id}" class="${styleClass}">${text}</h${level}>`;
  };

  // 리스트 스타일 개선
  const originalListitem = renderer.listitem.bind(renderer);
  renderer.listitem = function(text: string) {
    return `<li class="mb-2 text-gray-700">${text}</li>`;
  };

  const originalList = renderer.list.bind(renderer);
  renderer.list = function(body: string, ordered: boolean) {
    const tag = ordered ? 'ol' : 'ul';
    const classes = ordered 
      ? 'list-decimal list-inside space-y-2 mb-4 ml-4' 
      : 'list-disc list-inside space-y-2 mb-4 ml-4';
    return `<${tag} class="${classes}">${body}</${tag}>`;
  };

  // 코드 블록 스타일 개선
  const originalCodespan = renderer.codespan.bind(renderer);
  renderer.codespan = function(text: string) {
    return `<code class="bg-gray-100 text-gray-800 px-2 py-1 rounded text-sm font-mono">${text}</code>`;
  };

  const originalCode = renderer.code.bind(renderer);
  renderer.code = function(code: string, infostring: string | undefined) {
    const language = infostring ? infostring.split(/\s+/)[0] : '';
    return `<pre class="bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto mb-4"><code class="language-${language} font-mono text-sm">${code}</code></pre>`;
  };

  // 인용구 스타일 개선
  const originalBlockquote = renderer.blockquote.bind(renderer);
  renderer.blockquote = function(quote: string) {
    return `<blockquote class="border-l-4 border-blue-500 pl-4 py-2 my-4 italic text-gray-700 bg-blue-50">${quote}</blockquote>`;
  };

  // 링크 스타일 개선
  const originalLink = renderer.link.bind(renderer);
  renderer.link = function(href: string, title: string | null, text: string) {
    return `<a href="${href}" ${title ? `title="${title}"` : ''} class="text-blue-600 hover:text-blue-800 underline" target="_blank" rel="noopener noreferrer">${text}</a>`;
  };

  // 이미지 스타일 개선
  const originalImage = renderer.image.bind(renderer);
  renderer.image = function(href: string, title: string | null, text: string) {
    return `<img src="${href}" alt="${text}" ${title ? `title="${title}"` : ''} class="max-w-full h-auto rounded-lg shadow-md mx-auto my-4">`;
  };

  marked.use({ renderer });
};

// 마크다운 설정 초기화
setupMarked();

// 마크다운 렌더링
const renderedContent = computed(() => {
  if (!props.content) return '';
  
  try {
    // 먼저 이모지 단축어를 유니코드로 변환
    let content = props.content;
    
    // 일반적인 이모지 패턴 변환
    content = content
      .replace(/💻/g, '💻')
      .replace(/🚀/g, '🚀')
      .replace(/🗃️/g, '🗃️')
      .replace(/☁️/g, '☁️')
      .replace(/📚/g, '📚')
      .replace(/🛠️/g, '🛠️')
      .replace(/⚙️/g, '⚙️')
      .replace(/🗄️/g, '🗄️')
      .replace(/📦/g, '📦')
      .replace(/🌐/g, '🌐');
    
    // 마크다운을 HTML로 변환
    const rawHtml = marked.parse(content) as string;
    
    // DOMPurify 설정 - 더 허용적인 설정
    return DOMPurify.sanitize(rawHtml, {
      ADD_ATTR: ['class', 'id', 'target', 'rel'],
      ADD_TAGS: ['iframe', 'code', 'pre'],
      ALLOW_DATA_ATTR: true,
      FORBID_TAGS: [],
      FORBID_ATTR: []
    });
  } catch (error) {
    console.error('마크다운 렌더링 오류:', error);
    return '<p class="text-red-500">마크다운 렌더링 중 오류가 발생했습니다.</p>';
  }
});
</script>

<style>
/* 마크다운 스타일 개선 */
.markdown-body {
  line-height: 1.7;
  color: #333;
  overflow-wrap: break-word;
}

/* GitHub 마크다운 CSS 오버라이드 */
.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  color: inherit;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 0;
}

.markdown-body li {
  word-wrap: break-all;
}

/* 코드 블록 스타일링 */
.markdown-body pre {
  border-radius: 6px;
  font-size: 14px;
}

.markdown-body code {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  border-radius: 3px;
}

/* 키보드 단축키나 특수 텍스트 스타일 */
.markdown-body kbd {
  background-color: #f3f3f3;
  border: 1px solid #d0d7de;
  border-radius: 3px;
  color: #24292f;
  display: inline-block;
  font-size: 11px;
  padding: 3px 5px;
  vertical-align: middle;
}

/* 테이블 스타일 개선 */
.markdown-body table {
  border-collapse: collapse;
  border-spacing: 0;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-body table th,
.markdown-body table td {
  border: 1px solid #d0d7de;
  padding: 6px 13px;
}

.markdown-body table th {
  background-color: #f6f8fa;
  font-weight: 600;
}

/* 체크박스 리스트 */
.markdown-body .task-list-item {
  list-style-type: none;
}

.markdown-body .task-list-item .task-list-item-checkbox {
  margin: 0 0.2em 0.25em -1.6em;
  vertical-align: middle;
}
</style>
