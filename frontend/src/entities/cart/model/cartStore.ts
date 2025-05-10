import { defineStore } from 'pinia';
import { CartItem, CartState, ShippingInfo } from './types';
import { Product } from '@entities/product';
import { FREE_SHIPPING_THRESHOLD, SHIPPING_COST } from './constants';
import { cartRepository } from '../api';
import { useAuthStore } from '@features/auth';

export const useCartStore = defineStore('cart', {
  state: (): CartState => ({
    items: [],
  }),

  getters: {
    // 장바구니에 담긴 상품 수
    totalItems: (state): number => {
      return state.items.reduce((total, item) => total + item.quantity, 0);
    },

    // 장바구니 상품 원가 총액
    originalSubtotal: (state): number => {
      return state.items.reduce((total, item) => {
        return total + (item.product.originalPrice * item.quantity);
      }, 0);
    },

    // 장바구니 상품 할인가 총액
    subtotal: (state): number => {
      return state.items.reduce((total, item) => {
        return total + (item.product.price * item.quantity);
      }, 0);
    },

    // 총 할인 금액
    totalDiscount: (state): number => {
      return state.items.reduce((total, item) => {
        return total + ((item.product.originalPrice - item.product.price) * item.quantity);
      }, 0);
    },

    // 할인율 (%)
    discountRate: (state): number => {
      const originalTotal = state.items.reduce((total, item) => {
        return total + (item.product.originalPrice * item.quantity);
      }, 0);

      const discountTotal = state.items.reduce((total, item) => {
        return total + ((item.product.originalPrice - item.product.price) * item.quantity);
      }, 0);

      if (originalTotal === 0) return 0;

      return Math.round((discountTotal / originalTotal) * 100);
    },

    // 배송비 정보
    shippingInfo: (state): ShippingInfo => {
      const subtotal = state.items.reduce((total, item) => {
        return total + (item.product.price * item.quantity);
      }, 0);

      return {
        required: subtotal < FREE_SHIPPING_THRESHOLD && subtotal > 0,
        cost: subtotal < FREE_SHIPPING_THRESHOLD && subtotal > 0 ? SHIPPING_COST : 0
      };
    },

    // 최종 결제 금액 (상품 가격 + 배송비)
    total: (state): number => {
      const subtotal = state.items.reduce((total, item) => {
        return total + (item.product.price * item.quantity);
      }, 0);

      const shippingCost = subtotal < FREE_SHIPPING_THRESHOLD && subtotal > 0 ? SHIPPING_COST : 0;

      return subtotal + shippingCost;
    },

    // 장바구니에 상품이 있는지 여부
    isEmpty: (state): boolean => {
      return state.items.length === 0;
    },

    // 상품 ID를 기준으로 장바구니 아이템 찾기
    getItemById: (state) => (productId: number): CartItem | undefined => {
      return state.items.find(item => item.product.id === productId);
    },
  },

  actions: {
    // 장바구니 아이템 목록 조회
    async fetchCartItems() {
      try {
        // API에서 장바구니 아이템 목록 조회
        const items = await cartRepository.getCartItems();
        this.items = items;
        return items;
      } catch (error) {
        console.error('장바구니 아이템 조회 중 오류 발생:', error);
        throw error;
      }
    },

    // 장바구니에 상품 추가
    async addToCart(product: Product, quantity: number = 1) {
      try {
        // 이미 장바구니에 있는 상품인지 확인
        const existingItem = this.items.find(item => item.product.id === product.id);
        
        if (existingItem) {
          // 이미 장바구니에 있는 상품이면 수량 증가
          const newQuantity = existingItem.quantity + quantity;
          
          // 백엔드에서 수량 직접 업데이트 API가 있으면 그것을 사용하는 것이 좋으나,
          // 현재 API 구조에 맞춰 제거 후 다시 추가하는 방식 사용
          try {
            // 트랜잭션 방식으로 처리하여 오류 시 롤백 가능하도록 함
            await cartRepository.removeCartItem(product.id);
            await cartRepository.addCartItem(product.id, newQuantity);
            
            // 로컬 상태 업데이트는 API 호출 성공 후에만 실행
            existingItem.quantity = newQuantity;
            
            console.log(`상품 수량 업데이트: ${product.name}, 새 수량: ${newQuantity}`);
          } catch (innerError) {
            // API 호출 실패 시 원래 상태로 유지
            console.error('장바구니 업데이트 실패:', innerError);
            // 로컬 데이터와 서버 데이터의 불일치 방지를 위해 카트 다시 초기화
            await this.fetchCartItems();
            throw innerError;
          }
        } else {
          // 새 상품이면 장바구니에 추가
          await cartRepository.addCartItem(product.id, quantity);
          
          // 로컬 상태 업데이트
          this.items.push({ product, quantity });
          
          console.log(`새 상품 추가: ${product.name}, 수량: ${quantity}`);
        }
        
        return true;
      } catch (error) {
        console.error('장바구니 상품 추가 중 오류 발생:', error);
        throw error;
      }
    },

    // 장바구니에서 상품 제거
    async removeFromCart(productId: number) {
      try {
        // API를 통해 장바구니에서 상품 제거
        await cartRepository.removeCartItem(productId);
        
        // API 호출이 성공하면 로컬 상태도 업데이트
        const index = this.items.findIndex(item => item.product.id === productId);

        if (index !== -1) {
          this.items.splice(index, 1);
        }
        
        return true;
      } catch (error) {
        console.error('장바구니 상품 제거 중 오류 발생:', error);
        throw error;
      }
    },

    // 장바구니 상품 수량 변경
    async updateQuantity(productId: number, quantity: number) {
      try {
        if (quantity <= 0) {
          // 수량이 0 이하면 상품 제거
          await this.removeFromCart(productId);
          return true;
        }
        
        // 현재 아이템 찾기
        const item = this.items.find(item => item.product.id === productId);
        
        if (item) {
          console.log(`서버 API 호출: 장바구니 상품 수량 변경 요청 - 상품ID ${productId}, 새 수량: ${quantity}`);

          // API 호출 시작: 기존 아이템 제거 후 새 수량으로 추가
          await cartRepository.removeCartItem(productId);
          await cartRepository.addCartItem(productId, quantity);

          // 로컬 상태 업데이트
          item.quantity = quantity;
          console.log(`상품 수량 업데이트 완료: 상품ID ${productId}, 새 수량: ${quantity}`);
        } else {
          console.error(`장바구니에서 해당 상품을 찾을 수 없음: 상품ID ${productId}`);
          throw new Error('장바구니에서 해당 상품을 찾을 수 없습니다.');
        }
        
        return true;
      } catch (error) {
        console.error('장바구니 상품 수량 변경 중 오류 발생:', error);
        throw error;
      }
    },

    // 장바구니 비우기
    async clearCart() {
      try {
        // 각 아이템을 순회하며 개별적으로 제거
        const removePromises = this.items.map(item => 
          cartRepository.removeCartItem(item.product.id)
        );
        
        await Promise.all(removePromises);
        
        // 로컬 상태 업데이트
        this.items = [];
        
        return true;
      } catch (error) {
        console.error('장바구니 비우기 중 오류 발생:', error);
        throw error;
      }
    },
    
    // 앱 시작 시 장바구니 초기화 (API에서 가져오기)
    async initializeCart() {
      try {
        const authStore = useAuthStore();
        
        if (authStore.isAuthenticated) {
          console.log('장바구니 초기화: API에서 데이터 가져오기');
          await this.fetchCartItems();
        } else {
          console.log('비로그인 상태: 장바구니 초기화 생략');
          // 비로그인 상태에서는 빈 장바구니 상태 유지
          this.items = [];
        }
      } catch (error) {
        console.error('장바구니 초기화 중 오류 발생:', error);
      }
    }
  }
});
