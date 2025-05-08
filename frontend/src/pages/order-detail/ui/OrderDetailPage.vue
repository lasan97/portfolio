<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 로딩 표시 -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
    </div>
    
    <!-- 주문 정보가 없는 경우 -->
    <div v-else-if="!order" class="bg-white rounded-lg shadow p-8 text-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
      <p class="mt-4 text-lg text-gray-600">주문 정보를 찾을 수 없습니다.</p>
      <div class="mt-6 flex justify-center space-x-4">
        <button 
          @click="goBack"
          class="px-6 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition-colors"
        >
          이전으로
        </button>
        <button 
          @click="goToOrderHistory"
          class="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
        >
          주문 내역으로
        </button>
      </div>
    </div>
    
    <!-- 주문 상세 정보 -->
    <div v-else>
      <!-- 상단 헤더 영역 -->
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6">
        <div>
          <div class="flex items-center">
            <button 
              @click="goBack"
              class="mr-2 text-gray-600 hover:text-indigo-600 focus:outline-none"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            <h1 class="text-2xl font-bold">주문 상세 내역</h1>
          </div>
          <p class="text-gray-600 mt-1">주문번호: {{ order.id }}</p>
          <p class="text-gray-600">주문일자: {{ formatDate(order.createdAt) }}</p>
        </div>
        <div class="mt-4 md:mt-0">
          <span 
            class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
            :class="getStatusClass(order.orderStatus)"
          >
            {{ getStatusText(order.orderStatus) }}
          </span>
        </div>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 주문 상품 및 금액 정보 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 주문 상품 목록 -->
          <div class="bg-white rounded-lg shadow-md overflow-hidden">
            <div class="p-6 border-b border-gray-200">
              <h2 class="text-xl font-medium">주문 상품 ({{ order.orderItems.length }}개)</h2>
            </div>
            
            <ul class="divide-y divide-gray-200">
              <li v-for="(item, index) in order.orderItems" :key="index" class="p-6 flex flex-col sm:flex-row">
                <div class="flex-shrink-0 mb-4 sm:mb-0 sm:mr-4">
                  <div class="w-20 h-20 relative">
                    <img 
                      v-if="item.product.thumbnailImageUrl" 
                      :src="item.product.thumbnailImageUrl" 
                      :alt="item.product.name" 
                      class="w-20 h-20 object-cover rounded-md"
                    />
                    <div v-else class="w-20 h-20 flex items-center justify-center bg-gray-100 rounded-md">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                  </div>
                </div>
                
                <div class="flex-grow">
                  <div class="flex flex-col sm:flex-row sm:justify-between">
                    <h3 class="text-base font-medium text-gray-900">{{ item.product.name }}</h3>
                    
                    <div class="mt-2 sm:mt-0 sm:text-right">
                      <p class="text-base font-medium text-gray-900">{{ formatPrice(item.product.price) }}</p>
                      <p v-if="isOriginalPriceDifferent(item.product)" class="text-sm text-gray-500 line-through">
                        {{ formatPrice(item.product.originalPrice) }}
                      </p>
                      <p class="text-sm text-gray-600 mt-1">수량: {{ item.quantity }}개</p>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
          
          <!-- 배송 정보 -->
          <div class="bg-white rounded-lg shadow-md overflow-hidden">
            <div class="p-6 border-b border-gray-200">
              <h2 class="text-xl font-medium">배송 정보</h2>
            </div>
            
            <div class="p-6">
              <dl class="divide-y divide-gray-200">
                <div class="py-3 flex justify-between">
                  <dt class="text-sm text-gray-600">수령인</dt>
                  <dd class="text-sm text-gray-900">{{ order.deliveryInfo.name }}</dd>
                </div>
                <div class="py-3 flex justify-between">
                  <dt class="text-sm text-gray-600">연락처</dt>
                  <dd class="text-sm text-gray-900">{{ order.deliveryInfo.phone }}</dd>
                </div>
                <div class="py-3 flex justify-between">
                  <dt class="text-sm text-gray-600">배송지</dt>
                  <dd class="text-sm text-gray-900">
                    ({{ order.deliveryInfo.address.postCode }})
                    {{ order.deliveryInfo.address.address }}
                    {{ order.deliveryInfo.address.detailAddress }}
                  </dd>
                </div>
                <div v-if="order.deliveryInfo.deliveryRequest" class="py-3 flex justify-between">
                  <dt class="text-sm text-gray-600">요청사항</dt>
                  <dd class="text-sm text-gray-900">{{ order.deliveryInfo.deliveryRequest }}</dd>
                </div>
              </dl>
            </div>
          </div>
        </div>
        
        <!-- 결제 정보 요약 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg shadow-md p-6 sticky top-6">
            <h2 class="text-xl font-medium mb-6">결제 정보</h2>
            
            <div class="space-y-4">
              <!-- 상품 금액 -->
              <div class="flex justify-between">
                <span class="text-gray-600">상품 금액</span>
                <span>{{ getTotalProductPrice() }}</span>
              </div>
              
              <!-- 배송비 -->
              <div class="flex justify-between">
                <span class="text-gray-600">배송비</span>
                <span>{{ getShippingFee() }}</span>
              </div>
              
              <!-- 총 결제금액 -->
              <div class="border-t border-gray-200 pt-4 flex justify-between font-semibold">
                <span>총 결제 금액</span>
                <span class="text-indigo-600 text-xl">{{ formatPrice(order.totalPrice) }}</span>
              </div>
            </div>
            
            <div class="mt-8 space-y-4">
              <!-- 주문 취소 버튼 (상태가 CREATED인 경우에만 표시) -->
              <button 
                v-if="order.orderStatus === OrderStatus.CREATED"
                @click="cancelOrder"
                class="w-full border border-red-600 text-red-600 py-3 rounded-md font-medium hover:bg-red-50 transition-colors"
              >
                주문 취소
              </button>
              
              <!-- 주문 내역으로 버튼 -->
              <button 
                @click="goToOrderHistory"
                class="w-full bg-indigo-600 text-white py-3 rounded-md font-medium hover:bg-indigo-700 transition-colors"
              >
                주문 내역으로
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOrderWithAuth } from '@features/order';
import { Order, OrderStatus } from '@entities/order';
import { ToastService } from '@shared/ui/toast';

