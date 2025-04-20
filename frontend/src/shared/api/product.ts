import apiInstance from './instance';
import { ProductCategory, StockAdjustmentData, ProductData } from '@entities/product/model/types';

export interface ProductResponse {
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

export interface ProductListResponse {
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

export const productApi = {
  // 상품 목록 조회
  getProducts: async () => {
    const response = await apiInstance.get<ProductListResponse[]>('/api/products');
    return response.data;
  },

  // 단일 상품 조회
  getProduct: async (id: number) => {
    const response = await apiInstance.get<ProductResponse>(`/api/products/${id}`);
    return response.data;
  },

  // 관리자용 상품 추가
  createProduct: async (productData: ProductData) => {
    const response = await apiInstance.post<number>('/api/products', productData);
    return response.data;
  },

  // 관리자용 상품 수정
  updateProduct: async (id: number, productData: ProductData) => {
    const response = await apiInstance.put<number>(`/api/products/${id}`, productData);
    return response.data;
  },

  // 관리자용 상품 삭제
  deleteProduct: async (id: number) => {
    await apiInstance.delete(`/api/products/${id}`);
  },

  // 관리자용 재고 조정
  adjustStock: async (id: number, adjustData: StockAdjustmentData) => {
    console.log(adjustData);
    const response = await apiInstance.patch<number>(`/api/products/${id}/stock`, adjustData);
    return response.data;
  }
};
