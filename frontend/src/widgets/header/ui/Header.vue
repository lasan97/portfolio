<template>
  <header class="bg-white shadow">
    <div class="container mx-auto px-4 py-4">
      <div class="flex justify-between items-center">
        <router-link to="/" class="flex items-center">
          <img src="/image/logo.png" alt="h-martin 로고" class="h-8 w-auto mr-2" />
          <span class="text-xl font-bold text-blue-600">h-martin 포트폴리오</span>
        </router-link>
        
        <nav class="flex items-center space-x-4">
          <router-link to="/products" class="text-gray-700 hover:text-blue-600">상품목록</router-link>
          <router-link to="/introduction" class="text-gray-700 hover:text-blue-600">자기소개</router-link>
          <template v-if="isAdmin">
            <router-link to="/ssr" class="text-gray-700 hover:text-blue-600">SSR 예제</router-link>
          </template>

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
import { defineComponent, computed, watch, onMounted } from 'vue';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user';
import { useRouter } from 'vue-router';
import {UserRole} from "@shared/config";

export default defineComponent({
  name: 'Header',
  setup() {
    const authStore = useAuthStore();
    const userStore = useUserStore();
    const router = useRouter();
    
    // 인증 상태 리액티브하게 계산
    const isAuthenticated = computed(() => authStore.isAuthenticated);
    const isAdmin = computed(() => {
      return userStore.user?.role === UserRole.ADMIN || false;
    });
    // 사용자 이름 표시
    const userDisplayName = computed(() => userStore.userDisplayName);
    
    // 인증 상태 변경 시 새로고침 없이 UI 업데이트
    watch(() => authStore.token, (newToken) => {
      // 토큰이 변경되면 사용자 정보 새로고침
      if (newToken) {
        userStore.refreshUserFromStorage();
      }
    });
    
    // 컴포넌트 마운트 시 인증 상태 확인
    onMounted(() => {
      // 스토어의 인증 상태와 로컬스토리지/쿠키의 상태가 일치하는지 확인
      const tokenExists = !!authStore.token;
      
      // 로컬스토리지에 토큰은 있지만 사용자 정보가 없는 경우 사용자 정보 다시 로드
      if (tokenExists && !userStore.user) {
        authStore.fetchCurrentUser();
      }
    });
    
    const handleLogout = () => {
      // 로그아웃 처리
      authStore.logout();
      
      // 스토어 리셋 (상태 갱신을 확실히 하기 위해)
      authStore.reset();
      userStore.reset();
    };
    
    return {
      isAuthenticated,
      isAdmin,
      userDisplayName,
      handleLogout
    };
  }
});
</script>
