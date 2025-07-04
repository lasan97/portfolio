<template>
  <div class="callback-page flex items-center justify-center min-h-[400px]">
    <div v-if="loading" class="text-center">
      <div class="mb-4">
        <svg class="animate-spin h-10 w-10 text-blue-600 mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>
      <p class="text-lg">로그인 처리 중입니다...</p>
    </div>

    <div v-if="error" class="text-center text-red-600">
      <p class="text-lg mb-4">{{ error }}</p>
      <Button variant="primary" @click="navigateToLogin">로그인 페이지로 돌아가기</Button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user';
import { Button } from '@shared/ui';

export default defineComponent({
  name: 'OAuthCallbackPage',
  components: {
    Button
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const authStore = useAuthStore();
    const userStore = useUserStore();
    
    const loading = ref(true);
    const error = ref<string | null>(null);

    const navigateToLogin = () => {
      router.push('/login');
    };

    onMounted(async () => {
      try {
        const code = route.query.code as string;
        
        if (!code) {
          throw new Error('인증 코드가 없습니다.');
        }
        
        // GitHub OAuth 콜백 코드로 로그인/회원가입 처리
        await authStore.handleOAuthCallback(code);
        
        // 두 스토어 모두 상태 리셋
        authStore.reset();
        userStore.reset();
        await authStore.fetchCurrentUser();
        // 로그인 성공 시 홈으로 이동
        router.push('/');
      } catch (err: any) {
        error.value = err.message || '로그인 처리 중 오류가 발생했습니다.';
      } finally {
        loading.value = false;
      }
    });

    return {
      loading,
      error,
      navigateToLogin
    };
  }
});
</script>
