<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">주문하기</h1>
      
      <div v-if="cartStore.isEmpty" class="bg-white rounded-lg shadow-md p-8 text-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <h2 class="text-xl font-medium text-gray-900 mb-2">장바구니가 비어있습니다</h2>
        <p class="text-gray-600 mb-6">상품을 장바구니에 담고 이용해주세요.</p>
        <button 
          @click="goToProducts" 
          class="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
        >
          상품 목록으로 이동
        </button>
      </div>
      
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 주문 상품 목록 -->
        <div class="lg:col-span-2">
          <div class="bg-white rounded-lg shadow-md overflow-hidden mb-6">
            <div class="p-6 border-b border-gray-200">
              <h2 class="text-xl font-medium">주문 상품 ({{ cartStore.totalItems }}개)</h2>
            </div>
            
            <ul class="divide-y divide-gray-200">
              <li v-for="item in cartStore.items" :key="item.product.id" class="p-6 flex flex-col sm:flex-row sm:items-center">
                <div class="flex-shrink-0 mb-4 sm:mb-0 sm:mr-4">
                  <img :src="item.product.imageUrl" :alt="item.product.name" class="w-24 h-24 object-cover rounded-md">
                </div>
                
                <div class="flex-grow">
                  <div class="flex flex-col sm:flex-row sm:justify-between">
                    <div>
                      <h3 class="text-lg font-medium text-gray-900">{{ item.product.name }}</h3>
                      <p class="text-sm text-gray-500">{{ getCategoryDescription(item.product.category) }}</p>
                    </div>
                    
                    <div class="mt-2 sm:mt-0 text-right">
                      <p class="text-lg font-medium text-gray-900">{{ formatPrice(item.product.price) }}</p>
                      <p class="text-sm text-gray-500">수량: {{ item.quantity }}개</p>
                    </div>
                  </div>
                  
                  <div class="mt-4 flex justify-between items-center">
                    <div class="flex items-center">
                      <button 
                        @click="updateQuantity(item.product.id, Math.max(1, item.quantity - 1))" 
                        class="text-gray-600 hover:text-indigo-600 focus:outline-none"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M5 10a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1z" clip-rule="evenodd" />
                        </svg>
                      </button>
                      
                      <span class="mx-2 text-gray-700">{{ item.quantity }}</span>
                      
                      <button 
                        @click="updateQuantity(item.product.id, item.quantity + 1)" 
                        class="text-gray-600 hover:text-indigo-600 focus:outline-none"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd" />
                        </svg>
                      </button>
                    </div>
                    
                    <button 
                      @click="removeItem(item.product.id)" 
                      class="text-red-600 hover:text-red-800 transition-colors"
                    >
                      삭제
                    </button>
                  </div>
                </div>
              </li>
            </ul>
          </div>
          
          <!-- 배송 정보 입력 폼 -->
          <div class="bg-white rounded-lg shadow-md overflow-hidden">
            <div class="p-6 border-b border-gray-200">
              <h2 class="text-xl font-medium">배송 정보</h2>
            </div>
            
            <div class="p-6">
              <form @submit.prevent="placeOrder" class="space-y-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label for="name" class="block text-sm font-medium text-gray-700 mb-1">이름</label>
                    <input 
                      v-model="orderForm.name" 
                      type="text" 
                      id="name" 
                      required
                      class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                  </div>
                  
                  <div>
                    <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">전화번호</label>
                    <input 
                      v-model="orderForm.phone" 
                      type="tel" 
                      id="phone" 
                      required
                      class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                  </div>
                </div>
                
                <div>
                  <label for="email" class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
                  <input 
                    v-model="orderForm.email" 
                    type="email" 
                    id="email" 
                    required
                    class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                </div>
                
                <div>
                  <label for="address" class="block text-sm font-medium text-gray-700 mb-1">주소</label>
                  <input 
                    v-model="orderForm.address" 
                    type="text" 
                    id="address" 
                    required
                    class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                </div>
                
                <div>
                  <label for="addressDetail" class="block text-sm font-medium text-gray-700 mb-1">상세 주소</label>
                  <input 
                    v-model="orderForm.addressDetail" 
                    type="text" 
                    id="addressDetail" 
                    class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                </div>
                
                <div>
                  <label for="postcode" class="block text-sm font-medium text-gray-700 mb-1">우편번호</label>
                  <input 
                    v-model="orderForm.postcode" 
                    type="text" 
                    id="postcode" 
                    required
                    class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                </div>
                
                <div>
                  <label for="deliveryRequest" class="block text-sm font-medium text-gray-700 mb-1">배송 요청사항</label>
                  <textarea 
                    v-model="orderForm.deliveryRequest" 
                    id="deliveryRequest" 
                    rows="3"
                    class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  ></textarea>
                </div>
              </form>
            </div>
          </div>
        </div>
        
        <!-- 주문 요약 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg shadow-md p-6 sticky top-6">
            <h2 class="text-xl font-medium mb-6">주문 요약</h2>
            
            <div class="space-y-4">
              <!-- 원가 총액 -->
              <div class="flex justify-between">
                <span class="text-gray-600">상품 원가</span>
                <span class="line-through text-gray-500">{{ formatPrice(cartStore.originalSubtotal) }}</span>
              </div>
              
              <!-- 할인 금액 -->
              <div class="flex justify-between">
                <span class="text-gray-600">할인 금액</span>
                <span class="text-red-600">- {{ formatPrice(cartStore.totalDiscount) }}</span>
              </div>
              
              <!-- 상품 금액 (할인 적용) -->
              <div class="flex justify-between">
                <span class="text-gray-600">상품 금액</span>
                <span>{{ formatPrice(cartStore.subtotal) }}</span>
              </div>
              
              <!-- 배송비 -->
              <div class="flex justify-between">
                <span class="text-gray-600">배송비</span>
                <span v-if="cartStore.shippingInfo.required">{{ formatPrice(cartStore.shippingInfo.cost) }}</span>
                <span v-else class="text-green-600">무료</span>
              </div>
              
              <!-- 총 결제금액 -->
              <div class="border-t border-gray-200 pt-4 flex justify-between font-semibold">
                <span>총 결제 금액</span>
                <span class="text-indigo-600 text-xl">{{ formatPrice(cartStore.total) }}</span>
              </div>
              
              <!-- 총 할인율 표시 -->
              <div v-if="cartStore.discountRate > 0" class="text-right">
                <span class="inline-block bg-red-100 text-red-800 text-xs px-2 py-1 rounded-full font-medium">
                  총 {{ cartStore.discountRate }}% 할인 적용
                </span>
              </div>
            </div>
            
            <div class="mt-8">
              <button 
                @click="placeOrder" 
                class="w-full bg-indigo-600 text-white py-3 rounded-md font-medium hover:bg-indigo-700 transition-colors"
                :disabled="isSubmitting"
              >
                <span v-if="isSubmitting">
                  <svg class="animate-spin inline-block h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  처리 중...
                </span>
                <span v-else>결제하기</span>
              </button>
            </div>
            
            <div class="mt-6 text-center">
              <button 
                @click="goToProducts" 
                class="text-indigo-600 hover:text-indigo-800 transition-colors"
              >
                쇼핑 계속하기
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@entities/cart';
import { ProductCategory } from '@entities/product';

