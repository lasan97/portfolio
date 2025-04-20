<template>
  <div class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl max-w-lg w-full mx-4">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">재고 조정</h3>
      </div>
      
      <div class="px-6 py-4">
        <!-- 상품 정보 -->
        <div class="mb-4">
          <div class="flex items-center">
            <div class="h-12 w-12 flex-shrink-0 mr-4">
              <img class="h-12 w-12 rounded-md object-cover" :src="product?.thumbnailImageUrl || product?.imageUrl" alt="" />
            </div>
            <div>
              <div class="text-lg font-medium text-gray-900">{{ product?.name }}</div>
              <div class="text-sm text-gray-500">현재 재고: {{ product?.stock?.quantity || 0 }}개</div>
            </div>
          </div>
        </div>
        
        <form @submit.prevent="submitForm">
          <!-- 조정 수량 -->
          <div class="mb-4">
            <label for="quantity" class="block text-sm font-medium text-gray-700">조정 수량</label>
            <div class="mt-1 flex rounded-md shadow-sm">
              <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 sm:text-sm">
                +/-
              </span>
              <input 
                id="quantity" 
                v-model.number="form.quantity" 
                type="number" 
                class="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md border border-gray-300 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                placeholder="현재 재고의 수"
                required
              >
            </div>
            <p class="mt-1 text-sm text-gray-500">
              현재 재고의 수를 입력해주세요.
            </p>
          </div>
          
          <!-- 변경 사유 -->
          <div class="mb-4">
            <label for="reason" class="block text-sm font-medium text-gray-700">변경 사유</label>
            <select 
              id="reason" 
              v-model="form.reason" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              required
            >
              <option v-for="option in reasonOptions" :key="option.code" :value="option.code">
                {{ option.description }}
              </option>
            </select>
          </div>
          
          <!-- 메모 -->
          <div class="mb-4">
            <label for="memo" class="block text-sm font-medium text-gray-700">메모 (선택사항)</label>
            <textarea 
              id="memo" 
              v-model="form.memo" 
              rows="3" 
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
            ></textarea>
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
              {{ isSubmitting ? '처리 중...' : '확인' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType, ref, reactive, computed} from 'vue';
import { Product, StockChangeReason, StockAdjustmentData } from '@entities/product/model/types';

export default defineComponent({
  name: 'StockAdjustmentModal',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    // 폼 상태
    const form = reactive({
      quantity: props.product.stock?.quantity,
      reason: StockChangeReason.ADJUSTMENT,
      memo: ''
    });
    
    // 에러 상태
    const error = ref('');
    const isSubmitting = ref(false);
    
    // 재고 변경 사유 옵션
    const reasonOptions = computed(() => StockChangeReason.entries());
    
    // 폼 제출
    const submitForm = async () => {
      error.value = '';
      
      // 검증
      if (form.quantity < 0) {
        error.value = '수량은 0보다 작을 수 없습니다.';
        return;
      }
      
      try {
        isSubmitting.value = true;
        
        // 제출할 데이터 구성
        const stockAdjustmentData: StockAdjustmentData = {
          quantity: form.quantity,
          reason: form.reason,
          memo: form.memo
        };
        
        // 폼 제출 이벤트 발생
        emit('submit', stockAdjustmentData);
      } catch (err) {
        console.error('폼 제출 오류:', err);
        error.value = '오류가 발생했습니다. 다시 시도해주세요.';
      } finally {
        isSubmitting.value = false;
      }
    };
    
    return {
      form,
      error,
      isSubmitting,
      reasonOptions,
      submitForm
    };
  }
});
</script>
