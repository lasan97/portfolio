<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
      <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 mb-6">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
      </div>
      
      <h1 class="text-3xl font-bold text-gray-900 mb-4">주문 완료</h1>
      <p class="text-gray-600 mb-6">
        주문이 성공적으로 완료되었습니다.<br>
        주문 내역은 이메일로 발송되었습니다.
      </p>
      
      <div class="bg-gray-50 p-4 rounded-md mb-6">
        <p class="text-sm text-gray-600 mb-2">주문번호</p>
        <p class="text-lg font-semibold text-gray-900">{{ orderNumber }}</p>
      </div>
      
      <div class="space-y-4">
        <button 
          @click="goToHome" 
          class="w-full bg-indigo-600 text-white py-3 px-4 rounded-md hover:bg-indigo-700 transition-colors"
        >
          홈으로 이동
        </button>
        
        <button 
          @click="goToProducts" 
          class="w-full bg-white text-indigo-600 border border-indigo-600 py-3 px-4 rounded-md hover:bg-gray-50 transition-colors"
        >
          쇼핑 계속하기
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'OrderCompletePage',
  setup() {
    const router = useRouter();
    
    // 임의의 주문번호 생성 (실제로는 백엔드에서 받아옴)
    const orderNumber = computed(() => {
      const randomNum = Math.floor(Math.random() * 1000000);
      return `ORD-${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}${String(new Date().getDate()).padStart(2, '0')}-${randomNum}`;
    });
    
    const goToHome = () => {
      router.push('/');
    };
    
    const goToProducts = () => {
      router.push('/products');
    };
    
    return {
      orderNumber,
      goToHome,
      goToProducts
    };
  }
});
</script>
