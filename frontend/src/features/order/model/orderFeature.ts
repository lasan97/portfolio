import { usePersistedOrderStore, DeliveryInfo, Order } from '@entities/order';
import { useAuthStore } from '@features/auth';
import { useUserStore } from '@entities/user';
import { useCartStore } from '@entities/cart';
import { ToastService } from '@shared/ui/toast';
import { useRouter, Router } from 'vue-router';

// 인증이 필요한 주문 기능을 위한 컴포지션 함수
export function useOrderWithAuth() {
  const orderStore = usePersistedOrderStore();
  const cartStore = useCartStore();
  const authStore = useAuthStore();
  const userStore = useUserStore();
  let router: Router | null = null;

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

  // 인증 체크 및 오류 처리를 위한 고차 함수
  const withAuthCheck = async <T>(
    operation: () => Promise<T>,
    errorMessage: string = '오류가 발생했습니다.'
  ): Promise<T | null | false> => {
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

      // 작업 수행
      return await operation();
    } catch (error: any) {
      ToastService.error(error.message || errorMessage, {
        position: 'top-center',
        duration: 3000
      });

      return null;
    }
  };

  // 주문 생성 (인증 체크 포함)
  const createOrder = async (deliveryInfo: DeliveryInfo): Promise<boolean | null | false> => {
    return withAuthCheck(async () => {
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
    }, '주문 처리 중 오류가 발생했습니다.');
  };

  // 주문 목록 조회 (인증 체크 포함)
  const fetchOrders = async (page: number = 0, size: number = 10): Promise<any | null | false> => {
    return withAuthCheck(async () => {
      // 항상 API에서 최신 데이터 조회
      console.log('API에서 주문 목록을 가져옵니다');
      await orderStore.fetchOrders(page, size);
      return true;
    }, '주문 목록 조회 중 오류가 발생했습니다.');
  };

  // 주문 상세 조회 (인증 체크 포함)
  const fetchOrderDetails = async (orderId: string): Promise<Order | null | false> => {
    return withAuthCheck(async () => {
      // 항상 API에서 최신 데이터 조회
      console.log('API에서 주문 데이터를 가져옵니다:', orderId);
      return await orderStore.fetchOrderDetails(orderId);
    }, '주문 상세 조회 중 오류가 발생했습니다.');
  };

  // 주문 취소 (인증 체크 포함)
  const cancelOrder = async (orderId: string): Promise<boolean | null | false> => {
    return withAuthCheck(async () => {
      console.log('주문을 취소합니다:', orderId);
      return await orderStore.cancelOrder(orderId);
    }, '주문 취소 중 오류가 발생했습니다.');
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
    cancelOrder,

    // 인증 상태
    isLoggedIn: checkAuth
  };
}
