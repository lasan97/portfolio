import { defineStore } from 'pinia';
import { CartItem, CartState, ShippingInfo } from './types';
import { Product } from '@entities/product';
import { FREE_SHIPPING_THRESHOLD, SHIPPING_COST } from './constants';

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
    // 장바구니에 상품 추가
    addToCart(product: Product, quantity: number = 1) {
      const existingItem = this.items.find(item => item.product.id === product.id);

      if (existingItem) {
        // 이미 장바구니에 있는 상품이면 수량만 증가
        existingItem.quantity += quantity;
      } else {
        // 새 상품이면 장바구니에 추가
        this.items.push({ product, quantity });
      }
    },

    // 장바구니에서 상품 제거
    removeFromCart(productId: number) {
      const index = this.items.findIndex(item => item.product.id === productId);

      if (index !== -1) {
        this.items.splice(index, 1);
      }
    },

    // 장바구니 상품 수량 변경
    updateQuantity(productId: number, quantity: number) {
      const item = this.items.find(item => item.product.id === productId);

      if (item) {
        if (quantity > 0) {
          item.quantity = quantity;
        } else {
          // 수량이 0 이하면 상품 제거
          this.removeFromCart(productId);
        }
      }
    },

    // 장바구니 비우기
    clearCart() {
      this.items = [];
    }
  }
});
