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
      <div class="flex items-center mb-2">
        <div class="flex items-center mr-2">
          <span v-for="i in 5" :key="i" class="text-yellow-400">
            <svg v-if="i <= Math.round(product.rating)" class="w-4 h-4 fill-current" viewBox="0 0 24 24">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"></path>
            </svg>
            <svg v-else class="w-4 h-4 fill-current text-gray-300" viewBox="0 0 24 24">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"></path>
            </svg>
          </span>
        </div>
        <span class="text-sm text-gray-600">{{ product.rating }}/5</span>
      </div>
      <p class="text-sm text-gray-600 mb-3 line-clamp-2">{{ product.description }}</p>
      <div class="flex justify-between items-center">
        <span class="text-lg font-bold text-gray-900">{{ formatPrice(product.price) }}</span>
        <span class="text-xs px-2 py-1 bg-gray-100 rounded-full text-gray-600">{{ product.category }}</span>
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

    return {
      formatPrice
    };
  }
});
</script>
