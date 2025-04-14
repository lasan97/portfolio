<template>
  <div class="min-h-screen bg-gray-50">
    <div class="bg-white shadow">
      <div class="container mx-auto px-4 py-6">
        <h1 class="text-3xl font-bold text-gray-800">상품 목록</h1>
        <p class="text-gray-600 mt-2">다양한 상품을 살펴보세요</p>
      </div>
    </div>
    
    <div class="container mx-auto px-4 py-6">
      <div class="flex flex-col md:flex-row gap-6">
        <!-- 상품 목록 영역 -->
        <div class="md:w-3/4">
          <product-list @select-product="handleProductSelect" />
        </div>
        
        <!-- 장바구니 요약 영역 -->
        <div class="md:w-1/4 mb-6">
          <div class="sticky top-6">
            <cart-summary @checkout="goToCheckout" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useRouter } from 'vue-router';
import { ProductList } from '@widgets/productList';
import { CartSummary } from '@entities/cart';

export default defineComponent({
  name: 'ProductsPage',
  components: {
    ProductList,
    CartSummary
  },
  setup() {
    const router = useRouter();
    
    const handleProductSelect = (productId: number) => {
      // 상품 상세 페이지로 이동
      router.push(`/products/${productId}`);
    };
    
    const goToCheckout = () => {
      router.push('/checkout');
    };
    
    return {
      handleProductSelect,
      goToCheckout
    };
  }
});
</script>
