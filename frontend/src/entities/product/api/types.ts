import { ProductCategory } from '../model/constants';

// API 요청 타입
export namespace ProductRequest {
  export interface Create {
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
    stock: number;
  }

  export interface Update {
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
  }

  export interface StockAdjustment {
    quantity: number;
    reason: string;
    memo?: string;
  }
}

// API 응답 타입
export namespace ProductResponse {
  export interface Detail {
    id: number;
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
    status: string;
    stock: number;
    discountRate: number;
    createdAt: string;
    updatedAt: string;
  }

  export interface List {
    id: number;
    name: string;
    price: number;
    originalPrice: number;
    thumbnailImageUrl: string;
    category: ProductCategory;
    status: string;
    stock: number;
    discountRate: number;
  }
}
