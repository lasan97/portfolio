<template>
  <div class="container mx-auto max-w-7xl px-4 py-12">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-gray-900 mb-4">포트폴리오 프로젝트</h1>
      <p class="text-xl text-gray-600 max-w-2xl mx-auto">
        Spring Boot + Vue.js를 사용한 FSD 아키텍처 기반 포트폴리오 프로젝트입니다.
      </p>
      
      <div class="mt-12 grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-xl font-semibold mb-3">FSD 아키텍처</h3>
          <p class="text-gray-600">
            Feature-Sliced Design 아키텍처로 구조화되어 있어 확장성과 유지보수성이 뛰어납니다.
          </p>
        </div>
        
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-xl font-semibold mb-3">TypeScript</h3>
          <p class="text-gray-600">
            타입 안전성을 보장하는 TypeScript로 개발되어 높은 코드 품질을 유지합니다.
          </p>
        </div>
        
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-xl font-semibold mb-3">테일윈드 CSS</h3>
          <p class="text-gray-600">
            유틸리티 우선 CSS 프레임워크인 테일윈드를 통해 빠르고 일관된 UI를 구현합니다.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'HomePage'
})
</script>
