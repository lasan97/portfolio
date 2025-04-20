// UI 컴포넌트
export { default as ProductFormModal } from './ui/ProductFormModal.vue';
export { default as StockAdjustmentModal } from './ui/StockAdjustmentModal.vue';

// 모델 타입 및 매핑 함수
export type { ProductFormData, StockAdjustmentData } from './model/types';
export { 
  mapToProductFormData, 
  mapToCreateRequest, 
  mapToUpdateRequest, 
  mapToStockAdjustmentRequest
} from './model/mappers';

// API 요청 함수
export { productManagementApi } from './api/requests';
