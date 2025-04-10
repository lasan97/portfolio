<template>
  <div class="app-container min-h-screen bg-gray-100">
    <Header />
    <main class="container mx-auto py-6 px-4">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onUnmounted, provide } from 'vue';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user';
import { Header } from '@widgets/header';
import { Footer } from '@widgets/footer';

export default defineComponent({
  name: 'App',
  components: {
    Header,
    Footer
  },
  setup() {
    const authStore = useAuthStore();
    const userStore = useUserStore();

    // 다른 브라우저 탭에서의 로그인/로그아웃 감지
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'token' || event.key === null) {
        // localStorage에서 토큰이 변경됨
        const newToken = localStorage.getItem('token');
        
        if (newToken) {
          // 새 토큰이 생겼으면 (로그인) 사용자 정보 가져오기
          if (!authStore.isAuthenticated) {
            authStore.setToken(newToken);
            authStore.fetchCurrentUser();
          }
        } else {
          // 토큰이 삭제됐으면 (로그아웃) 상태 초기화
          if (authStore.isAuthenticated) {
            authStore.clearAuth();
            userStore.clearUser();
          }
        }
      } else if (event.key === 'user_info') {
        // 사용자 정보가 변경됨
        userStore.refreshUserFromStorage();
      }
    };

    onMounted(() => {
      // 로컬 스토리지 변경 이벤트 리스너 등록
      window.addEventListener('storage', handleStorageChange);
      
      // 초기 로드 시 토큰이 있으면 사용자 정보 가져오기
      const token = localStorage.getItem('token');
      if (token && !userStore.user) {
        authStore.fetchCurrentUser();
      }
    });
    
    onUnmounted(() => {
      // 이벤트 리스너 제거
      window.removeEventListener('storage', handleStorageChange);
    });

    // 모든 컴포넌트에서 접근 가능한 리프레시 메서드 제공
    const refreshAuthState = () => {
      const token = localStorage.getItem('token');
      if (token) {
        authStore.setToken(token);
        authStore.fetchCurrentUser();
      } else {
        authStore.clearAuth();
        userStore.clearUser();
      }
      
      // 두 스토어 모두 리셋
      authStore.reset();
      userStore.reset();
    };
    
    provide('refreshAuthState', refreshAuthState);

    return {
      refreshAuthState
    };
  }
});
</script>

<style>
.app-container {
  display: flex;
  flex-direction: column;
}

main {
  flex: 1;
}
</style>
