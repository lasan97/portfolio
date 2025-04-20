import { Product, ProductStock } from '../model/types';
import { ProductResponse } from './types';
import { ProductStatus } from '../model/constants';
import { ProductDisplayProps } from '../model/ui-types';

// API 응답 → 도메인 모델 매핑
export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  originalPrice: response.originalPrice,
  price: response.price,
  description: response.description,
  thumbnailImageUrl: response.thumbnailImageUrl,
  category: response.category,
  inStock: response.stock > 0,
  status: response.status as ProductStatus,
  stock: {
    quantity: response.stock
  }
});

// API 목록 응답 → 도메인 모델 매핑
export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    id: response.id,
    name: response.name,
    originalPrice: response.originalPrice,
    price: response.price,
    description: '',
    thumbnailImageUrl: response.thumbnailImageUrl,
    category: response.category,
    inStock: response.stock > 0,
    status: response.status as ProductStatus,
    stock: {
      quantity: response.stock
    }
  }));

// 도메인 모델 → UI 프롭스 매핑
export const mapToProductDisplayProps = (product: Product): ProductDisplayProps => ({
  id: product.id,
  name: product.name,
  price: product.price,
  originalPrice: product.originalPrice,
  thumbnailImageUrl: product.thumbnailImageUrl,
  category: product.category,
  inStock: product.inStock
});
