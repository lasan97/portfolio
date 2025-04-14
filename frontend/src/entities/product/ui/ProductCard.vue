<template>
  <div 
    class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300 cursor-pointer"
    @click="$emit('click')"
  >
    <div class="relative">
      <img :src="product.imageUrl" :alt="product.name" class="w-full h-48 object-cover">
      <span 
        v-if="!product.inStock" 
        class="absolute top-2 right-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded"
      >
        품절
      </span>
    </div>
    <div class="p-4">
      <h3 class="text-lg font-semibold mb-1 text-gray-800">{{ product.name }}</h3>
      <p class="text-sm text-gray-600 mb-3 line-clamp-2">{{ product.description }}</p>
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
          <span class="text-xs px-2 py-1 bg-gray-100 rounded-full text-gray-600">{{ product.category }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Product } from '../model/types';

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  emits: ['click'],
  setup() {
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };
    
    const calculateDiscountRate = (originalPrice: number, currentPrice: number): number => {
      const discountRate = ((originalPrice - currentPrice) / originalPrice) * 100;
      return Math.round(discountRate);
    };

    return {
      formatPrice,
      calculateDiscountRate
    };
  }
});
</script>
