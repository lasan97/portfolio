import { defineStore } from 'pinia';
import { Order, OrderState } from './types';
import { orderRepository } from '../api';
import { useAuthStore } from '@features/auth';

export const useOrderStore = defineStore('order', {
  state: (): OrderState => ({
    orders: [],
    currentOrder: null,
    loading: false,
    error: null
  }),

  getters: {
    // 주문 목록 정렬 (최신순)
    sortedOrders: (state): Order[] => {
      return [...state.orders].sort((a, b) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
    },

    // 주문이 있는지 여부
    hasOrders: (state): boolean => {
      return state.orders.length > 0;
    },

    // 주문 상세 정보
    orderDetails: (state) => (orderId: string): Order | undefined => {
      return state.orders.find(order => order.id === orderId);
    }
  },

  actions: {
    // 주문 생성
    async createOrder(deliveryInfo: Order['deliveryInfo'], totalPrice: number) {
      try {
        this.loading = true;
        this.error = null;

        // API 요청
        await orderRepository.createOrder({
          totalPrice: totalPrice,
          deliveryInfo: {
            name: deliveryInfo.name,
            phone: deliveryInfo.phone,
            address: {
              address: deliveryInfo.address.address,
              detailAddress: deliveryInfo.address.detailAddress,
              postCode: deliveryInfo.address.postCode
            },
            deliveryRequest: deliveryInfo.deliveryRequest
          }
        });

        // 주문 생성 후 주문 목록 갱신
        await this.fetchOrders();
        
        return true;
      } catch (error: any) {
        console.error('주문 생성 중 오류 발생:', error);
        this.error = error.message || '주문 생성 중 오류가 발생했습니다.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 주문 목록 조회
    async fetchOrders(page: number = 0, size: number = 10) {
      try {
        const authStore = useAuthStore();
        if (!authStore.isAuthenticated) {
          throw new Error('로그인이 필요합니다.');
        }

        this.loading = true;
        this.error = null;

        // API 요청
        const response = await orderRepository.getOrderPage(page, size);
        this.orders = response.content;
        
        return response;
      } catch (error: any) {
        console.error('주문 목록 조회 중 오류 발생:', error);
        this.error = error.message || '주문 목록 조회 중 오류가 발생했습니다.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 주문 상세 조회
    async fetchOrderDetails(orderId: string) {
      try {
        const authStore = useAuthStore();
        if (!authStore.isAuthenticated) {
          throw new Error('로그인이 필요합니다.');
        }

        this.loading = true;
        this.error = null;

        // 이미 로드된 주문인지 확인
        const existingOrder = this.orders.find(order => order.id === orderId);
        if (existingOrder) {
          this.currentOrder = existingOrder;
          return existingOrder;
        }

        // API 요청
        const order = await orderRepository.getOrder(orderId);
        this.currentOrder = order;
        
        // 주문 목록에 없는 경우 추가
        if (!this.orders.some(o => o.id === order.id)) {
          this.orders.push(order);
        }
        
        return order;
      } catch (error: any) {
        console.error('주문 상세 조회 중 오류 발생:', error);
        this.error = error.message || '주문 상세 조회 중 오류가 발생했습니다.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 주문 초기화 (스토어 초기화)
    clearOrders() {
      this.orders = [];
      this.currentOrder = null;
      this.error = null;
    }
  }
});
