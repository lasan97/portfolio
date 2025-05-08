<template>
  <div class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
    <div class="relative" @click="$emit('click')">
      <!-- 이미지 로딩 처리 개선 -->
      <div class="w-full h-48 relative">
        <img 
          v-if="!imageError" 
          :src="product.thumbnailImageUrl" 
          :alt="product.name" 
          class="w-full h-48 object-cover cursor-pointer"
          @error="handleImageError"
          @load="imageLoading = false"
        >
        <img 
          v-else 
          src="https://via.placeholder.com/300x200" 
          :alt="product.name" 
          class="w-full h-48 object-cover cursor-pointer"
        >
        <!-- 이미지 로딩 스피너 -->
        <div v-if="imageLoading" class="absolute inset-0 flex items-center justify-center bg-gray-100 bg-opacity-50">
          <div class="animate-spin rounded-full h-8 w-8 border-2 border-indigo-600"></div>
        </div>
      </div>
      <span 
        v-if="!product.inStock" 
        class="absolute top-2 right-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded"
      >
        품절
      </span>
    </div>
    <div class="p-4">
      <h3 class="text-lg font-semibold mb-1 text-gray-800 cursor-pointer" @click="$emit('click')">{{ product.name }}</h3>
      <p class="text-sm text-gray-600 mb-3 line-clamp-2 cursor-pointer" @click="$emit('click')">{{ product.description }}</p>
      <div class="space-y-1">
        <!-- 원가 표시 (취소선) -->
        <div v-if="product.originalPrice > product.price" class="text-sm text-gray-500">
          <span class="line-through">{{ formatPrice(product.originalPrice) }}</span>
          <span class="ml-1 text-xs text-red-500">
            {{ calculateDiscountRate(product.originalPrice, product.price) }}% 할인
          </span>
        </div>
        <!-- 판매가 표시 -->
        <div class="flex justify-between items-center">
          <span class="text-lg font-bold text-gray-900">{{ formatPrice(product.price) }}</span>
          <span class="text-xs px-2 py-1 bg-gray-100 rounded-full text-gray-600">{{ getCategoryDescription(product.category) }}</span>
        </div>
      </div>
      <div class="mt-4">
        <button 
          @click.stop="addToCart" 
          class="w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-md transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
          :disabled="!product.inStock"
        >
          {{ product.inStock ? '장바구니에 담기' : '품절' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { Product, ProductCategory } from '@entities/product';
import { useCartWithAuth } from '@features/cart';
import {formatPrice} from "@shared/lib";

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  emits: ['click'],
  setup(props) {
    const { addToCart: addProductToCart } = useCartWithAuth();
    
    // 이미지 로딩 상태 관리
    const imageLoading = ref(true);
    const imageError = ref(false);

    const calculateDiscountRate = (originalPrice: number, currentPrice: number): number => {
      const discountRate = ((originalPrice - currentPrice) / originalPrice) * 100;
      return Math.round(discountRate);
    };
    
    // 이미지 오류 처리 함수
    const handleImageError = (e: Event) => {
      imageLoading.value = false;
      imageError.value = true;
      e.stopPropagation();
    };

    const addToCart = async () => {
      if (props.product && props.product.inStock) {
        try {
          // useCartWithAuth를 사용하여 인증 체크와 토스트 메시지 표시
          await addProductToCart(props.product, 1);
          // 토스트 메시지는 useCartWithAuth 내부에서 처리하므로 여기서는 별도 처리 필요 없음
        } catch (error) {
          console.error('장바구니 추가 실패:', error);
        }
      }
    };

    return {
      formatPrice,
      calculateDiscountRate,
      getCategoryDescription: ProductCategory.getDescription,
      addToCart,
      imageLoading,
      imageError,
      handleImageError
    };
  }
});
</script>