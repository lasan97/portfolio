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
import { useCartStore } from '@entities/cart';
import { Header } from '@widgets/header';
import { Footer } from '@widgets/footer';
import { recoverAuthState, syncAuthState } from '@shared/lib';

export default defineComponent({
  name: 'App',
  components: {
    Header,
    Footer
  },
  setup() {
    const authStore = useAuthStore();
    const userStore = useUserStore();
    const cartStore = useCartStore();

    // 다른 브라우저 탭에서의 로그인/로그아웃 감지
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'auth_token' || event.key === null) {
        // localStorage에서 토큰이 변경됨
        const newToken = localStorage.getItem('auth_token');
        
        if (newToken) {
          // 새 토큰이 생겼으면 (로그인) 사용자 정보 가져오기
          if (!authStore.isAuthenticated) {
            authStore.setToken(newToken);
            authStore.fetchCurrentUser();
            // 로그인 시 장바구니 정보 가져오기
            fetchCartItems();
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

    // 장바구니 정보 가져오기
    const fetchCartItems = async () => {
      if (authStore.isAuthenticated) {
        try {
          await cartStore.fetchCartItems();
        } catch (error) {
          console.error('장바구니 정보 가져오기 실패:', error);
        }
      }
    };

    onMounted(() => {
      // 로컬 스토리지 변경 이벤트 리스너 등록
      window.addEventListener('storage', handleStorageChange);
      
      // 첫 번째: 새로고침 후 인증 상태 복원 시도
      recoverAuthState();
      
      // 두 번째: 인증 상태 동기화 검증 
      syncAuthState();
      
      // 세 번째: 저장소 토큰 확인 후 인증 스토어 초기화
      authStore.initialize().then(() => {
        // 인증 정보 초기화 후 장바구니 정보 가져오기
        fetchCartItems();
      });
      
      // 장바구니 초기화 (인증된 사용자만)
      cartStore.initializeCart();
    });
    
    onUnmounted(() => {
      // 이벤트 리스너 제거
      window.removeEventListener('storage', handleStorageChange);
    });

    // 모든 컴포넌트에서 접근 가능한 리프레시 메서드 제공 (개선된 버전)
    const refreshAuthState = async () => {
      try {
        // 인증 상태 동기화
        syncAuthState();
        
        // 인증 스토어 초기화 (전체 프로세스 수행)
        await authStore.initialize();
        
        // 인증 정보 초기화 후 장바구니 정보 가져오기
        await fetchCartItems();
      } catch (error) {
        // 오류 발생 시 기존 방식으로 폴백
        authStore.reset();
        userStore.reset();
      }
    };
    
    provide('refreshAuthState', refreshAuthState);
    // 장바구니 관련 메서드 제공
    provide('fetchCartItems', fetchCartItems);

    return {
      refreshAuthState,
      fetchCartItems
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