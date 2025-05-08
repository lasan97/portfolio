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
        주문 내역은 마이페이지에서 확인하실 수 있습니다.
      </p>
      
      <div v-if="loading" class="py-4 flex justify-center">
        <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
      </div>
      
      <div v-else class="bg-gray-50 p-4 rounded-md mb-6">
        <p class="text-sm text-gray-600 mb-2">주문번호</p>
        <p class="text-lg font-semibold text-gray-900">{{ order?.id || 'N/A' }}</p>
        
        <div v-if="order" class="mt-4 text-left">
          <p class="text-sm text-gray-600 mb-2">주문 일시</p>
          <p class="text-md text-gray-900 mb-4">{{ formatDate(order.createdAt) }}</p>
          
          <p class="text-sm text-gray-600 mb-2">주문 금액</p>
          <p class="text-md font-semibold text-indigo-600 mb-4">{{ formatPrice(order.totalPrice) }}</p>
          
          <p class="text-sm text-gray-600 mb-2">배송지</p>
          <p class="text-md text-gray-900">
            {{ order.deliveryInfo.name }}<br>
            {{ order.deliveryInfo.phone }}<br>
            {{ order.deliveryInfo.address.address }} 
            {{ order.deliveryInfo.address.detailAddress || '' }}<br>
            {{ order.deliveryInfo.address.postCode }}
          </p>
        </div>
      </div>
      
      <div class="space-y-4">
        <button 
          @click="goToOrderHistory" 
          class="w-full bg-indigo-600 text-white py-3 px-4 rounded-md hover:bg-indigo-700 transition-colors"
        >
          주문 내역 확인
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
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useOrderWithAuth } from '@features/order';
import { Order } from '@entities/order';

export default defineComponent({
  name: 'OrderCompletePage',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const { fetchOrderDetails } = useOrderWithAuth();
    
    const order = ref<Order | null>(null);
    const loading = ref(true);
    
    onMounted(async () => {
      try {
        // URL에서 주문 ID 가져오기
        const orderId = route.query.orderId as string;
        
        if (orderId) {
          // 주문 상세 정보 조회
          const orderDetails = await fetchOrderDetails(orderId);
          if (orderDetails) {
            order.value = orderDetails;
          }
        }
      } catch (error) {
        console.error('주문 정보 조회 실패:', error);
      } finally {
        loading.value = false;
      }
    });
    
    const formatDate = (dateString: string): string => {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    };
    
    const formatPrice = (price: any): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(price);
    };
    
    const goToOrderHistory = () => {
      router.push('/orders');
    };
    
    const goToProducts = () => {
      router.push('/products');
    };
    
    return {
      order,
      loading,
      formatDate,
      formatPrice,
      goToOrderHistory,
      goToProducts
    };
  }
});
</script>
