<template>
  <div class="min-h-screen bg-gray-50">
    <div class="bg-white shadow">
      <div class="container mx-auto px-4 py-6">
        <h1 class="text-3xl font-bold text-gray-800">상품 목록</h1>
        <p class="text-gray-600 mt-2">다양한 상품을 살펴보세요</p>
      </div>
    </div>
    
    <product-list @select-product="handleProductSelect" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useRouter } from 'vue-router';
import { ProductList } from '@widgets/productList';

export default defineComponent({
  name: 'ProductsPage',
  components: {
    ProductList
  },
  setup() {
    const router = useRouter();
    
    const handleProductSelect = (productId: number) => {
      // 상품 상세 페이지로 이동
      router.push(`/products/${productId}`);
    };
    
    return {
      handleProductSelect
    };
  }
});
</script>
