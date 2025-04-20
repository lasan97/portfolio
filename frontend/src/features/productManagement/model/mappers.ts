import { Product, ProductRequest } from '@entities/product';
import { ProductFormData, StockAdjustmentData } from './types';

// 도메인 모델 → 폼 데이터 매핑
export const mapToProductFormData = (product: Product): ProductFormData => ({
  name: product.name,
  originalPrice: product.originalPrice,
  price: product.price,
  description: product.description,
  thumbnailImageUrl: product.thumbnailImageUrl || '',
  category: product.category
});

// 폼 데이터 → API 요청 매핑 (생성)
export const mapToCreateRequest = (formData: ProductFormData): ProductRequest.Create => ({
  name: formData.name,
  originalPrice: formData.originalPrice,
  price: formData.price,
  description: formData.description,
  thumbnailImageUrl: formData.thumbnailImageUrl,
  category: formData.category,
  stock: formData.stock || 0
});

// 폼 데이터 → API 요청 매핑 (수정)
export const mapToUpdateRequest = (formData: ProductFormData): ProductRequest.Update => ({
  name: formData.name,
  originalPrice: formData.originalPrice,
  price: formData.price,
  description: formData.description,
  thumbnailImageUrl: formData.thumbnailImageUrl,
  category: formData.category
});

// 재고 조정 데이터 → API 요청 매핑
export const mapToStockAdjustmentRequest = (data: StockAdjustmentData): ProductRequest.StockAdjustment => ({
  quantity: data.quantity,
  reason: data.reason,
  memo: data.memo
});
