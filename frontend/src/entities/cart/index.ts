// 도메인 모델
export type { CartItem, CartState, ShippingInfo } from './model/types';
export { FREE_SHIPPING_THRESHOLD, SHIPPING_COST } from './model/constants';

// UI 모델
export type { CartSummaryProps } from './model/ui-types';

// 스토어
export { useCartStore } from './model/cartStore';

// UI 컴포넌트
export { default as CartSummary } from './ui/CartSummary.vue';
