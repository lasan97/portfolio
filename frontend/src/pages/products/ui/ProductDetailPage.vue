<template>
  <div class="product-detail-container">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
    </div>
    
    <div v-else-if="error" class="bg-red-100 border border-red-200 text-red-700 px-6 py-4 rounded-lg shadow-sm mb-6">
      {{ error }}
    </div>
    
    <template v-else-if="product">
      <!-- 상품 상세 컨텐츠 -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden">
        <div class="md:flex">
          <!-- 상품 이미지 -->
          <div class="md:w-1/2 relative">
            <!-- 이미지 로딩 처리 개선 -->
            <div class="relative" style="min-height: 300px;">
              <img 
                v-if="!imageError" 
                :src="product.thumbnailImageUrl"
                :alt="product.name" 
                class="w-full h-full object-cover object-center"
                style="min-height: 300px;"
                @error="handleImageError"
                @load="imageLoading = false"
              />
              <img 
                v-else 
                src="https://via.placeholder.com/600x400" 
                :alt="product.name" 
                class="w-full h-full object-cover object-center"
                style="min-height: 300px;"
              />
              <!-- 이미지 로딩 스피너 -->
              <div v-if="imageLoading" class="absolute inset-0 flex items-center justify-center bg-gray-100 bg-opacity-50">
                <div class="animate-spin rounded-full h-10 w-10 border-2 border-indigo-600"></div>
              </div>
            </div>
          </div>
          
          <!-- 상품 정보 -->
          <div class="md:w-1/2 p-8">
            <div class="uppercase tracking-wide text-sm text-indigo-600 font-semibold">
              {{ getCategoryDescription(product.category) }}
            </div>
            <h1 class="mt-2 text-3xl font-bold text-gray-900">{{ product.name }}</h1>
            
            <!-- 상품 가격 정보 -->
            <div class="mt-4 space-y-1">
              <!-- 원가 표시 (할인이 있는 경우) -->
              <div v-if="product.originalPrice > product.price" class="text-lg text-gray-500">
                <span class="line-through">{{ formatPrice(product.originalPrice) }}</span>
                <span class="ml-2 text-red-500 font-medium">
                  {{ calculateDiscountRate(product.originalPrice, product.price) }}% 할인
                </span>
              </div>
              
              <!-- 판매가 표시 -->
              <div>
                <span class="text-3xl font-bold text-gray-900">{{ formatPrice(product.price) }}</span>
              </div>
            </div>
            
            <!-- 재고 상태 -->
            <div class="mt-4">
              <span 
                :class="[
                  'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium',
                  product.inStock 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                ]"
              >
                {{ product.inStock ? '재고 있음' : '품절' }}
              </span>
            </div>
            
            <!-- 설명 -->
            <div class="mt-6">
              <h3 class="text-lg font-medium text-gray-900">상품 설명</h3>
              <p class="mt-2 text-gray-600">{{ product.description }}</p>
            </div>
            
            <!-- 구매 버튼 -->
            <div class="mt-8">
              <button 
                class="w-full bg-indigo-600 text-white py-3 px-6 rounded-md font-medium hover:bg-indigo-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                :disabled="!product.inStock"
                @click="addToCart"
              >
                {{ product.inStock ? '장바구니에 담기' : '품절' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 추가 정보 (예: 관련 상품, 리뷰 등) -->
      <div class="mt-12 grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- 배송 정보 카드 -->
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-lg font-medium text-gray-900 mb-4">배송 정보</h3>
          <ul class="space-y-3">
            <li class="flex">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span>무료 배송 (3만원 이상 구매 시)</span>
            </li>
            <li class="flex">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span>평균 배송 기간: 2-3일</span>
            </li>
            <li class="flex">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span>30일 이내 반품 가능</span>
            </li>
          </ul>
        </div>
        
        <!-- 장바구니 요약 카드 -->
        <cart-summary @checkout="goToCheckout" />

      </div>
    </template>
    
    <div v-else class="text-center py-12">
      <p class="text-gray-500 text-lg">상품을 찾을 수 없습니다.</p>
    </div>
    
    <!-- 뒤로가기 버튼 -->
    <div class="mt-8">
      <button 
        @click="goBack" 
        class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        상품 목록으로 돌아가기
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useProductStore, type Product, ProductCategory } from '@entities/product';
import { CartSummary } from '@entities/cart';
import { useCartWithAuth } from '@features/cart';
import {formatPrice} from "@shared/lib";

export default defineComponent({
  name: 'ProductDetail',
  components: {
    CartSummary
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const productStore = useProductStore();
    const { addToCart: addProductToCart } = useCartWithAuth();
    
    const loading = ref(true);
    const error = ref<string | null>(null);
    const product = ref<Product | null>(null);
    
    // 이미지 로딩 상태 관리
    const imageLoading = ref(true);
    const imageError = ref(false);
    
    const productId = computed(() => {
      const id = route.params.id;
      return typeof id === 'string' ? parseInt(id, 10) : -1;
    });
    
    onMounted(async () => {
      try {
        loading.value = true;
        error.value = null;
        
        // 상품 ID로 상세 정보 조회 (API 호출)
        const foundProduct = await productStore.fetchProductById(productId.value);
        
        if (foundProduct) {
          product.value = foundProduct;
        } else {
          error.value = '상품을 찾을 수 없습니다.';
        }
      } catch (err: any) {
        error.value = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
      } finally {
        loading.value = false;
      }
    });
    
    // 이미지 오류 처리 함수
    const handleImageError = (e: Event) => {
      imageLoading.value = false;
      imageError.value = true;
      e.stopPropagation();
    };
    
    const goBack = () => {
      router.push('/products');
    };
    
    const addToCart = async () => {
      if (product.value) {
        try {
          // useCartWithAuth를 사용하여 인증 체크와 토스트 메시지 표시
          await addProductToCart(product.value, 1);
          // 토스트 메시지는 useCartWithAuth 내부에서 처리하므로 여기서는 별도 처리 필요 없음
        } catch (error) {
          console.error('장바구니 추가 실패:', error);
        }
      }
    };
    
    const goToCheckout = () => {
      router.push('/cart');
    };
    
    const calculateDiscountRate = (originalPrice: number, currentPrice: number): number => {
      const discountRate = ((originalPrice - currentPrice) / originalPrice) * 100;
      return Math.round(discountRate);
    };

    return {
      product,
      loading,
      error,
      imageLoading,
      imageError,
      handleImageError,
      formatPrice,
      goBack,
      addToCart,
      goToCheckout,
      calculateDiscountRate,
      getCategoryDescription: ProductCategory.getDescription
    };
  }
});
</script>