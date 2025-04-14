<template>
  <div class="profile-page py-8 bg-gray-50 min-h-screen">
    <div class="max-w-4xl mx-auto px-4">
      <h1 class="text-3xl font-bold mb-8 text-gray-800">내 프로필</h1>
      
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
      </div>
      
      <div v-else-if="error" class="bg-red-100 border border-red-200 text-red-700 px-6 py-4 rounded-lg shadow-sm">
        {{ error }}
      </div>
      
      <div v-else-if="user" class="space-y-8">
        <!-- 프로필 카드 -->
        <div class="bg-white rounded-xl shadow-md overflow-hidden">
          <div class="bg-gradient-to-r from-indigo-500 to-blue-500 h-28 relative">
            <div class="absolute -bottom-12 left-6">
              <div v-if="user.profileImageUrl" class="relative">
                <img 
                  :src="user.profileImageUrl" 
                  alt="Profile" 
                  class="w-24 h-24 rounded-full object-cover border-4 border-white"
                />
              </div>
              <div v-else class="w-24 h-24 bg-white rounded-full flex items-center justify-center border-4 border-white">
                <span class="text-indigo-500 text-2xl font-bold">{{ userInitials }}</span>
              </div>
            </div>
          </div>
          
          <div class="pt-14 pb-6 px-6">
            <div class="flex justify-between items-center mb-4">
              <div>
                <h3 class="text-2xl font-bold text-gray-800">{{ user.nickname }}</h3>
                <p class="text-gray-600">{{ user.email }}</p>
              </div>
              <div class="flex space-x-2">
                <Button variant="primary" class="px-4">프로필 수정</Button>
                <Button variant="danger" @click="handleLogout">로그아웃</Button>
              </div>
            </div>
            
            <div class="flex items-center text-sm text-gray-500">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118l-2.8-2.034c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              <span>{{ user.role || UserRole.USER }} 회원</span>
            </div>
          </div>
        </div>
        
        <!-- 크레딧 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <CreditCard :credit="userCredit" @charge="handleChargeCredit" />
          
          <!-- 최근 활동 카드 (예시) -->
          <div class="bg-white rounded-xl shadow-md overflow-hidden p-6">
            <h3 class="text-xl font-bold text-gray-800 mb-4">최근 활동</h3>
            
            <div class="space-y-4">
              <div v-if="recentActivities.length === 0" class="text-gray-500 text-center py-8">
                최근 활동 내역이 없습니다.
              </div>
              
              <div v-for="(activity, index) in recentActivities" :key="index" class="flex items-start border-b border-gray-100 pb-3">
                <div class="bg-blue-100 rounded-full p-2 mr-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div class="flex-1">
                  <p class="text-gray-700 font-medium">{{ activity.title }}</p>
                  <p class="text-gray-500 text-sm">{{ activity.date }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, onMounted, ref } from 'vue';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user';
import { useRouter } from 'vue-router';
import { UserCard } from '@entities/user';
import { Button } from '@shared/ui';
import { CreditCard } from '@features/credit';
import {UserRole} from "../../../shared/config";

export default defineComponent({
  name: 'ProfilePage',
  computed: {
    UserRole() {
      return UserRole
    }
  },
  components: {
    UserCard,
    Button,
    CreditCard
  },
  setup() {
    const authStore = useAuthStore();
    const userStore = useUserStore();
    const router = useRouter();
    
    const user = computed(() => userStore.user);
    const loading = computed(() => userStore.loading);
    const error = computed(() => userStore.error);
    const userCredit = computed(() => userStore.userCredit);
    
    // 사용자 이니셜 계산
    const userInitials = computed(() => {
      if (!user.value?.nickname) return '?';
      return user.value.nickname.charAt(0).toUpperCase();
    });
    
    // 최근 활동 데이터 (예시)
    const recentActivities = ref([
      {
        title: '크레딧 충전',
        date: '2025년 4월 12일',
      },
      {
        title: '프로필 정보 수정',
        date: '2025년 4월 10일',
      },
      {
        title: '계정 생성',
        date: '2025년 4월 5일',
      },
    ]);
    
    onMounted(() => {
      authStore.fetchCurrentUser();
      
      // 사용자 데이터에 credit 필드가 없으면 초기값 설정
      if (user.value && !user.value.hasOwnProperty('credit')) {
        const updatedUser = {
          ...user.value,
          credit: 0
        };
        userStore.setUser(updatedUser);
      }
    });
    
    const handleLogout = () => {
      authStore.logout();
      router.push('/');
    };
    
    const handleChargeCredit = (amount: number) => {
      const success = userStore.chargeCredit(amount);
      
      if (success) {
        // 충전 성공 시 최근 활동에 추가
        recentActivities.value.unshift({
          title: `크레딧 충전: ${new Intl.NumberFormat('ko-KR').format(amount)}원`,
          date: new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
          }).format(new Date())
        });
      }
    };
    
    return {
      user,
      loading,
      error,
      userCredit,
      userInitials,
      recentActivities,
      handleLogout,
      handleChargeCredit
    };
  }
});
</script>
