// 도메인 모델
export type { CartState, ShippingInfo } from './model/types';
export { FREE_SHIPPING_THRESHOLD, SHIPPING_COST } from './model/constants';

// UI 모델
export type { CartSummaryProps } from './model/ui-types';

// API 모델 및 함수
export type { CartRequest, CartResponse } from './api/types';
export { cartRepository } from './api/repository';
export { mapToCartItem, mapToCartItems } from './api/mappers';

// 스토어
export { useCartStore } from './model/cartStore';

// UI 컴포넌트
export { default as CartSummary } from './ui/CartSummary.vue';
export { default as CartItemComponent } from './ui/CartItem.vue';
export { default as CartPage } from './ui/CartPage.vue';

// 타입 정의
export type { CartItem } from './model/types';
