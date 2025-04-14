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
          <div class="md:w-1/2">
            <img 
              :src="product.imageUrl" 
              :alt="product.name" 
              class="w-full h-full object-cover object-center"
              style="min-height: 300px;"
            />
          </div>
          
          <!-- 상품 정보 -->
          <div class="md:w-1/2 p-8">
            <div class="uppercase tracking-wide text-sm text-indigo-600 font-semibold">
              {{ product.category }}
            </div>
            <h1 class="mt-2 text-3xl font-bold text-gray-900">{{ product.name }}</h1>
            
            <!-- 상품 평점 -->
            <div class="mt-4 flex items-center">
              <div class="flex items-center">
                <span v-for="i in 5" :key="i" class="text-yellow-400">
                  <svg v-if="i <= Math.round(product.rating)" class="w-5 h-5 fill-current" viewBox="0 0 24 24">
                    <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"></path>
                  </svg>
                  <svg v-else class="w-5 h-5 fill-current text-gray-300" viewBox="0 0 24 24">
                    <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"></path>
                  </svg>
                </span>
              </div>
              <span class="ml-2 text-gray-600">{{ product.rating }}/5</span>
            </div>
            
            <!-- 상품 가격 -->
            <div class="mt-4">
              <span class="text-3xl font-bold text-gray-900">{{ formatPrice(product.price) }}</span>
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
        
        <!-- 구매 혜택 카드 -->
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-lg font-medium text-gray-900 mb-4">구매 혜택</h3>
          <ul class="space-y-3">
            <li class="flex">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>구매 금액의 2% 적립</span>
            </li>
            <li class="flex">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>첫 구매 시 5,000원 할인</span>
            </li>
            <li class="flex">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>리뷰 작성 시 500원 적립</span>
            </li>
          </ul>
        </div>
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
import { useProductStore, type Product } from '@entities/product';

export default defineComponent({
  name: 'ProductDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const productStore = useProductStore();
    
    const loading = ref(true);
    const error = ref<string | null>(null);
    const product = ref<Product | null>(null);
    
    const productId = computed(() => {
      const id = route.params.id;
      return typeof id === 'string' ? parseInt(id, 10) : -1;
    });
    
    onMounted(async () => {
      try {
        loading.value = true;
        error.value = null;
        
        // 상품 목록이 비어있으면 먼저 로드
        if (productStore.products.length === 0) {
          await productStore.fetchProducts();
        }
        
        // 상품 ID로 상품 조회
        const foundProduct = productStore.getProductById(productId.value);
        
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
    
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };
    
    const goBack = () => {
      router.push('/products');
    };
    
    const addToCart = () => {
      // 실제로는 장바구니 기능 구현
      alert(`장바구니에 ${product.value?.name}이(가) 추가되었습니다.`);
    };
    
    return {
      product,
      loading,
      error,
      formatPrice,
      goBack,
      addToCart
    };
  }
});
</script>
