<template>
  <header class="bg-white shadow">
    <div class="container mx-auto px-4 py-4">
      <div class="flex justify-between items-center">
        <router-link to="/" class="text-xl font-bold text-blue-600">포트폴리오</router-link>
        
        <nav class="flex items-center space-x-4">
          <router-link to="/" class="text-gray-700 hover:text-blue-600">홈</router-link>
          <router-link to="/ssr" class="text-gray-700 hover:text-blue-600">SSR 예제</router-link>
          
          <template v-if="isAuthenticated">
            <router-link to="/profile" class="text-gray-700 hover:text-blue-600">프로필</router-link>
            <button 
              @click="handleLogout" 
              class="text-gray-700 hover:text-blue-600"
            >
              로그아웃
            </button>
          </template>
          
          <template v-else>
            <router-link to="/login" class="text-gray-700 hover:text-blue-600">로그인</router-link>
          </template>
        </nav>
      </div>
    </div>
  </header>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useAuthStore } from '@features/auth';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'Header',
  setup() {
    const authStore = useAuthStore();
    const router = useRouter();
    
    const isAuthenticated = computed(() => authStore.isAuthenticated);
    
    const handleLogout = () => {
      authStore.logout();
      router.push('/');
    };
    
    return {
      isAuthenticated,
      handleLogout
    };
  }
});
</script>
