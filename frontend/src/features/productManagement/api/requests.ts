import { productRepository } from '@entities/product';
import { 
  mapToCreateRequest, 
  mapToUpdateRequest, 
  mapToStockAdjustmentRequest 
} from '../model/mappers';
import { ProductFormData, StockAdjustmentData } from '../model/types';

export const productManagementApi = {
  // 상품 생성
  async createProduct(formData: ProductFormData): Promise<number> {
    const requestData = mapToCreateRequest(formData);
    return await productRepository.createProduct(requestData);
  },
  
  // 상품 수정
  async updateProduct(id: number, formData: ProductFormData): Promise<number> {
    const requestData = mapToUpdateRequest(formData);
    return await productRepository.updateProduct(id, requestData);
  },
  
  // 재고 조정
  async adjustStock(id: number, adjustmentData: StockAdjustmentData): Promise<number> {
    const requestData = mapToStockAdjustmentRequest(adjustmentData);
    return await productRepository.adjustStock(id, requestData);
  },
  
  // 상품 삭제
  async deleteProduct(id: number): Promise<void> {
    await productRepository.deleteProduct(id);
  }
};
