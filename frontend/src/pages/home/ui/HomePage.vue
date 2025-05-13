<template>
  <div class="home-page bg-gradient-to-b from-gray-50 to-white min-h-screen py-10">
    <div class="max-w-5xl mx-auto px-4">
      <Card class="shadow-lg border-0 overflow-hidden">
        <div class="py-10 px-6 text-center bg-gradient-to-r from-blue-50 to-green-50 border-b">
          <h1 class="text-4xl font-bold mb-4 text-gray-800">포트폴리오 사이트</h1>
          <p class="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">vue3와 spring boot로 구현된 개인 포트폴리오 사이트입니다.</p>
          <div class="mt-8 flex flex-col sm:flex-row gap-6 justify-center items-center">
            <div class="text-center">
              <p class="mb-4 text-gray-700">프로젝트 소스 코드를 확인해보세요.</p>
              <a 
                href="https://github.com/lasan97/portfolio" 
                target="_blank" 
                rel="noopener noreferrer"
                class="inline-flex items-center text-lg px-8 py-2 rounded-full shadow-md transform hover:scale-105 transition-transform duration-300 bg-gray-800 text-white hover:bg-gray-700"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" class="w-5 h-5 mr-2" viewBox="0 0 16 16">
                  <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.012 8.012 0 0 0 16 8c0-4.42-3.58-8-8-8z"/>
                </svg>
                GitHub 소스코드
              </a>
            </div>
          </div>
        </div>
      </Card>
      
      <!-- 주요 카드 섹션 -->
      <div class="mt-10">
        <!-- 자기소개 카드 -->
        <Card class="shadow-lg hover:shadow-xl transition-all duration-300 border-0 overflow-hidden bg-gradient-to-br from-purple-50 to-indigo-50">
          <div class="p-6">
            <div class="flex items-center mb-4">
              <div class="w-12 h-12 rounded-full bg-purple-100 flex items-center justify-center mr-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h3 class="text-xl font-bold text-gray-800">자기소개</h3>
            </div>
            <p class="text-gray-600 mb-6">저에 대한 자세한 소개와 기술 스택, 경험 등을 확인하실 수 있습니다.</p>
            <Button variant="secondary" class="px-6 py-2 rounded-full shadow-md transform hover:scale-105 transition-transform duration-300 w-full" @click="navigateToIntroduction">
              자기소개 보기
            </Button>
          </div>
        </Card>
      </div>
      
      <!-- 포트폴리오 기술 스택 및 아키텍처 섹션 -->
      <div class="mt-12" ref="techStackSection">
        <h2 class="text-3xl font-bold mb-10 text-center text-gray-800 relative">
          <span class="relative z-10">포트폴리오 프로젝트 기술 스택 및 아키텍처</span>
          <span class="absolute -bottom-3 left-1/2 transform -translate-x-1/2 w-24 h-1 bg-blue-500 rounded-full"></span>
        </h2>
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-10">
          <!-- 프론트엔드 서버 -->
          <Card class="p-0 shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden border-0">
            <div class="bg-blue-600 py-4 px-6">
              <h3 class="text-2xl font-bold text-white">프론트엔드</h3>
            </div>
            <div class="p-6">
              <div class="mb-6">
                <h4 class="font-semibold text-lg mb-3 flex items-center text-blue-700">
                  <span class="w-1.5 h-1.5 bg-blue-500 rounded-full mr-2"></span>
                  주요 기술
                </h4>
                <ul class="grid grid-cols-2 gap-2 mb-4">
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-blue-200 rounded-full mr-2"></span>Vue 3 + TypeScript</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-blue-200 rounded-full mr-2"></span>Vite 빌드 시스템</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-blue-200 rounded-full mr-2"></span>Pinia 상태 관리</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-blue-200 rounded-full mr-2"></span>Vue Router 4</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-blue-200 rounded-full mr-2"></span>Tailwind CSS</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-blue-200 rounded-full mr-2"></span>하이브리드 CSR/SSR</li>
                </ul>
              </div>
              
              <div class="mb-6">
                <h4 class="font-semibold text-lg mb-3 flex items-center text-blue-700">
                  <span class="w-1.5 h-1.5 bg-blue-500 rounded-full mr-2"></span>
                  아키텍처
                </h4>
                <p class="mb-3 text-gray-700 font-medium">Feature-Sliced Design(FSD) 아키텍처 적용</p>
                <ul class="space-y-2">
                  <li class="flex items-start text-gray-700">
                    <span class="w-2 h-2 bg-blue-200 rounded-full mr-2 mt-1.5"></span>
                    <span>기능별 독립적인 모듈화 및 명확한 계층 구조</span>
                  </li>
                  <li class="flex items-start text-gray-700">
                    <span class="w-2 h-2 bg-blue-200 rounded-full mr-2 mt-1.5"></span>
                    <span>단방향 의존성으로 설계 (상위 레이어만 하위 레이어 사용)</span>
                  </li>
                  <li class="flex items-start text-gray-700">
                    <span class="w-2 h-2 bg-blue-200 rounded-full mr-2 mt-1.5"></span>
                    <span>index.ts 파일을 통해서만 모듈 접근 가능</span>
                  </li>
                </ul>
              </div>
              
              <div>
                <h4 class="font-semibold text-lg mb-3 flex items-center text-blue-700">
                  <span class="w-1.5 h-1.5 bg-blue-500 rounded-full mr-2"></span>
                  패키지 구조
                </h4>
                <div class="bg-gray-50 p-3 rounded-md font-mono text-sm text-gray-600">
                  <pre class="whitespace-pre-wrap">
