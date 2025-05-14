<template>
  <div class="bg-white rounded-lg shadow-md p-6">
    <h2 class="text-2xl font-semibold mb-6">주문 정보</h2>
    
    <!-- 주문자 정보 폼 -->
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- 배송 정보 섹션 -->
      <div class="border-b pb-6">
        <h3 class="text-lg font-medium mb-4">배송 정보</h3>
        <div class="space-y-4">
          <!-- 이름 입력 -->
          <div>
            <label for="name" class="block text-sm font-medium text-gray-700">
              이름 <span class="text-red-500">*</span>
            </label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              required
            />
            <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
          </div>
          
          <!-- 연락처 입력 -->
          <div>
            <label for="phone" class="block text-sm font-medium text-gray-700">
              연락처 <span class="text-red-500">*</span>
            </label>
            <input
              id="phone"
              v-model="form.phone"
              @input="handlePhoneInput"
              @keydown="preventNonNumeric"
              @compositionstart="handleComposition"
              @compositionend="handleComposition"
              type="text"
              inputmode="numeric"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              placeholder="010-0000-0000"
              maxlength="13"
              required
            />
            <p v-if="errors.phone" class="mt-1 text-sm text-red-600">{{ errors.phone }}</p>
          </div>
          
          <!-- 주소 입력 -->
          <div>
            <label for="postCode" class="block text-sm font-medium text-gray-700">
              우편번호 <span class="text-red-500">*</span>
            </label>
            <div class="flex space-x-2">
              <input
                id="postCode"
                v-model="form.postCode"
                type="text"
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
                required
                readonly
              />
              <button
                type="button"
                @click="openAddressSearch"
                class="mt-1 bg-gray-200 text-gray-800 py-2 px-4 rounded-md hover:bg-gray-300"
              >
                주소 검색
              </button>
            </div>
          </div>
          
          <div>
            <label for="address" class="block text-sm font-medium text-gray-700">
              주소 <span class="text-red-500">*</span>
            </label>
            <input
              id="address"
              v-model="form.address"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              required
              readonly
            />
          </div>
          
          <div>
            <label for="detailAddress" class="block text-sm font-medium text-gray-700">
              상세 주소
            </label>
            <input
              id="detailAddress"
              v-model="form.detailAddress"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          
          <!-- 배송 요청사항 -->
          <div>
            <label for="deliveryRequest" class="block text-sm font-medium text-gray-700">
              배송 요청사항
            </label>
            <textarea
              id="deliveryRequest"
              v-model="form.deliveryRequest"
              rows="2"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              placeholder="배송 기사님께 전달할 메시지를 입력해주세요."
            ></textarea>
          </div>
        </div>
      </div>
      
      <!-- 주문 아이템 목록 -->
      <div class="border-b pb-6">
        <h3 class="text-lg font-medium mb-4">주문 상품</h3>
        <div class="space-y-4">
          <div v-for="item in cartItems" :key="item.product.id" class="flex items-center py-2">
            <div class="flex-shrink-0 w-16 h-16 bg-gray-100 rounded overflow-hidden">
              <img 
                v-if="item.product.thumbnailImageUrl" 
                :src="item.product.thumbnailImageUrl" 
                :alt="item.product.name"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
                이미지 없음
              </div>
            </div>
            <div class="ml-4 flex-1">
              <h4 class="text-sm font-medium text-gray-800">{{ item.product.name }}</h4>
              <div class="mt-1 flex items-center justify-between">
                <div>
                  <span class="text-sm text-gray-500">{{ formatPrice(item.product.price) }}</span>
                  <span v-if="item.product.originalPrice !== item.product.price" class="ml-2 text-xs line-through text-gray-400">
                    {{ formatPrice(item.product.originalPrice) }}
                  </span>
                </div>
                <div class="text-sm text-gray-600">
                  {{ item.quantity }}개
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 주문 금액 요약 -->
      <div class="border-b pb-6">
        <h3 class="text-lg font-medium mb-4">주문 금액</h3>
        <div class="space-y-2">
          <div class="flex justify-between text-sm">
            <span class="text-gray-600">상품 금액</span>
            <span>{{ formatPrice(subtotal) }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-600">할인 금액</span>
            <span class="text-red-500">-{{ formatPrice(totalDiscount) }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-600">배송비</span>
            <span>{{ formatPrice(shippingCost) }}</span>
          </div>
          <div class="flex justify-between text-base font-semibold mt-4 pt-4 border-t">
            <span>총 결제 금액</span>
            <span class="text-blue-600">{{ formatPrice(total) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 결제 버튼 -->
      <div class="mt-8">
        <button
          type="submit"
          class="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          :disabled="loading"
        >
          <template v-if="loading">
            <span class="inline-block animate-spin mr-2">&#9696;</span>
            주문 처리 중...
          </template>
          <template v-else>
            결제하기
          </template>
        </button>
      </div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue';
import { useCartStore, CartItem } from '@entities/cart';
import { useOrderWithAuth } from '../model/orderFeature';
import {formatPrice} from "@shared/lib";

export default defineComponent({
  name: 'OrderForm',
  setup() {
    const cartStore = useCartStore();
    const { createOrder, loading } = useOrderWithAuth();
    
    // 폼 상태
    const form = ref({
      name: '',
      phone: '',
      postCode: '',
      address: '',
      detailAddress: '',
      deliveryRequest: ''
    });
    
    // 폼 검증 에러
    const errors = ref({
      name: '',
      phone: '',
      postCode: '',
      address: '',
    });
    
    // 카트 아이템
    const cartItems = computed<CartItem[]>(() => cartStore.items);
    
    // 금액 계산
    const subtotal = computed(() => cartStore.subtotal);
    const totalDiscount = computed(() => cartStore.totalDiscount);
    const shippingCost = computed(() => cartStore.shippingInfo.cost);
    const total = computed(() => cartStore.total);
    
    // 주소 검색
    const openAddressSearch = () => {
      form.value.postCode = '12345';
      form.value.address = '서울특별시 강남구 테헤란로 0';
    };
    
    // 폼 검증
    const validateForm = () => {
      let isValid = true;
      errors.value = {
        name: '',
        phone: '',
        postCode: '',
        address: ''
      };
      
      if (!form.value.name.trim()) {
        errors.value.name = '이름을 입력해주세요.';
        isValid = false;
      }

      if (!form.value.phone.trim()) {
        errors.value.phone = '연락처를 입력해주세요.';
        isValid = false;
      } else if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(form.value.phone)) {
        errors.value.phone = '올바른 연락처 형식이 아닙니다.';
        isValid = false;
      }
      
      if (!form.value.postCode.trim()) {
        errors.value.postCode = '우편번호를 입력해주세요.';
        isValid = false;
      }
      
      if (!form.value.address.trim()) {
        errors.value.address = '주소를 입력해주세요.';
        isValid = false;
      }
      
      return isValid;
    };
    
    // 폼 제출
    const handleSubmit = async () => {
      if (!validateForm()) {
        return;
      }
      
      // 장바구니가 비어있는지 한번 더 확인
      if (cartItems.value.length === 0) {
        alert('장바구니가 비어있습니다. 상품을 먼저 담아주세요.');
        window.location.href = '/products'; // 상품 목록 페이지로 이동
        return;
      }
      
      try {
        // 주문 생성
        const success = await createOrder({
          name: form.value.name,
          phone: form.value.phone,
          deliveryRequest: form.value.deliveryRequest,
          address: {
            address: form.value.address,
            detailAddress: form.value.detailAddress,
            postCode: form.value.postCode
          }
        });
        
        if (!success) {
          // 주문 생성 실패 처리
          console.error('주문 생성 실패');
        }
      } catch (error) {
        console.error('주문 처리 중 오류 발생:', error);
        alert('주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
      }
    };

    const formatPhoneNumber = (value: string) => {
      const numbers = value.replace(/[^\d]/g, '').slice(0, 11);

      if (numbers.length <= 3) {
        return numbers;
      } else if (numbers.length <= 7) {
        return numbers.slice(0, 3) + '-' + numbers.slice(3);
      } else {
        return numbers.slice(0, 3) + '-' + numbers.slice(3, 7) + '-' + numbers.slice(7);
      }
    };

    const isComposing = ref(false);

    const handleComposition = (event: CompositionEvent) => {
      isComposing.value = event.type === 'compositionstart';

      if (event.type === 'compositionend') {
        const input = event.target as HTMLInputElement;
        form.value.phone = formatPhoneNumber(input.value);
      }
    };

    const handlePhoneInput = (event: Event) => {
      if (isComposing.value) {
        return;
      }

      const input = event.target as HTMLInputElement;
      form.value.phone = formatPhoneNumber(input.value);
    };

    const preventNonNumeric = (event: KeyboardEvent) => {
      // 허용할 키들: 숫자, Backspace, Delete, Tab, Arrow keys, Home, End
      const allowedKeys = [
        'Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight',
        'ArrowUp', 'ArrowDown', 'Home', 'End'
      ];

      // 숫자 키패드도 허용
      const isNumeric = /^[0-9]$/.test(event.key);

      // Ctrl/Cmd + A/C/V/X 허용
      const isCopyPaste = (event.ctrlKey || event.metaKey) &&
          ['a', 'c', 'v', 'x'].includes(event.key.toLowerCase());

      if (!isNumeric && !allowedKeys.includes(event.key) && !isCopyPaste) {
        event.preventDefault();
      }
    };

    // 컴포넌트 마운트 시 카트 아이템이 있는지 확인
    onMounted(() => {
      if (cartStore.isEmpty) {
        alert('장바구니가 비어있습니다.');
        window.location.href = '/cart';
      }
    });
    
    return {
      form,
      errors,
      cartItems,
      subtotal,
      totalDiscount,
      shippingCost,
      total,
      loading,
      openAddressSearch,
      handleSubmit,
      formatPrice,
      handlePhoneInput,
      preventNonNumeric,
      handleComposition
    };
  }
});
</script>
