<template>
  <div class="flex items-center justify-between p-4 border-b border-gray-200">
    <!-- 상품 정보 -->
    <div class="flex items-center flex-1">
      <!-- 상품 이미지 -->
      <img 
        :src="item.product.thumbnailImageUrl || 'https://via.placeholder.com/150'" 
        :alt="item.product.name" 
        class="w-16 h-16 object-cover rounded"
        @error="handleImageError"
      />
      <!-- 상품 이름 및 가격 -->
      <div class="ml-4">
        <h3 class="text-sm font-medium text-gray-900">{{ item.product.name }}</h3>
        <p class="text-sm text-gray-600">{{ formatPrice(item.product.price) }}</p>
      </div>
    </div>
    
    <!-- 수량 조절 및 삭제 -->
    <div class="flex items-center">
      <!-- 수량 조절 컨트롤 -->
      <div class="flex items-center border border-gray-300 rounded">
        <!-- 감소 버튼 -->
        <button 
          @click="decreaseQuantity"
          class="w-8 h-8 flex items-center justify-center text-gray-600 hover:bg-gray-100"
          :disabled="isUpdating"
        >
          <span class="text-lg">-</span>
        </button>
        
        <!-- 수량 표시 -->
        <span class="w-10 text-center">{{ item.quantity }}</span>
        
        <!-- 증가 버튼 -->
        <button 
          @click="increaseQuantity"
          class="w-8 h-8 flex items-center justify-center text-gray-600 hover:bg-gray-100"
          :disabled="isUpdating"
        >
          <span class="text-lg">+</span>
        </button>
      </div>
      
      <!-- 삭제 버튼 -->
      <button 
        @click="removeItem"
        class="ml-4 text-red-500 hover:text-red-700"
        :disabled="isUpdating"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { CartItem } from '@entities/cart';
import { useCartWithAuth } from '@features/cart';

export default defineComponent({
  name: 'CartItemComponent',
  props: {
    item: {
      type: Object as PropType<CartItem>,
      required: true
    }
  },
  setup(props, { emit }) {
    const { updateQuantity, removeFromCart } = useCartWithAuth();
    const isUpdating = ref(false);
    const imageError = ref(false);
    
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };
    
    const increaseQuantity = async () => {
      try {
        isUpdating.value = true;
        const newQuantity = props.item.quantity + 1;
        await updateQuantity(props.item.product.id, newQuantity);
        emit('updated');
      } catch (error) {
        console.error('수량 증가 실패:', error);
      } finally {
        isUpdating.value = false;
      }
    };
    
    const decreaseQuantity = async () => {
      if (props.item.quantity <= 1) {
        // 수량이 1이면 더 이상 감소 불가
        return;
      }
      
      try {
        isUpdating.value = true;
        const newQuantity = props.item.quantity - 1;
        await updateQuantity(props.item.product.id, newQuantity);
        emit('updated');
      } catch (error) {
        console.error('수량 감소 실패:', error);
      } finally {
        isUpdating.value = false;
      }
    };
    
    const removeItem = async () => {
      try {
        isUpdating.value = true;
        await removeFromCart(props.item.product.id);
        emit('removed', props.item.product.id);
      } catch (error) {
        console.error('상품 제거 실패:', error);
      } finally {
        isUpdating.value = false;
      }
    };
    
    const handleImageError = (e: Event) => {
      imageError.value = true;
      // 이미지 로드 실패 시 기본 이미지로 대체 (외부 placeholder 이미지 사용)
      const imgElement = e.target as HTMLImageElement;
      imgElement.src = 'https://via.placeholder.com/150';
    };
    
    return {
      formatPrice,
      increaseQuantity,
      decreaseQuantity,
      removeItem,
      isUpdating,
      handleImageError,
      imageError
    };
  }
});
</script>
