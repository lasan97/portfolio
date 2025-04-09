<template>
  <div class="home-page">
    <div class="max-w-4xl mx-auto">
      <Card>
        <div class="py-8 text-center">
          <h1 class="text-3xl font-bold mb-4">환영합니다!</h1>
          <p class="text-lg text-gray-600 mb-6">GitHub OAuth2를 이용한 로그인 시스템이 구현된 포트폴리오 사이트입니다.</p>
          <div v-if="isAuthenticated">
            <p class="mb-4">현재 로그인되어 있습니다.</p>
            <Button variant="primary" @click="navigateToProfile">프로필 보기</Button>
          </div>
          <div v-else>
            <p class="mb-4">GitHub 계정으로 로그인하거나 회원가입하세요.</p>
            <Button variant="primary" @click="navigateToLogin">로그인 / 회원가입</Button>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useAuthStore } from '@features/auth';
import { useRouter } from 'vue-router';
import { Card, Button } from '@shared/ui';

export default defineComponent({
  name: 'HomePage',
  components: {
    Card,
    Button
  },
  setup() {
    const authStore = useAuthStore();
    const router = useRouter();

    const isAuthenticated = computed(() => authStore.isAuthenticated);

    const navigateToLogin = () => {
      router.push('/login');
    };

    const navigateToProfile = () => {
      router.push('/profile');
    };

    return {
      isAuthenticated,
      navigateToLogin,
      navigateToProfile
    };
  }
});
</script>
