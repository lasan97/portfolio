import { apiInstance } from '@shared/api';
import { Order } from '../model/types';
import { OrderRequest, OrderResponse } from './types';
import { mapToOrder, mapToOrders } from './mappers';

export const orderRepository = {
  /**
   * 주문 생성
   */
  async createOrder(request: OrderRequest.Create): Promise<void> {
    // 요청 데이터 로깅 (디버깅용)
    console.log('주문 요청 데이터:', JSON.stringify(request, null, 2));
    
    // API 호출
    await apiInstance.post('/api/orders', request);
  },

  /**
   * 주문 취소
   */
  async cancelOrder(orderId: string): Promise<void> {
    await apiInstance.post(`/api/orders/cancel/${orderId}`);
  },

  /**
   * 주문 목록 조회 (페이징)
   */
  async getOrderPage(page: number = 0, size: number = 10): Promise<{
    content: Order[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    const response = await apiInstance.get<{
      content: OrderResponse.Get[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    }>(`/api/orders/page?page=${page}&size=${size}`);

    return {
      content: mapToOrders(response.data.content),
      totalElements: response.data.totalElements,
      totalPages: response.data.totalPages,
      size: response.data.size,
      number: response.data.number
    };
  },

  /**
   * 주문 상세 조회
   */
  async getOrder(orderId: string): Promise<Order> {
    const response = await apiInstance.get<OrderResponse.Get>(`/api/orders/${orderId}`);
    return mapToOrder(response.data);
  }
};
