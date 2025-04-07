<template>
  <div class="min-h-screen bg-gray-50 font-sans antialiased text-gray-900">
    <!-- 헤더 -->
    <header class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <router-link to="/" class="text-xl font-bold text-blue-600">
                Portfolio FSD
              </router-link>
            </div>
            <nav class="ml-6 flex space-x-8">
              <router-link to="/" class="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-gray-500 hover:text-gray-700 hover:border-gray-300">
                홈
              </router-link>
            </nav>
          </div>
          <div class="flex items-center">
            <router-link to="/login" class="ml-4 px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-700 hover:bg-gray-100">
              로그인
            </router-link>
            <router-link to="/register" class="ml-4 px-3 py-2 rounded-md text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
              회원가입
            </router-link>
          </div>
        </div>
      </div>
    </header>

    <!-- 메인 컨텐츠 -->
    <main>
      <router-view/>
    </main>

    <!-- 푸터 -->
    <footer class="bg-white mt-12 border-t border-gray-200">
      <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <p class="text-center text-gray-500 text-sm">
          &copy; {{ new Date().getFullYear() }} Portfolio FSD. 모든 권리 보유.
        </p>
      </div>
    </footer>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'App'
})
</script>
