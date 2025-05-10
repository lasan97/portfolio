<template>
  <div class="cart-page min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4">
      <h1 class="text-2xl font-bold mb-6">장바구니</h1>
      
      <!-- 로그인하지 않은 사용자를 위한 안내 메시지 -->
      <div v-if="!isLoggedIn" class="bg-blue-50 rounded-lg shadow p-8 text-center">
        <img src="/logo.svg" class="h-16 w-16 mx-auto " alt="로고"/>
        <h2 class="mt-4 text-xl font-semibold text-gray-800">로그인이 필요합니다</h2>
        <p class="mt-2 text-gray-600">장바구니 기능을 이용하려면 로그인이 필요합니다.</p>
        <div class="mt-6 flex justify-center space-x-4">
          <router-link 
            :to="{ name: 'login', query: { returnUrl: $route.fullPath } }"
            class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            로그인하기
          </router-link>
          <router-link 
            to="/products"
            class="px-6 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
          >
            상품 둘러보기
          </router-link>
        </div>
      </div>
      
      <!-- 로그인한 사용자를 위한 장바구니 내용 -->
      <template v-else>
        <div v-if="loading" class="flex justify-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
        </div>
        
        <div v-else-if="cartIsEmpty" class="bg-white rounded-lg shadow p-8 text-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <p class="mt-4 text-lg text-gray-600">장바구니가 비어 있습니다.</p>
          <button 
            @click="goToProducts"
            class="mt-6 px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
          >
            상품 둘러보기
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
                    <!-- 이미지 처리를 위한 오류 핸들링 추가 -->
                    <div class="w-24 h-24 relative">
                      <img 
                        v-if="!(item as CartItemWithUI).imageError" 
                        :src="item.product.thumbnailImageUrl" 
                        :alt="item.product.name" 
                        class="w-24 h-24 object-cover rounded-md"
                        @error="handleImageError($event, item as CartItemWithUI)"
                        @load="handleImageLoad(item as CartItemWithUI)"
                      />
                      <img 
                        v-else 
                        src="https://via.placeholder.com/150" 
                        :alt="item.product.name" 
                        class="w-24 h-24 object-cover rounded-md"
                      />
                      <!-- 이미지 로딩 스피너 -->
                      <div v-if="(item as CartItemWithUI).imageLoading" class="absolute inset-0 flex items-center justify-center bg-gray-100 bg-opacity-50 rounded-md">
                        <div class="animate-spin rounded-full h-6 w-6 border-2 border-indigo-600"></div>
                      </div>
                    </div>
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
                          @click="updateItemQuantity(item.product.id, Math.max(1, item.quantity - 1))" 
                          class="text-gray-600 hover:text-indigo-600 focus:outline-none"
                          :disabled="isItemUpdating(item.product.id)"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5 10a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1z" clip-rule="evenodd" />
                          </svg>
                        </button>
                        
                        <span class="mx-2 text-gray-700 min-w-[20px] text-center">{{ item.quantity }}</span>
                        
                        <button 
                          @click="updateItemQuantity(item.product.id, item.quantity + 1)" 
                          class="text-gray-600 hover:text-indigo-600 focus:outline-none"
                          :disabled="isItemUpdating(item.product.id)"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd" />
                          </svg>
                        </button>
                      </div>
                      
                      <button 
                        @click="removeItem(item.product.id)" 
                        class="text-red-600 hover:text-red-800 transition-colors"
                        :disabled="isItemUpdating(item.product.id)"
                      >
                        삭제
                      </button>
                    </div>
                  </div>
                </li>
              </ul>
              
              <!-- 장바구니 비우기 버튼 -->
              <div class="p-4 flex justify-end">
                <button 
                  @click="clearCart"
                  class="text-sm text-red-500 hover:text-red-700"
                  :disabled="isClearing"
                >
                  장바구니 비우기
                </button>
              </div>
            </div>
            
            <!-- 배송 정보 입력 폼 (주문 진행 시에만 표시) -->
            <div v-if="orderMode" class="bg-white rounded-lg shadow-md overflow-hidden">
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
                
                <!-- 무료 배송까지 남은 금액 알림 -->
                <div v-if="cartStore.shippingInfo.required" class="text-sm text-indigo-600 border-t border-b border-indigo-100 py-2">
                  {{ formatPrice(FREE_SHIPPING_THRESHOLD - cartStore.subtotal) }} 더 구매 시 무료 배송
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
                <router-link 
                  to="/order"
                  class="w-full bg-indigo-600 text-white py-3 rounded-md font-medium hover:bg-indigo-700 transition-colors block text-center"
                >
                  주문하기
                </router-link>
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
      </template>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@entities/cart';
