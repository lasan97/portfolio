// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus, StockChangeReason } from './model/constants';

// UI 모델
export type { ProductDisplayProps } from './model/ui-types';

// API 모델 및 함수
export type { ProductRequest, ProductResponse } from './api/types';
export { productRepository } from './api/repository';
export { mapToProduct, mapToProducts, mapToProductDisplayProps } from './api/mappers';

// 스토어
export { useProductStore } from './model/productStore';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
