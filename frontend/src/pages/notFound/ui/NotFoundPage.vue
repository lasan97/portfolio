<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8 text-center">
      <div>
        <h1 class="text-9xl font-extrabold text-gray-900">404</h1>
        <h2 class="mt-2 text-3xl font-bold text-gray-900">페이지를 찾을 수 없습니다</h2>
        <p class="mt-2 text-base text-gray-600">요청하신 페이지가 존재하지 않습니다.</p>
      </div>
      <div class="mt-6">
        <router-link to="/" class="inline-flex items-center justify-center px-4 py-2 bg-blue-600 border border-transparent rounded-md font-medium text-white hover:bg-blue-700">
          홈으로 돌아가기
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'NotFoundPage'
});
</script>
