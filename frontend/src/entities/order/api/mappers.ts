import { Order, OrderStatus } from '../model/types';
import { OrderResponse } from './types';

/**
 * API 응답을 도메인 모델로 매핑
 */
export const mapToOrder = (response: OrderResponse.Get): Order => {
  return {
    id: response.id,
    orderStatus: response.orderStatus,
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
    totalPrice: response.totalPrice,
    orderItems: response.orderItems.map(item => ({
      product: {
        id: item.product.id,
        name: item.product.name,
        originalPrice: item.product.originalPrice,
        price: item.product.price,
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
