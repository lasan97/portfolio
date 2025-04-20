import { ProductCategory, ProductStatus } from './constants';

// 핵심 도메인 모델
export interface Product {
  id: number;
  name: string;
  originalPrice: number; // 원가
  price: number; // 판매가
  description: string;
  thumbnailImageUrl?: string;
  category: ProductCategory;
  inStock: boolean;
  status?: ProductStatus;
  stock?: ProductStock;
}

export interface ProductStock {
  id?: number;
  quantity: number;
  version?: number;
}
