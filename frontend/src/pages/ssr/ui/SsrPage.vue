<template>
  <div class="ssr-page-container">
    <h1 class="text-3xl font-bold mb-6">SSR 샘플 페이지</h1>
    
    <div class="bg-white shadow-md rounded-lg p-6 mb-6">
      <h2 class="text-xl font-semibold mb-4">서버 사이드 렌더링 예제</h2>
      <p class="mb-4">
        이 페이지는 Vue 3와 Vite에서 SSR(Server-Side Rendering)을 시연하기 위한 예제 페이지입니다.
        SSR은 초기 페이지 로드 시 서버에서 컴포넌트를 렌더링하여 성능과 SEO를 향상시키는 기법입니다.
      </p>
      
      <div class="bg-gray-100 p-4 rounded-md mb-4">
        <h3 class="font-medium mb-2">SSR의 장점:</h3>
        <ul class="list-disc pl-5 space-y-1">
          <li>첫 페이지 로딩 속도 향상</li>
          <li>검색 엔진 최적화(SEO) 개선</li>
          <li>소셜 미디어 공유 시 메타데이터 제공</li>
          <li>느린 네트워크 환경에서 더 나은 사용자 경험</li>
        </ul>
      </div>
    </div>

    <div class="bg-white shadow-md rounded-lg p-6 mb-6">
      <h2 class="text-xl font-semibold mb-4">서버에서 렌더링된 데이터</h2>
      <p class="mb-4">
        {{ isHydrated ? '이 데이터는 서버에서 렌더링되었습니다.' : '이 데이터는 클라이언트에서 로드되었습니다.' }}
      </p>
      
      <div v-if="isLoading" class="flex justify-center my-6">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>
      
      <div v-else class="border rounded-md p-4">
        <div v-if="error" class="text-red-600">
          {{ error }}
        </div>
        <div v-else>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div 
              v-for="(item, index) in items" 
              :key="index"
              class="border rounded p-3 hover:bg-gray-50 transition"
            >
              <p class="font-medium">{{ item.title }}</p>
              <p class="text-gray-600 text-sm">{{ item.description }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="mt-4 p-3 bg-blue-50 rounded-md">
        <p><strong>렌더링 정보:</strong> {{ renderType }}</p>
        <p><strong>렌더링 시간:</strong> {{ renderTime }}</p>
      </div>
    </div>
    
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
      <h2 class="text-xl font-semibold mb-4">구현 방법</h2>
      <p class="mb-4">
        Vue와 Vite에서 SSR을 구현하기 위해서는 다음 단계가 필요합니다:
      </p>
      
      <ol class="list-decimal pl-5 space-y-2">
        <li>
          <span class="font-medium">서버 진입점 생성:</span>
          <code class="bg-gray-100 px-2 py-1 rounded ml-2">server.js</code> 파일을 생성하여 서버 로직을 구현
        </li>
        <li>
          <span class="font-medium">클라이언트 진입점 분리:</span>
          <code class="bg-gray-100 px-2 py-1 rounded ml-2">client.js</code>와 
          <code class="bg-gray-100 px-2 py-1 rounded">server.js</code>로 나누어 관리
        </li>
        <li>
          <span class="font-medium">SSR 플러그인 설정:</span>
          Vite SSR 플러그인 설정 추가
        </li>
        <li>
          <span class="font-medium">빌드 스크립트 추가:</span>
          클라이언트와 서버 빌드를 위한 스크립트 구성
        </li>
      </ol>
    </div>
  </div>
</template>

<script lang="ts">
// SSR 전용 데이터 프리페칭 메서드
export default {
  name: 'SsrPage',
  
  // SSR 전용 데이터 프리페칭 메서드
  ssrPrefetch({ route, store, cookie }: {
    route: any;
    store: any;
    cookie?: string
  }) {
    // 실제 구현에서는 여기서 스토어 액션을 호출하여 서버에서 데이터를 가져옵니다
    console.log('서버에서 데이터 프리페칭:', route.path);
    
    // 예제를 위한 간단한 비동기 작업 반환
    return new Promise<void>(resolve => {
      setTimeout(() => {
        // 실제로는 여기서 store를 업데이트합니다
        resolve();
      }, 100);
    });
  }
}
</script>

<script setup lang="ts">
import { ref, onMounted, onServerPrefetch, computed } from 'vue';

// 상태 관리
const isLoading = ref(true);
const error = ref<string | null>(null);
const items = ref<any[]>([]);
const renderTime = ref(new Date().toLocaleTimeString());
const isHydrated = ref(false);

// 서버에서 렌더링되었는지 확인
const renderType = computed(() => {
  return typeof window === 'undefined' 
    ? '서버 사이드 렌더링 (SSR)' 
    : '클라이언트 사이드 렌더링 (CSR)';
});

// 데이터 가져오기 함수
async function fetchData() {
  try {
    isLoading.value = true;
    
    // API 호출을 시뮬레이션 (실제로는 서버에서 pre-rendering 시 수행)
    await new Promise(resolve => setTimeout(resolve, 500));
    
    items.value = [
      { 
        title: '서버에서 렌더링된 항목 1', 
        description: '이 데이터는 일반적으로 서버에서 미리 가져와 초기 HTML에 포함됩니다.' 
      },
      { 
        title: '서버에서 렌더링된 항목 2', 
        description: 'SSR 모드에서는 이 텍스트가 JavaScript가 로드되기 전에 표시됩니다.' 
      },
      { 
        title: '서버에서 렌더링된 항목 3', 
        description: '완전한 SSR 구현에서는 이 콘텐츠가 페이지 소스에 포함됩니다.' 
      },
      { 
        title: '서버에서 렌더링된 항목 4', 
        description: '검색 엔진은 JavaScript 없이도 이 콘텐츠를 볼 수 있습니다.' 
      }
    ];
    
    // 하이드레이션 여부 감지
    if (typeof window !== 'undefined') {
      isHydrated.value = !!(window as any).__INITIAL_STATE__;
    }
  } catch (err) {
    error.value = '데이터를 불러오는 중 오류가 발생했습니다.';
    console.error('데이터 로딩 오류:', err);
  } finally {
    isLoading.value = false;
  }
}

// 서버에서 데이터 프리페칭
onServerPrefetch(async () => {
  console.log('서버에서 onServerPrefetch 실행');
  await fetchData();
});

// 클라이언트에서 마운트될 때 데이터 가져오기 (필요한 경우)
onMounted(() => {
  console.log('클라이언트에서 onMounted 실행');
  renderTime.value = new Date().toLocaleTimeString();
  
  // 이미 데이터가 있으면 (SSR에서 하이드레이션) 스킵, 아니면 로드
  if (items.value.length === 0) {
    fetchData();
  } else {
    isLoading.value = false;
    
    // 하이드레이션 여부 감지
    if (typeof window !== 'undefined') {
      isHydrated.value = !!(window as any).__INITIAL_STATE__;
    }
  }
});
</script>

<style scoped>
.ssr-page-container {
  max-width: 1000px;
  margin: 0 auto;
}
</style>