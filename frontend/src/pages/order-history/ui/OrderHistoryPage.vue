<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">주문 내역</h1>
    
    <!-- 로딩 중 표시 -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
    </div>
    
    <!-- 주문 내역이 없는 경우 -->
    <div v-else-if="!hasOrders" class="bg-white rounded-lg shadow p-8 text-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
      <p class="mt-4 text-lg text-gray-600">주문 내역이 없습니다.</p>
      <button 
        @click="goToProducts"
        class="mt-6 px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
      >
        상품 둘러보기
      </button>
    </div>
    
    <!-- 주문 내역 목록 -->
    <div v-else class="space-y-6">
      <div v-for="order in sortedOrders" :key="order.id" class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="p-6 border-b border-gray-200 flex flex-col sm:flex-row justify-between items-start sm:items-center">
          <div>
            <h2 class="text-lg font-semibold text-gray-800">주문번호: {{ order.id }}</h2>
            <p class="text-sm text-gray-600 mt-1">{{ formatDate(order.createdAt) }}</p>
          </div>
          <div class="mt-2 sm:mt-0">
            <span 
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              :class="getStatusClass(order.orderStatus)"
            >
              {{ getStatusText(order.orderStatus) }}
            </span>
          </div>
        </div>
        
        <div class="p-6">
          <div class="space-y-4">
            <!-- 주문 상품 목록 (최대 2개만 표시) -->
            <div v-for="(item, index) in order.orderItems.slice(0, 2)" :key="index" class="flex items-center py-2">
              <div class="flex-shrink-0 w-12 h-12 bg-gray-100 rounded overflow-hidden">
                <img 
                  v-if="item.product.thumbnailImageUrl" 
                  :src="item.product.thumbnailImageUrl" 
                  :alt="item.product.name"
                  class="w-full h-full object-cover"
                />
                <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
              <div class="ml-4 flex-1">
                <h4 class="text-sm font-medium text-gray-800">{{ item.product.name }}</h4>
                <div class="mt-1 flex justify-between">
                  <span class="text-sm text-gray-500">{{ formatPrice(item.product.price) }}</span>
                  <span class="text-sm text-gray-600">{{ item.quantity }}개</span>
                </div>
              </div>
            </div>
            
            <!-- 추가 상품이 있는 경우 표시 -->
            <div v-if="order.orderItems.length > 2" class="text-sm text-gray-500 pt-2">
              외 {{ order.orderItems.length - 2 }}개 상품
            </div>
            
            <!-- 합계 금액 -->
            <div class="border-t border-gray-200 pt-4 flex justify-between items-center">
              <span class="text-sm text-gray-600">총 결제 금액</span>
              <span class="text-lg font-semibold text-indigo-600">{{ formatPrice(order.totalPrice) }}</span>
            </div>
          </div>
          
          <!-- 상세 보기 버튼 -->
          <div class="mt-6 text-right">
            <router-link 
              :to="`/order/${order.id}`" 
              class="text-indigo-600 hover:text-indigo-800 transition-colors text-sm font-medium"
            >
              상세 보기 &rarr;
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onActivated, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useOrderWithAuth } from '@features/order';
import { OrderStatus } from '@entities/order';

export default defineComponent({
  name: 'OrderHistoryPage',
  setup() {
    const router = useRouter();
    const { fetchOrders, sortedOrders, loading, hasOrders } = useOrderWithAuth();
    
    // 데이터 가져오는 함수
    const loadOrders = async () => {
      await fetchOrders();
    };
    
    // 컴포넌트 마운트 시 주문 내역 조회
    onMounted(loadOrders);
    
    // keep-alive를 사용하는 경우 활성화될 때마다 데이터 새로고침
    onActivated(loadOrders);
    
    // 날짜 포맷팅
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
    
    // 가격 포맷팅
    const formatPrice = (price: any): string => {
      // price가 숫자인 경우 바로 포맷팅
      if (typeof price === 'number') {
        return new Intl.NumberFormat('ko-KR', {
          style: 'currency',
          currency: 'KRW',
          maximumFractionDigits: 0
        }).format(price);
      }
      
      // price가 객체인 경우 price.amount 사용 (API 응답 구조 호환성 유지)
      if (price && typeof price === 'object' && 'amount' in price) {
        return new Intl.NumberFormat('ko-KR', {
          style: 'currency',
          currency: 'KRW',
          maximumFractionDigits: 0
        }).format(price.amount);
      }
      
      // 오류 방지를 위한 기본값
      return '₩0';
    };
    
    // 주문 상태에 따른 스타일 클래스
    const getStatusClass = (status: OrderStatus): string => {
      switch (status) {
        case OrderStatus.CREATED:
          return 'bg-blue-100 text-blue-800';
        case OrderStatus.PAYMENT_CONFIRMED:
          return 'bg-green-100 text-green-800';
        case OrderStatus.SHIPPING_READY:
          return 'bg-yellow-100 text-yellow-800';
        case OrderStatus.SHIPPING:
          return 'bg-purple-100 text-purple-800';
        case OrderStatus.DELIVERED:
          return 'bg-green-100 text-green-800';
        case OrderStatus.CANCELED:
          return 'bg-red-100 text-red-800';
        default:
          return 'bg-gray-100 text-gray-800';
      }
    };
    
    // 주문 상태 텍스트
    const getStatusText = (status: OrderStatus): string => {
      switch (status) {
        case OrderStatus.CREATED:
          return '주문 완료';
        case OrderStatus.PAYMENT_CONFIRMED:
          return '결제 확인';
        case OrderStatus.SHIPPING_READY:
          return '배송 준비중';
        case OrderStatus.SHIPPING:
          return '배송중';
        case OrderStatus.DELIVERED:
          return '배송 완료';
        case OrderStatus.CANCELED:
          return '주문 취소';
        default:
          return '알 수 없음';
      }
    };
    
    // 상품 페이지로 이동
    const goToProducts = () => {
      router.push('/products');
    };
    
    return {
      sortedOrders,
      loading,
      hasOrders,
      formatDate,
      formatPrice,
      getStatusClass,
      getStatusText,
      goToProducts
    };
  }
});
</script>
