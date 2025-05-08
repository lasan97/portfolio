import { useOrderStore, DeliveryInfo } from '@entities/order';
import { useAuthStore } from '@features/auth';
import { useCartStore } from '@entities/cart';
import { ToastService } from '@shared/ui/toast';
import { useRouter } from 'vue-router';

// 인증이 필요한 주문 기능을 위한 컴포지션 함수
export function useOrderWithAuth() {
  const orderStore = useOrderStore();
  const cartStore = useCartStore();
  const authStore = useAuthStore();
  let router: any = null;
  
  try {
    // SSR 환경에서는 오류가 발생할 수 있으므로 try-catch로 감싸기
    if (typeof window !== 'undefined') {
      router = useRouter();
    }
  } catch (e) {
    // 라우터를 사용할 수 없는 경우 조용히 무시
  }

  // 인증 상태 확인 메서드
  const checkAuth = () => {
    return authStore.isAuthenticated;
  };

  // 주문 생성 (인증 체크 포함)
  const createOrder = async (deliveryInfo: DeliveryInfo) => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        
        // 로그인 페이지로 이동
        if (router) {
          const returnUrl = encodeURIComponent(window.location.pathname);
          router.push(`/login?returnUrl=${returnUrl}`);
        }
        
        return false;
      }
      
      // 장바구니가 비어있는지 확인
      if (cartStore.isEmpty) {
        ToastService.warning('장바구니가 비어있습니다.', {
          position: 'top-center',
          duration: 3000
        });
        return false;
      }
      
      // 주문 생성
      await orderStore.createOrder(deliveryInfo, cartStore.total);
      
      // 장바구니 비우기
      await cartStore.clearCart();
      
      // 주문 완료 페이지로 이동
      if (router) {
        // 가장 최근 생성된 주문의 ID 가져오기
        const latestOrder = orderStore.sortedOrders[0];
        if (latestOrder) {
          router.push(`/order-complete?orderId=${latestOrder.id}`);
        } else {
          router.push('/order-complete');
        }
      }
      
      return true;
    } catch (error: any) {
      ToastService.error(error.message || '주문 처리 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      
      return false;
    }
  };
  
  // 주문 목록 조회 (인증 체크 포함)
  const fetchOrders = async (page: number = 0, size: number = 10) => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        
        // 로그인 페이지로 이동
        if (router) {
          const returnUrl = encodeURIComponent(window.location.pathname);
          router.push(`/login?returnUrl=${returnUrl}`);
        }
        
        return false;
      }
      
      // 스토어에 이미 주문 목록이 있는지 확인
      if (orderStore.hasOrders) {
        console.log('스토어에 이미 주문 목록이 있습니다:', orderStore.orders.length, '개');
        return true;
      }
      
      // localStorage에서 캐시된 주문 목록이 있는지 확인
      const cachedOrdersJson = localStorage.getItem('cached_orders');
      const userId = authStore.userData?.id;
      
      // 캐시된 데이터가 현재 로그인한 사용자의 것인지 확인하기 위해 사용자 ID도 함께 저장
      const cachedUserId = localStorage.getItem('cached_orders_user');
      
      if (cachedOrdersJson && cachedUserId === userId) {
        try {
          const cachedOrders = JSON.parse(cachedOrdersJson);
          // 캐시된 주문 데이터가 있으면 스토어에 설정
          if (cachedOrders && Array.isArray(cachedOrders) && cachedOrders.length > 0) {
            orderStore.orders = cachedOrders;
            console.log('캐시된 주문 목록을 사용합니다:', cachedOrders.length, '개');
            return true;
          }
        } catch (e) {
          console.error('캐시된 주문 목록 파싱 중 오류 발생:', e);
          localStorage.removeItem('cached_orders');
          localStorage.removeItem('cached_orders_user');
        }
      } else if (cachedUserId !== userId) {
        // 다른 사용자의 캐시가 있으면 삭제
        localStorage.removeItem('cached_orders');
        localStorage.removeItem('cached_orders_user');
      }
      
      // 캐시에 없으면 API에서 조회
      console.log('API에서 주문 목록을 가져옵니다');
      await orderStore.fetchOrders(page, size);
      
      // 결과를 localStorage에 캐싱
      if (orderStore.orders.length > 0) {
        localStorage.setItem('cached_orders', JSON.stringify(orderStore.orders));
        localStorage.setItem('cached_orders_user', userId);
      }
      
      return true;
    } catch (error: any) {
      ToastService.error(error.message || '주문 목록 조회 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      
      return false;
    }
  };
  
  // 주문 상세 조회 (인증 체크 포함)
  const fetchOrderDetails = async (orderId: string) => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        
        // 로그인 페이지로 이동
        if (router) {
          const returnUrl = encodeURIComponent(window.location.pathname);
          router.push(`/login?returnUrl=${returnUrl}`);
        }
        
        return null;
      }
      
      // 스토어에 이미 현재 주문이 있는지 확인
      if (orderStore.currentOrder && orderStore.currentOrder.id === orderId) {
        console.log('스토어에 이미 해당 주문이 있습니다:', orderId);
        return orderStore.currentOrder;
      }
      
      // 스토어의 orders 배열에서 먼저 찾기
      const orderInStore = orderStore.orders.find((order) => order.id === orderId);
      if (orderInStore) {
        orderStore.currentOrder = orderInStore;
        console.log('스토어의 주문 목록에서 주문을 찾았습니다:', orderId);
        return orderInStore;
      }
      
      // localStorage에서 캐시된 주문이 있는지 확인
      const cachedOrdersJson = localStorage.getItem('cached_orders');
      const userId = authStore.userData?.id;
      const cachedUserId = localStorage.getItem('cached_orders_user');
      
      if (cachedOrdersJson && cachedUserId === userId) {
        try {
          const cachedOrders = JSON.parse(cachedOrdersJson);
          // 캐시된 주문 중에서 현재 주문 ID와 일치하는 것이 있으면 사용
          const cachedOrder = cachedOrders.find((order: any) => order.id === orderId);
          
          if (cachedOrder) {
            // 찾은 캐시 데이터를 스토어에 설정
            if (!orderStore.hasOrders) {
              orderStore.orders = cachedOrders;
            }
            orderStore.currentOrder = cachedOrder;
            console.log('캐시된 주문 데이터를 사용합니다:', cachedOrder.id);
            return cachedOrder;
          }
        } catch (e) {
          console.error('캐시된 주문 데이터 파싱 중 오류 발생:', e);
          // 캐시 파싱 에러 시 로컬스토리지 비우기
          localStorage.removeItem('cached_orders');
          localStorage.removeItem('cached_orders_user');
        }
      } else if (cachedUserId !== userId) {
        // 다른 사용자의 캐시가 있으면 삭제
        localStorage.removeItem('cached_orders');
        localStorage.removeItem('cached_orders_user');
      }
      
      // 캐시에 없으면 API에서 조회
      console.log('API에서 주문 데이터를 가져옵니다:', orderId);
      const order = await orderStore.fetchOrderDetails(orderId);
      
      // 결과를 localStorage에 캐싱
      if (order) {
        const ordersToCache = orderStore.orders.length > 0 ? orderStore.orders : [order];
        localStorage.setItem('cached_orders', JSON.stringify(ordersToCache));
        localStorage.setItem('cached_orders_user', userId);
      }
      
      return order;
    } catch (error: any) {
      ToastService.error(error.message || '주문 상세 조회 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      
      return null;
    }
  };

  return {
    // 주문 스토어에서 가져온 정보
    orders: orderStore.orders,
    currentOrder: orderStore.currentOrder,
    sortedOrders: orderStore.sortedOrders,
    hasOrders: orderStore.hasOrders,
    loading: orderStore.loading,
    error: orderStore.error,
    
    // 인증 체크가 포함된 메서드들
    createOrder,
    fetchOrders,
    fetchOrderDetails,
    
    // 인증 상태
    isLoggedIn: checkAuth
  };
}