interface OrderForm {
  name: string;
  phone: string;
  email: string;
  address: string;
  addressDetail: string;
  postcode: string;
  deliveryRequest: string;
}

export default defineComponent({
  name: 'CheckoutPage',
  setup() {
    const router = useRouter();
    const cartStore = useCartStore();
    
    const orderForm = ref<OrderForm>({
      name: '',
      phone: '',
      email: '',
      address: '',
      addressDetail: '',
      postcode: '',
      deliveryRequest: ''
    });

    const isSubmitting = ref(false);
    
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };
    
    const goToProducts = () => {
      router.push('/products');
    };
    
    const updateQuantity = (productId: number, quantity: number) => {
      cartStore.updateQuantity(productId, quantity);
    };
    
    const removeItem = (productId: number) => {
      cartStore.removeFromCart(productId);
    };
    
    const placeOrder = async () => {
      // 실제 구현에서는 여기서 API 호출을 통해 주문 처리
      try {
        isSubmitting.value = true;
        
        // 주문 처리 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        alert('주문이 성공적으로 완료되었습니다!');
        
        // 장바구니 비우기
        cartStore.clearCart();
        
        // 주문 완료 페이지로 이동 (실제로는 구현 필요)
        router.push('/order-complete');
      } catch (error) {
        alert('주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
        console.error('Order processing error:', error);
      } finally {
        isSubmitting.value = false;
      }
    };

    const getCategoryDescription = (category: ProductCategory): string => {
      return ProductCategory.getDescription(category);
    };

    return {
      cartStore,
      orderForm,
      isSubmitting,
      formatPrice,
      goToProducts,
      updateQuantity,
      removeItem,
      placeOrder,
      getCategoryDescription
    };
  }
});
</script>
