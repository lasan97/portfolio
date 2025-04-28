<template>
  <div class="bg-gradient-to-r from-purple-500 to-indigo-600 rounded-xl shadow-lg overflow-hidden p-6 text-white">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-2xl font-bold">마이 크레딧</h3>
      <div class="flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div class="text-sm opacity-80">크레딧</div>
      </div>
    </div>
    
    <div class="mb-8 flex items-baseline">
      <span class="text-4xl font-extrabold">{{ formatNumber(credit) }}</span>
      <span class="text-xl ml-2">원</span>
    </div>
    
    <div class="flex items-center justify-between mb-4">
      <div class="text-sm opacity-80">최근 업데이트</div>
      <div class="text-sm font-medium">{{ formatDate(lastUpdated) }}</div>
    </div>
    
    <button 
      @click="isChargeModalOpen = true"
      class="w-full py-3 bg-white bg-opacity-20 hover:bg-opacity-30 transition-all rounded-lg font-medium text-center"
    >
      크레딧 충전하기
    </button>
    
    <!-- 충전 모달 -->
    <div v-if="isChargeModalOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white text-gray-800 rounded-lg shadow-xl p-6 w-full max-w-md mx-4 relative">
        <button @click="isChargeModalOpen = false" class="absolute top-4 right-4 text-gray-500 hover:text-gray-700">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        
        <h3 class="text-xl font-bold mb-4">크레딧 충전하기</h3>
        <p class="text-gray-600 mb-6">원하시는 충전 금액을 선택해주세요.</p>
        
        <div class="grid grid-cols-2 gap-4 mb-6">
          <button 
            v-for="amount in chargeAmounts" 
            :key="amount" 
            @click="selectedAmount = amount"
            :class="[
              'py-3 rounded-lg font-medium transition-all',
              selectedAmount === amount 
                ? 'bg-indigo-600 text-white' 
                : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            ]"
          >
            {{ formatNumber(amount) }}원
          </button>
        </div>
        
        <div class="flex justify-between items-center mb-6">
          <span class="text-gray-700 font-medium">충전 금액:</span>
          <span class="text-xl font-bold text-indigo-600">{{ formatNumber(selectedAmount) }}원</span>
        </div>
        
        <div class="flex space-x-4">
          <button 
            @click="isChargeModalOpen = false"
            class="flex-1 py-3 bg-gray-200 hover:bg-gray-300 transition-all rounded-lg font-medium"
          >
            취소
          </button>
          <button 
            @click="handleCharge"
            :disabled="charging"
            class="flex-1 py-3 bg-indigo-600 hover:bg-indigo-700 transition-all rounded-lg font-medium text-white disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="charging" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              처리중...
            </span>
            <span v-else>충전하기</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, PropType } from 'vue';

export default defineComponent({
  name: 'CreditCard',
  props: {
    credit: {
      type: Number,
      required: true
    },
    lastUpdated: {
      type: Date as PropType<Date>,
      default: () => new Date()
    }
  },
  emits: ['charge'],
  setup(props, { emit }) {
    const isChargeModalOpen = ref(false);
    const chargeAmounts = [10000, 50000, 100000, 1000000];
    const selectedAmount = ref(chargeAmounts[0]);
    const charging = ref(false);
    
    const formatNumber = (num: number): string => {
      return new Intl.NumberFormat('ko-KR').format(num);
    };
    
    const formatDate = (date: Date): string => {
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }).format(date);
    };
    
    const handleCharge = async () => {
      charging.value = true;
      try {
        await emit('charge', selectedAmount.value);
      } finally {
        charging.value = false;
        isChargeModalOpen.value = false;
      }
    };
    
    return {
      isChargeModalOpen,
      chargeAmounts,
      selectedAmount,
      charging,
      formatNumber,
      formatDate,
      handleCharge
    };
  }
});
</script>
