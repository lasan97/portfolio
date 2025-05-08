<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-8">주문하기</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 주문 양식 (2/3 너비) -->
      <div class="lg:col-span-2">
        <OrderForm />
      </div>
      
      <!-- 주문 요약 (1/3 너비) -->
      <div>
        <CartSummary
          :title="'주문 요약'"
          :show-checkout-button="false"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { OrderForm } from '@features/order';
import { CartSummary } from '@entities/cart';

export default defineComponent({
  name: 'OrderPage',
  components: {
    OrderForm,
    CartSummary
  }
});
</script>
