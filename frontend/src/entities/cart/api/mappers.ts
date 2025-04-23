import { CartItem } from '../model/types';
import { CartResponse } from './types';
import { Product, ProductStatus } from '@entities/product';

// API 응답 → 도메인 모델 매핑
export const mapToCartItem = (response: CartResponse.Item): CartItem => ({
  product: {
    id: response.product.id,
    name: response.product.name,
    price: response.product.price,
    originalPrice: response.product.originalPrice,
    thumbnailImageUrl: response.product.thumbnailImageUrl,
    status: response.product.status as ProductStatus,
    // 백엔드에서 제공하지 않는 필드는 기본값 설정
    description: '',
    inStock: response.product.status === 'ACTIVE',
    category: 'ELECTRONICS' // 기본값 설정, 실제 API 응답에 카테고리가 포함되어 있으면 이 부분 수정
  } as Product,
  quantity: response.quantity
});

// API 목록 응답 → 도메인 모델 목록 매핑
export const mapToCartItems = (responses: CartResponse.Item[]): CartItem[] => 
  responses.map(response => mapToCartItem(response));