export default defineComponent({
  name: 'OrderDetailPage',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { fetchOrderDetails, loading } = useOrderWithAuth();
    
    // 현재 주문 정보
    const order = ref<Order | null>(null);
    
    // 컴포넌트 마운트 시 주문 상세 정보 조회
    onMounted(async () => {
      try {
        const orderId = route.params.id as string;
        if (orderId) {
          const orderData = await fetchOrderDetails(orderId);
          order.value = orderData;
        }
      } catch (error) {
        console.error('주문 상세 정보 조회 실패:', error);
        ToastService.error('주문 정보를 불러오는데 실패했습니다.');
      }
    });
    
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
      // price가 숫자인 경우 바로 포맷팅 (개선된 API 응답 구조)
      if (typeof price === 'number') {
        return new Intl.NumberFormat('ko-KR', {
          style: 'currency',
          currency: 'KRW',
          maximumFractionDigits: 0
        }).format(price);
      }
      
      // price가 객체인 경우 price.amount 사용 (기존 API 응답 구조)
      if (price && typeof price === 'object' && 'amount' in price) {
        return new Intl.NumberFormat('ko-KR', {
          style: 'currency',
          currency: 'KRW',
          maximumFractionDigits: 0
        }).format(price.amount);
      }
      
      // NaN 값인 경우
      if (price === 'NaN' || price === 'WNaN' || (typeof price === 'number' && isNaN(price))) {
        console.error('가격 데이터 오류: NaN 값이 감지되었습니다', price);
        return '₩0';
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
    
    // 총 상품 가격 계산
    const getTotalProductPrice = () => {
      if (!order.value) return '0원';
      
      try {
        const totalProductPrice = order.value.orderItems.reduce((sum, item) => {
          // price가 객체인지 숫자인지 확인
          let itemPrice = 0;
          
          if (typeof item.product.price === 'object' && item.product.price !== null && 'amount' in item.product.price) {
            itemPrice = item.product.price.amount;
          } else if (typeof item.product.price === 'number' && !isNaN(item.product.price)) {
            itemPrice = item.product.price;
          }
          
          return sum + (itemPrice * item.quantity);
        }, 0);
        
        return new Intl.NumberFormat('ko-KR', {
          style: 'currency',
          currency: 'KRW',
          maximumFractionDigits: 0
        }).format(totalProductPrice);
      } catch (error) {
        console.error('총 상품 가격 계산 오류:', error);
        return '₩0';
      }
    };
    
    // 배송비 계산 (실제 비즈니스 로직에 맞게 수정 필요)
    const getShippingFee = () => {
      if (!order.value) return '0원';
      
      try {
        // 주문 총액과 상품 금액의 차이를 배송비로 계산
        const totalProductPrice = order.value.orderItems.reduce((sum, item) => {
          // price가 객체인지 숫자인지 확인
          let itemPrice = 0;
          
          if (typeof item.product.price === 'object' && item.product.price !== null && 'amount' in item.product.price) {
            itemPrice = item.product.price.amount;
          } else if (typeof item.product.price === 'number' && !isNaN(item.product.price)) {
            itemPrice = item.product.price;
          }
          
          return sum + (itemPrice * item.quantity);
        }, 0);
        
        // totalPrice가 객체인지 숫자인지 확인
        let orderTotalPrice = 0;
        
        if (typeof order.value.totalPrice === 'object' && order.value.totalPrice !== null && 'amount' in order.value.totalPrice) {
          orderTotalPrice = order.value.totalPrice.amount;
        } else if (typeof order.value.totalPrice === 'number' && !isNaN(order.value.totalPrice)) {
          orderTotalPrice = order.value.totalPrice;
        }
        
        const shippingFee = Math.max(0, orderTotalPrice - totalProductPrice);
        
        return new Intl.NumberFormat('ko-KR', {
          style: 'currency',
          currency: 'KRW',
          maximumFractionDigits: 0
        }).format(shippingFee);
      } catch (error) {
        console.error('배송비 계산 오류:', error);
        return '₩0';
      }
    };
    
    // 이전 페이지로 이동
    const goBack = () => {
      router.go(-1);
    };
    
    // 주문 내역 페이지로 이동
    const goToOrderHistory = () => {
      router.push('/orders');
    };
    
    // 주문 취소 (실제 구현 필요)
    const cancelOrder = async () => {
      try {
        if (!order.value) return;
        
        // 실제 주문 취소 API 호출이 필요합니다
        // const result = await orderRepository.cancelOrder(order.value.id);
        
        // 임시로 성공 메시지만 표시
        ToastService.success('주문이 취소되었습니다.');
        
        // 주문 내역 페이지로 이동
        router.push('/orders');
      } catch (error) {
        console.error('주문 취소 실패:', error);
        ToastService.error('주문 취소에 실패했습니다.');
      }
    };
    
    // 원래 가격과 판매 가격이 다른지 확인하는 함수
    const isOriginalPriceDifferent = (product: any) => {
      // 둘 다 객체인 경우
      if (typeof product.originalPrice === 'object' && typeof product.price === 'object' &&
          product.originalPrice !== null && product.price !== null &&
          'amount' in product.originalPrice && 'amount' in product.price) {
        return product.originalPrice.amount !== product.price.amount;
      }
      
      // 둘 다 숫자인 경우
      if (typeof product.originalPrice === 'number' && typeof product.price === 'number') {
        return product.originalPrice !== product.price;
      }
      
      // 혼합된 경우
      if (typeof product.originalPrice === 'object' && product.originalPrice !== null && 'amount' in product.originalPrice &&
          typeof product.price === 'number') {
        return product.originalPrice.amount !== product.price;
      }
      
      if (typeof product.price === 'object' && product.price !== null && 'amount' in product.price &&
          typeof product.originalPrice === 'number') {
        return product.originalPrice !== product.price.amount;
      }
      
      // 기본값: 다르지 않음
      return false;
    };
    
    return {
      order,
      loading,
      formatDate,
      formatPrice,
      getStatusClass,
      getStatusText,
      getTotalProductPrice,
      getShippingFee,
      goBack,
      goToOrderHistory,
      cancelOrder,
      OrderStatus,
      isOriginalPriceDifferent
    };
  }
});
</script>
