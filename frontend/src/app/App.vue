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
import { defineComponent, onMounted } from 'vue';
import { useAuthStore } from '@features/auth';
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

    onMounted(() => {
      // 로컬 스토리지에서 토큰을 확인하고 사용자 정보 가져오기
      const token = localStorage.getItem('token');
      if (token) {
        authStore.fetchCurrentUser();
      }
    });

    return {};
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
