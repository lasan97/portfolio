import { defineStore } from 'pinia';
import { Order, OrderState } from './types';
import { orderRepository } from '../api';
import { useAuthStore } from '@features/auth';

// 로컬 스토리지 키
const ORDERS_STORAGE_KEY = 'order-store-orders';

// 로컬 스토리지에서 주문 데이터 가져오기
const getOrdersFromStorage = (): Order[] => {
  if (typeof window === 'undefined') return []; // SSR 환경 대응

  try {
    const storedOrders = localStorage.getItem(ORDERS_STORAGE_KEY);
    return storedOrders ? JSON.parse(storedOrders) : [];
  } catch (error) {
    console.error('로컬 스토리지에서 주문 데이터를 가져오는 중 오류 발생:', error);
    return [];
  }
};

// 로컬 스토리지에 주문 데이터 저장
const saveOrdersToStorage = (orders: Order[]): void => {
  if (typeof window === 'undefined') return; // SSR 환경 대응

  try {
    localStorage.setItem(ORDERS_STORAGE_KEY, JSON.stringify(orders));
  } catch (error) {
    console.error('로컬 스토리지에 주문 데이터를 저장하는 중 오류 발생:', error);
  }
};

export const useOrderStore = defineStore('order', {
  state: (): OrderState => ({
    orders: getOrdersFromStorage(),
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
    async createOrder(deliveryInfo: Order['deliveryInfo'], totalPrice: number): Promise<boolean> {
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
    async fetchOrders(page: number = 0, size: number = 10): Promise<any> {
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

        // 로컬 스토리지에 저장
        saveOrdersToStorage(this.orders);

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
    async fetchOrderDetails(orderId: string): Promise<Order> {
      try {
        const authStore = useAuthStore();
        if (!authStore.isAuthenticated) {
          throw new Error('로그인이 필요합니다.');
        }

        this.loading = true;
        this.error = null;

        // API 요청
        const order = await orderRepository.getOrder(orderId);
        this.currentOrder = order;

        // 주문 목록에 없는 경우 추가 또는 업데이트
        const existingOrderIndex = this.orders.findIndex(o => o.id === order.id);
        if (existingOrderIndex === -1) {
          this.orders.push(order);
        } else {
          // 이미 존재하는 주문 정보 업데이트
          this.orders[existingOrderIndex] = order;
        }

        // 로컬 스토리지에 저장
        saveOrdersToStorage(this.orders);

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

      // 로컬 스토리지에서도 삭제
      if (typeof window !== 'undefined') {
        localStorage.removeItem(ORDERS_STORAGE_KEY);
      }
    }
  }
});

// 스토어를 사용하는 커스텀 훅 (자동 저장 기능 포함)
export function usePersistedOrderStore() {
  const store = useOrderStore();

  // 클라이언트 환경에서만 실행
  if (typeof window !== 'undefined') {
    // 상태 변경 감지 및 자동 저장 (컴포넌트가 마운트될 때 한 번만 설정)
    store.$subscribe((mutation, state) => {
      // orders 배열이 변경되었을 때만 저장
      saveOrdersToStorage(state.orders);
    });
  }

  return store;
}
