<template>
  <div class="bg-white rounded-lg shadow-md p-4">
    <h3 class="text-lg font-medium text-gray-900 mb-4">장바구니 요약</h3>
    
    <div v-if="isEmpty" class="text-center py-4">
      <p class="text-gray-500">장바구니가 비어있습니다</p>
    </div>
    
    <template v-else>
      <ul class="space-y-3">
        <li v-for="item in cart.items" :key="item.product.id" class="flex justify-between">
          <div class="flex-1">
            <p class="text-sm font-medium">{{ item.product.name }}</p>
            <p class="text-xs text-gray-500">{{ item.quantity }}개</p>
          </div>
          <p class="text-sm font-medium">{{ formatPrice(item.product.price * item.quantity) }}</p>
        </li>
      </ul>
      
      <div class="border-t border-gray-200 mt-4 pt-4 space-y-2">
        <!-- 원가 총액 -->
        <div class="flex justify-between text-sm">
          <span>상품 원가</span>
          <span class="line-through text-gray-500">{{ formatPrice(originalSubtotal) }}</span>
        </div>
        
        <!-- 할인 금액 -->
        <div class="flex justify-between text-sm">
          <span>할인 금액</span>
          <span class="text-red-600">- {{ formatPrice(totalDiscount) }}</span>
        </div>
        
        <!-- 할인된 상품 금액 -->
        <div class="flex justify-between text-sm">
          <span>할인된 상품 금액</span>
          <span>{{ formatPrice(subtotal) }}</span>
        </div>
        
        <div class="flex justify-between text-sm">
          <span>배송비</span>
          <span v-if="shippingInfo.required">{{ formatPrice(shippingInfo.cost) }}</span>
          <span v-else class="text-green-600">무료</span>
        </div>
        
        <div class="flex justify-between font-medium text-base mt-3 pt-3 border-t border-gray-200">
          <span>총 결제 금액</span>
          <span>{{ formatPrice(total) }}</span>
        </div>
        
        <!-- 총 할인율 표시 -->
        <div v-if="discountRate > 0" class="text-right">
          <span class="inline-block bg-red-100 text-red-800 text-xs px-2 py-1 rounded-full font-medium">
            총 {{ discountRate }}% 할인 중
          </span>
        </div>
      </div>
      
      <button 
        class="mt-4 w-full bg-indigo-600 text-white py-2 rounded-md hover:bg-indigo-700 transition-colors"
        @click="$emit('checkout')"
      >
        구매하기
      </button>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useCartStore } from '../model/cartStore';

export default defineComponent({
  name: 'CartSummary',
  emits: ['checkout'],
  setup() {
    const cart = useCartStore();
    
    const isEmpty = computed(() => cart.isEmpty);
    const originalSubtotal = computed(() => cart.originalSubtotal);
    const subtotal = computed(() => cart.subtotal);
    const totalDiscount = computed(() => cart.totalDiscount);
    const discountRate = computed(() => cart.discountRate);
    const shippingInfo = computed(() => cart.shippingInfo);
    const total = computed(() => cart.total);
    
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };
    
    return {
      cart,
      isEmpty,
      originalSubtotal,
      subtotal,
      totalDiscount,
      discountRate,
      shippingInfo,
      total,
      formatPrice
    };
  }
});
</script>
