import { ProductCategory, StockChangeReason } from '@entities/product';

// 상품 생성/수정 폼 데이터
export interface ProductFormData {
  name: string;
  originalPrice: number;
  price: number;
  description: string;
  thumbnailImageUrl: string;
  category: ProductCategory;
  stock?: number; // 신규 등록 시에만 사용
}

// 재고 조정 폼 데이터
export interface StockAdjustmentData {
  quantity: number;
  reason: StockChangeReason;
  memo?: string;
}
