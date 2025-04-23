import { apiInstance } from '@shared/api';
import { CartItem } from '../model/types';
import { CartRequest, CartResponse } from './types';
import { mapToCartItems } from './mappers';

export const cartRepository = {
  /**
   * 장바구니 아이템 목록 조회
   */
  async getCartItems(): Promise<CartItem[]> {
    const response = await apiInstance.get<CartResponse.Item[]>('/api/cart');
    return mapToCartItems(response.data);
  },

  /**
   * 장바구니에 상품 추가
   */
  async addCartItem(productId: number, quantity: number): Promise<void> {
    const request: CartRequest.AddItem = {
      productId,
      quantity
    };
    
    await apiInstance.post('/api/cart', request);
  },

  /**
   * 장바구니에서 상품 제거
   */
  async removeCartItem(productId: number): Promise<void> {
    const request: CartRequest.RemoveItem = {
      productId
    };
    
    await apiInstance.delete('/api/cart', { data: request });
  }
};
