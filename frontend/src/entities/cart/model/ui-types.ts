import { CartItem } from './types';

// UI 모델 - 장바구니 요약 정보를 위한 타입
export interface CartSummaryProps {
  totalItems: number;
  subtotal: number;
  shippingCost: number;
  total: number;
}