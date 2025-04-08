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
import { useStore } from 'vuex';
import { Button } from '@/shared/ui';

export default defineComponent({
  name: 'OAuthCallbackPage',
  components: {
    Button
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    
    const loading = ref(true);
    const error = ref<string | null>(null);

    const navigateToLogin = () => {
      router.push('/login');
    };

    onMounted(async () => {
      try {
        const token = route.query.token as string;
        
        if (!token) {
          throw new Error('토큰이 없습니다.');
        }
        
        await store.dispatch('auth/handleTokenFromCallback', token);
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
