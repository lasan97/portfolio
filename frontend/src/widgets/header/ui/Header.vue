<template>
  <header class="bg-white shadow">
    <div class="container mx-auto px-4 py-4">
      <div class="flex justify-between items-center">
        <router-link to="/" class="flex items-center">
          <img src="/image/logo.png" alt="h-martin 로고" class="h-8 w-auto mr-2" />
          <span class="text-xl font-bold text-blue-600">han-martin</span>
        </router-link>

        <nav class="flex items-center space-x-4">
          <router-link to="/products" class="text-gray-700 hover:text-blue-600">상품목록</router-link>
          <router-link to="/introduction" class="text-gray-700 hover:text-blue-600">자기소개</router-link>
          <template v-if="isAdmin">
            <router-link to="/ssr" class="text-gray-700 hover:text-blue-600">SSR 예제</router-link>
          </template>

          <!-- 장바구니 링크 -->
          <router-link 
            to="/cart" 
            class="relative flex items-center text-gray-700 hover:text-blue-600"
            title="장바구니"
          >
            <!-- 장바구니 아이콘 -->
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            
            <!-- 장바구니 아이템 수 뱃지 -->
            <span 
              v-if="totalItems > 0" 
              class="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center"
            >
              {{ totalItems > 99 ? '99+' : totalItems }}
            </span>
          </router-link>

          <template v-if="isAuthenticated">
            <router-link to="/orders" class="text-gray-700 hover:text-blue-600">주문내역</router-link>
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
import { useCartStore } from '@entities/cart';
import { useRouter } from 'vue-router';
import {UserRole} from "@entities/user";

export default defineComponent({
  name: 'Header',
  setup() {
    const authStore = useAuthStore();
    const userStore = useUserStore();
    const cartStore = useCartStore();
    const router = useRouter();

    // 인증 상태 리액티브하게 계산
    const isAuthenticated = computed(() => authStore.isAuthenticated);
    const isAdmin = computed(() => {
      return userStore.user?.role === UserRole.ADMIN || false;
    });
    // 사용자 이름 표시
    const userDisplayName = computed(() => userStore.userDisplayName);
    
    // 장바구니 아이템 수 계산
    const totalItems = computed(() => cartStore.totalItems);

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
      
      // 페이지 로드 시 장바구니 정보 가져오기
      if (isAuthenticated.value) {
        cartStore.fetchCartItems().catch(error => {
          console.error('장바구니 데이터 로드 중 오류 발생:', error);
        });
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
      totalItems,
      handleLogout
    };
  }
});
</script>
