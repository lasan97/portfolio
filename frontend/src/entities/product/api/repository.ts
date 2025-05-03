import { apiInstance } from '@shared/api';
import { ProductRequest, ProductResponse } from './types';
import { Product } from '../model/types';
import { mapToProduct, mapToProducts } from './mappers';
import { StockChangeReason } from '../model/constants';

export const productRepository = {
  // 상품 목록 조회
  async getProducts(): Promise<Product[]> {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  },

  // 단일 상품 조회
  async getProduct(id: number): Promise<Product> {
    const response = await apiInstance.get<ProductResponse.Detail>(`/api/products/${id}`);
    return mapToProduct(response.data);
  },

  // 관리자용 상품 추가
  async createProduct(productData: ProductRequest.Create): Promise<number> {
    const response = await apiInstance.post<number>('/api/products', productData);
    return response.data;
  },

  // 관리자용 상품 수정
  async updateProduct(id: number, productData: ProductRequest.Update): Promise<number> {
    const response = await apiInstance.put<number>(`/api/products/${id}`, productData);
    return response.data;
  },

  // 관리자용 상품 삭제
  async deleteProduct(id: number): Promise<void> {
    await apiInstance.delete(`/api/products/${id}`);
  },

  // 관리자용 재고 조정
  async adjustStock(id: number, adjustData: ProductRequest.StockAdjustment): Promise<number> {
    const response = await apiInstance.patch<number>(`/api/products/${id}/stocks`, adjustData);
    return response.data;
  }
};
