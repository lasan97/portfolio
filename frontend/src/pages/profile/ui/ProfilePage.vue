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

        <!-- 크레딧 카드와 히스토리 섹션 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 크레딧 카드 (고정 높이) -->
          <div class="h-full">
            <CreditCard :credit="userCredit" @charge="handleChargeCredit" :lastUpdated="creditLastUpdated" />
          </div>

          <!-- 크레딧 히스토리 카드 (스크롤 가능) -->
          <div class="bg-white rounded-xl shadow-md overflow-hidden p-6">
            <h3 class="text-xl font-bold text-gray-800 mb-4">크레딧 히스토리</h3>

            <div class="space-y-4">
              <div v-if="creditHistoryLoading && creditHistory.length === 0" class="flex justify-center py-4">
                <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
              </div>

              <div v-else-if="creditHistory.length === 0" class="text-gray-500 text-center py-8">
                크레딧 거래 내역이 없습니다.
              </div>

              <!-- 스크롤 가능한 히스토리 목록 영역 -->
              <div v-else class="max-h-80 overflow-y-auto pr-2">
                <div class="space-y-3">
                  <div v-for="history in creditHistory" :key="history.id" class="flex items-start border-b border-gray-100 pb-3">
                    <div :class="[
                      'rounded-full p-2 mr-3 flex-shrink-0',
                      history.transactionType === 'INCREASE' ? 'bg-green-100' : 'bg-red-100'
                    ]">
                      <svg v-if="history.transactionType === 'INCREASE'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                      <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex justify-between">
                        <p class="text-gray-700 font-medium truncate">
                          {{ history.transactionType === 'INCREASE' ? '크레딧 충전' : '크레딧 사용' }}
                        </p>
                        <p :class="[
                          'font-semibold ml-2 flex-shrink-0',
                          history.transactionType === 'INCREASE' ? 'text-green-600' : 'text-red-600'
                        ]">
                          {{ history.transactionType === 'INCREASE' ? '+' : '-' }}{{ formatNumber(history.amount) }}원
                        </p>
                      </div>
                      <p class="text-gray-500 text-sm">{{ formatDate(history.transactionDateTime) }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 로딩 표시 (추가 로드 시) -->
              <div v-if="creditHistoryLoading && creditHistory.length > 0" class="flex justify-center py-2">
                <div class="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-600"></div>
              </div>

              <!-- 더보기 버튼 -->
              <div v-if="hasMoreHistory" class="mt-4 text-center">
                <button 
                  @click="loadMoreHistory"
                  :disabled="creditHistoryLoading" 
                  class="text-indigo-600 hover:text-indigo-800 font-medium disabled:text-indigo-300 disabled:cursor-not-allowed"
                >
                  <span v-if="creditHistoryLoading">로딩 중...</span>
                  <span v-else>더 보기</span>
                </button>
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
import { useUserStore, UserCard } from '@entities/user';
import { useRouter } from 'vue-router';
import { Button } from '@shared/ui';
import { CreditCard } from '@features/credit';
import { UserRole } from "@entities/user";
import {formatDate, formatNumber} from "@shared/lib";

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
    const creditHistory = computed(() => userStore.creditHistory);
    const creditHistoryLoading = computed(() => userStore.creditHistoryLoading);
    const creditLastUpdated = ref<Date>(new Date());
    
    // 페이지네이션 관련 상태
    const currentPage = ref(0);
    const hasMoreHistory = ref(true); // 더 불러올 수 있는 데이터가 있는지 여부

    // 사용자 이니셜 계산
    const userInitials = computed(() => {
      if (!user.value?.nickname) return '?';
      return user.value.nickname.charAt(0).toUpperCase();
    });

    onMounted(async () => {
      // 현재 로그인한 사용자 정보 가져오기
      await authStore.fetchCurrentUser();
      
      // 현재 크레딧 정보 가져오기
      await fetchCreditInfo();
      
      // 크레딧 히스토리 가져오기
      await loadCreditHistory();
    });

    // 크레딧 정보 가져오기
    const fetchCreditInfo = async () => {
      const creditData = await userStore.fetchCurrentCredit();
      if (creditData) {
        creditLastUpdated.value = new Date(creditData.updatedAt);
      }
    };

    // 크레딧 히스토리 가져오기
    const loadCreditHistory = async (append = false) => {
      const response = await userStore.fetchCreditHistory(currentPage.value, 10, append);
      
      // 응답에서 더 불러올 데이터가 있는지 확인
      if (response && response.content) {
        // 현재 페이지가 마지막 페이지인지, 또는 데이터가 없는지 확인
        hasMoreHistory.value = 
          !response.last && 
          response.content.length > 0 && 
          response.totalElements > 0;
      } else {
        hasMoreHistory.value = false;
      }
    };

    // 더 많은 히스토리 로드
    const loadMoreHistory = async () => {
      if (creditHistoryLoading.value || !hasMoreHistory.value) return;
      
      currentPage.value += 1;
      await loadCreditHistory(true); // append 파라미터를 true로 설정
    };

    const handleLogout = () => {
      authStore.logout();
      router.push('/');
    };

    const handleChargeCredit = async (amount: number) => {
      const success = await userStore.chargeCredit(amount);

      if (success) {
        // 크레딧 정보 업데이트
        await fetchCreditInfo();
        
        // 히스토리 새로고침 (첫 페이지부터)
        currentPage.value = 0;
        await loadCreditHistory();
      }
    };

    // 날짜 포맷팅에 사용할 옵션
    const dateFormatOptions: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };

    // 날짜 포맷팅 함수 (옵션 적용)
    const formatDateWithOptions = (dateString: string) => {
      return formatDate(dateString, dateFormatOptions);
    };

    return {
      user,
      loading,
      error,
      userCredit,
      userInitials,
      creditHistory,
      creditHistoryLoading,
      creditLastUpdated,
      hasMoreHistory,
      handleLogout,
      handleChargeCredit,
      loadMoreHistory,
      formatNumber,
      formatDate: formatDateWithOptions
    };
  }
});
</script>
