<template>
  <div class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">{{ productData ? '상품 정보 수정' : '새 상품 등록' }}</h3>
      </div>
      
      <form @submit.prevent="submitForm" class="px-6 py-4">
        <div v-if="isLoading" class="flex justify-center items-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
        </div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 상품명 -->
          <div class="col-span-2">
            <label for="name" class="block text-sm font-medium text-gray-700">상품명</label>
            <input 
              id="name" 
              v-model="form.name" 
              type="text" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
          </div>
          
          <!-- 원가 -->
          <div>
            <label for="originalPrice" class="block text-sm font-medium text-gray-700">원가 (원)</label>
            <input 
              id="originalPrice" 
              v-model.number="form.originalPrice" 
              type="number" 
              min="0" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
          </div>
          
          <!-- 판매가 -->
          <div>
            <label for="price" class="block text-sm font-medium text-gray-700">판매가 (원)</label>
            <input 
              id="price" 
              v-model.number="form.price" 
              type="number" 
              min="0" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
          </div>
          
          <!-- 카테고리 -->
          <div>
            <label for="category" class="block text-sm font-medium text-gray-700">카테고리</label>
            <select 
              id="category" 
              v-model="form.category" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
              <option v-for="category in categories" :key="category.code" :value="category.code">
                {{ category.description }}
              </option>
            </select>
          </div>
          
          <!-- 재고 (신규 등록 시에만) -->
          <div v-if="!productId">
            <label for="stock" class="block text-sm font-medium text-gray-700">초기 재고 수량</label>
            <input 
              id="stock" 
              v-model.number="form.stock" 
              type="number" 
              min="0" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
          </div>
          
          <!-- 썸네일 이미지 URL -->
          <div class="col-span-2">
            <label for="thumbnailImageUrl" class="block text-sm font-medium text-gray-700">썸네일 이미지 URL</label>
            <input 
              id="thumbnailImageUrl" 
              v-model="form.thumbnailImageUrl" 
              type="url" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
            <div v-if="form.thumbnailImageUrl" class="mt-2">
              <p class="text-sm text-gray-500 mb-1">이미지 미리보기:</p>
              <img :src="form.thumbnailImageUrl" alt="썸네일 미리보기" class="h-24 w-24 object-cover border rounded">
            </div>
          </div>
          
          <!-- 상품 설명 -->
          <div class="col-span-2">
            <label for="description" class="block text-sm font-medium text-gray-700">상품 설명</label>
            <textarea 
              id="description" 
              v-model="form.description" 
              rows="5" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            ></textarea>
          </div>
        </div>
        
        <!-- 에러 메시지 -->
        <div v-if="error" class="mt-4 text-red-600 text-sm">
          {{ error }}
        </div>
        
        <!-- 버튼 영역 -->
        <div class="mt-6 flex justify-end space-x-3">
          <button 
            type="button" 
            @click="$emit('close')" 
            class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            취소
          </button>
          <button 
            type="submit" 
            class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '처리 중...' : (productData ? '수정하기' : '등록하기') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, reactive, computed, watch } from 'vue';
import { useProductStore, ProductCategory, Product } from '@entities/product';
import { ProductFormData, mapToProductFormData } from '@features/productManagement';

export default defineComponent({
  name: 'ProductFormModal',
  props: {
    // product 대신 productId만 받도록 변경
    productId: {
      type: Number as PropType<number | null>,
      default: null
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    // 카테고리 옵션 목록
    const categories = computed(() => ProductCategory.entries());

    const productStore = useProductStore();
    const isLoading = ref(false);
    
    // 상품 데이터 (스토어에서 가져옴)
    const productData = computed<Product | null>(() => {
      if (props.productId) {
        return productStore.getProductById(props.productId) || null;
      }
      return null;
    });

    // 폼 상태
    const form = reactive<ProductFormData>({
      name: '',
      originalPrice: 0,
      price: 0,
      description: '',
      thumbnailImageUrl: '',
      category: ProductCategory.ELECTRONICS,
      stock: 0 // 초기 재고 (신규 등록 시에만 사용)
    });
    
    // 에러 상태
    const error = ref('');
    const isSubmitting = ref(false);
    
    // productId가 변경될 때마다 상품 정보 로드
    watch(() => props.productId, async (newProductId) => {
      if (newProductId) {
        isLoading.value = true;
        try {
          // 상품 상세 정보 로드
          await productStore.fetchProductById(newProductId);
        } catch (err) {
          console.error('상품 정보 로드 실패:', err);
          error.value = '상품 정보를 불러오는 중 오류가 발생했습니다.';
        } finally {
          isLoading.value = false;
        }
      }
    }, { immediate: true });
    
    // 상품 데이터가 변경될 때마다 폼 업데이트
    watch(() => productData.value, (newProductData) => {
      if (newProductData) {
        // 매핑 함수를 사용하여 도메인 모델을 폼 데이터로 변환
        const formData = mapToProductFormData(newProductData);
        Object.assign(form, formData);
      } else {
        // 새 상품 등록 시 초기화
        form.name = '';
        form.originalPrice = 0;
        form.price = 0;
        form.description = '';
        form.thumbnailImageUrl = '';
        form.category = ProductCategory.ELECTRONICS;
        form.stock = 0;
      }
    }, { immediate: true });
    
    // 폼 제출
    const submitForm = async () => {
      error.value = '';
      
      // 기본 폼 검증
      if (form.originalPrice < form.price) {
        error.value = '원가는 판매가보다 작을 수 없습니다.';
        return;
      }
      
      // 가격이 0인지 확인
      if (form.price <= 0) {
        error.value = '판매가는 0보다 커야 합니다.';
        return;
      }
      
      // 필드 유효성 검사 추가
      if (!form.name.trim()) {
        error.value = '상품명을 입력해주세요.';
        return;
      }
      
      if (!form.thumbnailImageUrl.trim()) {
        error.value = '썸네일 이미지 URL을 입력해주세요.';
        return;
      }
      
      try {
        isSubmitting.value = true;
        
        // 폼 제출 이벤트 발생
        emit('submit', form);
        
        // 성공 시에만 모달 닫기
        // 참고: 부모 컴포넌트에서 API 호출 성공 여부를 처리해야 함
      } catch (err) {
        console.error('폼 제출 오류:', err);
        error.value = '오류가 발생했습니다. 다시 시도해주세요.';
      } finally {
        isSubmitting.value = false;
      }
    };
    
    return {
      form,
      categories,
      error,
      isSubmitting,
      isLoading,
      productData,
      submitForm
    };
  }
});
</script>
