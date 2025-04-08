<template>
  <div class="profile-page py-8">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-2xl font-bold mb-6">내 프로필</h1>
      
      <div v-if="loading" class="flex justify-center py-8">
        <svg class="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>
      
      <div v-else-if="error" class="bg-red-100 border border-red-200 text-red-700 px-4 py-3 rounded">
        {{ error }}
      </div>
      
      <div v-else-if="user">
        <UserCard :user="user" />
        
        <div class="mt-6">
          <Button variant="danger" @click="handleLogout">로그아웃</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { UserCard } from '@/entities/user';
import { Button } from '@/shared/ui';

export default defineComponent({
  name: 'ProfilePage',
  components: {
    UserCard,
    Button
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    
    const user = computed(() => store.state.user.user);
    const loading = computed(() => store.state.user.loading);
    const error = computed(() => store.state.user.error);
    
    onMounted(() => {
      store.dispatch('auth/fetchCurrentUser');
    });
    
    const handleLogout = () => {
      store.dispatch('auth/logout');
      router.push('/');
    };
    
    return {
      user,
      loading,
      error,
      handleLogout
    };
  }
});
</script>