import { useCartWithAuth } from '@features/cart';
import { useAuthStore } from '@features/auth';
import { ProductCategory } from '@entities/product';
import { ToastService } from '@shared/ui/toast';
import { FREE_SHIPPING_THRESHOLD } from '@entities/cart/model/constants';
import type { CartItem } from '@entities/cart';
import {formatPrice} from "@shared/lib";

interface OrderForm {
  name: string;
  phone: string;
  email: string;
  address: string;
  addressDetail: string;
  postcode: string;
  deliveryRequest: string;
}

// 이미지 처리를 위한 확장된 인터페이스
interface CartItemWithUI extends CartItem {
  imageError?: boolean;
  imageLoading?: boolean;
}

export default defineComponent({
  name: 'CartPage',
  setup() {
    const router = useRouter();
    const cartStore = useCartStore();
    const authStore = useAuthStore();
    const { updateQuantity: apiUpdateQuantity } = useCartWithAuth();
    
    const loading = ref(true);
    const isClearing = ref(false);
    const isSubmitting = ref(false);
    
    // 주문 모드 (주문하기 클릭 시 활성화)
    const orderMode = ref(false);
    
    // 현재 업데이트 중인 아이템 ID 추적
    const updatingItemIds = reactive(new Set<number>());
    
    // 로그인 상태 확인
    const isLoggedIn = computed(() => authStore.isAuthenticated);
    
    // 장바구니가 비어있는지 확인
    const cartIsEmpty = computed(() => {
      return !cartStore.items || cartStore.items.length === 0;
    });
    
    // 주문 폼 데이터
    const orderForm = ref<OrderForm>({
      name: '',
      phone: '',
      email: '',
      address: '',
      addressDetail: '',
      postcode: '',
      deliveryRequest: ''
    });
    
    // 이미지 로딩/에러 처리를 위한 UI 상태 초기화
    const initializeUIState = () => {
      if (cartStore.items && cartStore.items.length > 0) {
        cartStore.items.forEach((item: CartItemWithUI) => {
          // 이미지 상태 추가
          if (!('imageError' in item)) {
            item.imageError = false;
          }
          if (!('imageLoading' in item)) {
            item.imageLoading = true;
          }
        });
      }
    };
    
    const refreshCart = async () => {
      try {
        loading.value = true;
        await cartStore.fetchCartItems();
        initializeUIState(); // UI 상태 초기화
      } catch (error) {
        console.error('장바구니 데이터 로드 중 오류 발생:', error);
        ToastService.error('장바구니 데이터를 불러오는데 실패했습니다.');
      } finally {
        loading.value = false;
      }
    };
    
    onMounted(async () => {
      // 로그인한 상태일 때만 장바구니 데이터 로드
      if (isLoggedIn.value) {
        try {
          loading.value = true;
          await cartStore.fetchCartItems();
          initializeUIState(); // UI 상태 초기화
        } catch (error) {
          console.error('장바구니 데이터 로드 중 오류 발생:', error);
          ToastService.error('장바구니 데이터를 불러오는데 실패했습니다.');
        } finally {
          loading.value = false;
        }
      }
    });
    
    const goToProducts = () => {
      router.push('/products');
    };
    
    // 아이템 수량 업데이트 (UI 즉시 반영 + API 호출)
    const updateItemQuantity = async (productId: number, quantity: number) => {
      // 이미 업데이트 중인 아이템이면 무시
      if (updatingItemIds.has(productId)) return;
      
      try {
        // 해당 아이템 업데이트 상태로 표시
        updatingItemIds.add(productId);
        
        // 즉시 UI에 반영 (낙관적 업데이트)
        const itemToUpdate = cartStore.items.find(item => item.product.id === productId);
        if (itemToUpdate) {
          const oldQuantity = itemToUpdate.quantity;
          itemToUpdate.quantity = quantity;
          
          // API 호출로 서버 상태 업데이트
          try {
            console.log(`장바구니 수량 업데이트 API 호출: 상품ID ${productId}, 수량 ${quantity}`);
            await cartStore.updateQuantity(productId, quantity);
            // API 호출 성공 시 토스트 메시지 표시
            if (oldQuantity < quantity) {
              ToastService.success('상품 수량이 증가되었습니다.');
            } else {
              ToastService.success('상품 수량이 감소되었습니다.');
            }
          } catch (apiError) {
            console.error('API 호출 실패:', apiError);
            // API 호출 실패 시 UI 롤백
            itemToUpdate.quantity = oldQuantity;
            throw apiError;
          }
        }
      } catch (error) {
        console.error('수량 업데이트 실패:', error);
        ToastService.error('수량 변경에 실패했습니다. 다시 시도해주세요.');
        // 오류 발생 시 최신 데이터로 다시 로드
        await refreshCart();
      } finally {
        // 업데이트 완료되면 상태에서 제거
        updatingItemIds.delete(productId);
      }
    };
    
    // 아이템이 현재 업데이트 중인지 확인
    const isItemUpdating = (productId: number): boolean => {
      return updatingItemIds.has(productId);
    };
    
    // 이미지 오류 처리 함수
    const handleImageError = (e: Event, item: CartItemWithUI) => {
      item.imageLoading = false;
      item.imageError = true;
      
      // 이벤트 전파 중지
      e.stopPropagation();
    };
    
    // 이미지 로드 완료 처리 함수
    const handleImageLoad = (item: CartItemWithUI) => {
      item.imageLoading = false;
    };
    
    const removeItem = async (productId: number) => {
      // 이미 업데이트 중인 아이템이면 무시
      if (updatingItemIds.has(productId)) return;
      
      try {
        // 해당 아이템 업데이트 상태로 표시
        updatingItemIds.add(productId);
        
        // UI에서 즉시 제거 (낙관적 업데이트)
        const index = cartStore.items.findIndex(item => item.product.id === productId);
        if (index !== -1) {
          cartStore.items.splice(index, 1);
        }
        
        // API 호출로 서버 상태 업데이트
        await cartStore.removeFromCart(productId);
        ToastService.success('상품이 장바구니에서 제거되었습니다.');
      } catch (error) {
        console.error('상품 제거 실패:', error);
        ToastService.error('상품 제거에 실패했습니다. 다시 시도해주세요.');
        // 오류 발생 시 최신 데이터로 다시 로드
        await refreshCart();
      } finally {
        // 업데이트 완료되면 상태에서 제거
        updatingItemIds.delete(productId);
      }
    };
    
    const clearCart = async () => {
      try {
        isClearing.value = true;
        await cartStore.clearCart();
        ToastService.success('장바구니가 비워졌습니다.');
      } catch (error) {
        console.error('장바구니 비우기 실패:', error);
        ToastService.error('장바구니 비우기에 실패했습니다.');
      } finally {
        isClearing.value = false;
      }
    };
    
    const placeOrder = async () => {
      try {
        isSubmitting.value = true;
        
        // 주문 처리 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        // 주문 완료 메시지
        ToastService.success('주문이 성공적으로 완료되었습니다!');
        
        // 장바구니 비우기
        await cartStore.clearCart();
        
        // 주문 완료 페이지로 이동
        router.push('/order-complete');
      } catch (error) {
        console.error('주문 처리 중 오류 발생:', error);
        ToastService.error('주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
      } finally {
        isSubmitting.value = false;
      }
    };

    return {
      cartStore,
      isLoggedIn,
      cartIsEmpty,
      loading,
      isClearing,
      isSubmitting,
      orderMode,
      orderForm,
      updatingItemIds,
      formatPrice,
      goToProducts,
      updateItemQuantity,
      isItemUpdating,
      removeItem,
      clearCart,
      refreshCart,
      placeOrder,
      handleImageError,
      handleImageLoad,
      FREE_SHIPPING_THRESHOLD,
      getCategoryDescription: ProductCategory.getDescription
    };
  }
});
</script>
