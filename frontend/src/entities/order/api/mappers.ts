import { Order, OrderStatus } from '../model/types';
import { OrderResponse } from './types';

/**
 * API 응답을 도메인 모델로 매핑
 */
export const mapToOrder = (response: OrderResponse.Get): Order => {
  return {
    id: response.id,
    orderStatus: response.orderStatus as OrderStatus,
    deliveryInfo: {
      name: response.deliveryInfo.name,
      phone: response.deliveryInfo.phone,
      deliveryRequest: response.deliveryInfo.deliveryRequest,
      address: {
        address: response.deliveryInfo.address.address,
        detailAddress: response.deliveryInfo.address.detailAddress,
        postCode: response.deliveryInfo.address.postCode
      }
    },
    // 직접 숫자로 설정하여 중첩된 객체(price.amount) 구조 제거
    totalPrice: typeof response.totalPrice === 'number' 
      ? response.totalPrice
      : (response.totalPrice && typeof response.totalPrice === 'object' && 'amount' in response.totalPrice 
          ? response.totalPrice.amount
          : 0),
          
    orderItems: response.orderItems.map(item => ({
      product: {
        id: item.product.id,
        name: item.product.name,
        // 원가 직접 숫자로 변환
        originalPrice: typeof item.product.originalPrice === 'number'
          ? item.product.originalPrice
          : (item.product.originalPrice && typeof item.product.originalPrice === 'object' && 'amount' in item.product.originalPrice
              ? item.product.originalPrice.amount
              : 0),
        // 판매가 직접 숫자로 변환
        price: typeof item.product.price === 'number'
          ? item.product.price
          : (item.product.price && typeof item.product.price === 'object' && 'amount' in item.product.price
              ? item.product.price.amount
              : 0),
        thumbnailImageUrl: item.product.thumbnailImageUrl
      },
      quantity: item.quantity
    })),
    createdAt: response.createdAt
  };
};

/**
 * API 응답 배열을 도메인 모델 배열로 매핑
 */
export const mapToOrders = (responses: OrderResponse.Get[]): Order[] => {
  return responses.map(mapToOrder);
};
