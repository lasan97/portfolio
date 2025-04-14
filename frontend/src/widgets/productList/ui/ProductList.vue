<template>
  <div class="container mx-auto px-4 py-8">
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
    </div>
    
    <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
      <span class="block sm:inline">{{ error }}</span>
    </div>
    
    <template v-else>
      <!-- 필터 및 검색 섹션 -->
      <div class="mb-8">
        <div class="flex flex-col md:flex-row md:justify-between md:items-center gap-4">
          <div class="flex-grow max-w-md">
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="상품명으로 검색..." 
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
          </div>
          
          <div class="flex flex-wrap gap-2">
            <select 
              v-model="selectedCategory" 
              class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체 카테고리</option>
              <option v-for="category in categories" :key="category" :value="category">
                {{ category }}
              </option>
            </select>
            
            <select 
              v-model="sortOption" 
              class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="nameAsc">이름 (오름차순)</option>
              <option value="nameDesc">이름 (내림차순)</option>
              <option value="priceAsc">가격 (낮은순)</option>
              <option value="priceDesc">가격 (높은순)</option>
              <option value="ratingDesc">평점 (높은순)</option>
            </select>
            
            <label class="flex items-center px-4 py-2 border border-gray-300 rounded-md">
              <input 
                v-model="showInStockOnly" 
                type="checkbox" 
                class="form-checkbox h-5 w-5 text-blue-600"
              >
              <span class="ml-2 text-gray-700">재고 있는 상품만</span>
            </label>
          </div>
        </div>
      </div>
      
      <!-- 상품 목록 결과 -->
      <div v-if="filteredProducts.length === 0" class="text-center py-12">
        <p class="text-gray-500 text-lg">검색 결과가 없습니다.</p>
      </div>
      
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <product-card 
          v-for="product in filteredProducts" 
          :key="product.id" 
          :product="product"
          @click="$emit('select-product', product.id)" 
          class="cursor-pointer"
        />
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import {computed, defineComponent, onMounted, ref} from 'vue';
import {ProductCard, useProductStore} from '@entities/product';

export default defineComponent({
  name: 'ProductList',
  components: {
    ProductCard
  },
  emits: ['select-product'],
  setup() {
    const productStore = useProductStore();
    const { products, isLoading, error } = productStore;
    
    // 필터링 상태
    const searchQuery = ref('');
    const selectedCategory = ref('');
    const showInStockOnly = ref(false);
    const sortOption = ref('nameAsc');
    
    // 데이터 로드
    onMounted(async () => {
      if (products.length === 0) {
        await productStore.fetchProducts();
      }
    });
    
    // 카테고리 목록 계산
    const categories = computed(() => {
      const categorySet = new Set<string>();
      productStore.products.forEach(product => categorySet.add(product.category));
      return Array.from(categorySet);
    });
    
    // 필터링된 상품 목록
    const filteredProducts = computed(() => {
      let result = [...productStore.products];
      
      // 검색어 필터링
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(p => 
          p.name.toLowerCase().includes(query) || 
          p.description.toLowerCase().includes(query)
        );
      }
      
      // 카테고리 필터링
      if (selectedCategory.value) {
        result = result.filter(p => p.category === selectedCategory.value);
      }
      
      // 재고 있는 상품만 필터링
      if (showInStockOnly.value) {
        result = result.filter(p => p.inStock);
      }
      
      // 정렬
      switch (sortOption.value) {
        case 'nameAsc':
          result.sort((a, b) => a.name.localeCompare(b.name));
          break;
        case 'nameDesc':
          result.sort((a, b) => b.name.localeCompare(a.name));
          break;
        case 'priceAsc':
          result.sort((a, b) => a.price - b.price);
          break;
        case 'priceDesc':
          result.sort((a, b) => b.price - a.price);
          break;
        case 'ratingDesc':
          result.sort((a, b) => b.rating - a.rating);
          break;
      }
      
      return result;
    });
    
    return {
      products,
      isLoading,
      error,
      searchQuery,
      selectedCategory,
      categories,
      showInStockOnly,
      sortOption,
      filteredProducts
    };
  }
});
</script>