src/
├── app/         # 앱 초기화, 전역 설정
├── pages/       # 페이지 컴포넌트
├── widgets/     # 복합 UI 블록 (헤더, 푸터 등)
├── features/    # 사용자 상호작용 기능
├── entities/    # 비즈니스 엔티티 (사용자, 제품 등)
└── shared/      # 공유 유틸리티, UI 키트</pre>
                </div>
              </div>
            </div>
          </Card>
          
          <!-- 백엔드 서버 -->
          <Card class="p-0 shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden border-0">
            <div class="bg-green-600 py-4 px-6">
              <h3 class="text-2xl font-bold text-white">백엔드</h3>
            </div>
            <div class="p-6">
              <div class="mb-6">
                <h4 class="font-semibold text-lg mb-3 flex items-center text-green-700">
                  <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-2"></span>
                  주요 기술
                </h4>
                <ul class="grid grid-cols-2 gap-2 mb-4">
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-green-200 rounded-full mr-2"></span>Spring Boot 3.2.5</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-green-200 rounded-full mr-2"></span>Spring Security</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-green-200 rounded-full mr-2"></span>Spring Data JPA</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-green-200 rounded-full mr-2"></span>JWT 인증</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-green-200 rounded-full mr-2"></span>OAuth2 (GitHub)</li>
                  <li class="flex items-center text-gray-700"><span class="w-2 h-2 bg-green-200 rounded-full mr-2"></span>Java 17 & Gradle</li>
                </ul>
              </div>
              
              <div class="mb-6">
                <h4 class="font-semibold text-lg mb-3 flex items-center text-green-700">
                  <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-2"></span>
                  주요 기능
                </h4>
                <ul class="space-y-2">
                  <li class="flex items-start text-gray-700">
                    <span class="w-2 h-2 bg-green-200 rounded-full mr-2 mt-1.5"></span>
                    <span>GitHub OAuth2 로그인 및 사용자 관리</span>
                  </li>
                  <li class="flex items-start text-gray-700">
                    <span class="w-2 h-2 bg-green-200 rounded-full mr-2 mt-1.5"></span>
                    <span>JWT 토큰 기반 인증 및 보안</span>
                  </li>
                  <li class="flex items-start text-gray-700">
                    <span class="w-2 h-2 bg-green-200 rounded-full mr-2 mt-1.5"></span>
                    <span>포트폴리오 자기소개 CRUD API</span>
                  </li>
                </ul>
              </div>
              
              <div>
                <h4 class="font-semibold text-lg mb-3 flex items-center text-green-700">
                  <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-2"></span>
                  패키지 구조
                </h4>
                <div class="bg-gray-50 p-3 rounded-md font-mono text-sm text-gray-600">
                  <pre class="whitespace-pre-wrap">
src/
├── controller/   # REST API 컨트롤러
├── service/      # 비즈니스 로직
├── domain/       # 도메인 모델 및 Repository
└── common/       # 공통 기능(보안, 예외처리 등)</pre>
                </div>
              </div>
            </div>
          </Card>
        </div>
        
        <div class="text-center mt-12 p-6 bg-gradient-to-r from-blue-50 to-green-50 rounded-lg shadow-md">
          <p class="text-lg text-gray-700 font-medium">이 포트폴리오는 다양한 웹 기술을 학습하고 적용한 개인 프로젝트입니다.</p>
          <p class="mt-3 text-gray-600">
            프론트엔드 개발 시 Claude MCP를 활용하여 효율적으로 구현했으며,<br>
            깔끔한 설계와 모듈화된 코드 구조로 유지보수성과 확장성을 높였습니다.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, ref } from 'vue';
import { useAuthStore } from '@features/auth';
import { useRouter } from 'vue-router';
import { Card, Button } from '@shared/ui';

export default defineComponent({
  name: 'HomePage',
  components: {
    Card,
    Button
  },
  setup() {
    const authStore = useAuthStore();
    const router = useRouter();
    const techStackSection = ref<HTMLElement | null>(null);

    const isAuthenticated = computed(() => authStore.isAuthenticated);

    const navigateToLogin = () => {
      router.push('/login');
    };

    const navigateToProfile = () => {
      router.push('/profile');
    };
    
    const navigateToIntroduction = () => {
      router.push('/introduction');
    };

    return {
      isAuthenticated,
      navigateToLogin,
      navigateToProfile,
      navigateToIntroduction,
      techStackSection
    };
  }
});
</script>
