import { useCartStore } from '@entities/cart';
import { useAuthStore } from '@features/auth';
import { ToastService } from '@shared/ui/toast';
import { Product } from '@entities/product';
import { useRouter } from 'vue-router';

// 인증이 필요한 장바구니 기능을 위한 컴포지션 함수
export function useCartWithAuth() {
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

  // 장바구니에 상품 추가 (인증 체크 포함)
  const addToCart = async (product: Product, quantity: number = 1) => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        // 비로그인 상태일 때 토스트 메시지 표시
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        
        // 로그인 페이지로 이동 (옵션)
        if (router) {
          // 현재 URL을 저장하여 로그인 후 돌아올 수 있도록 함
          const returnUrl = encodeURIComponent(window.location.pathname);
          router.push(`/login?returnUrl=${returnUrl}`);
        }
        
        return false;
      }
      
      // 인증된 사용자는 장바구니에 추가
      await cartStore.addToCart(product, quantity);
      
      // 성공 메시지 표시
      ToastService.success('장바구니에 상품이 추가되었습니다.', {
        position: 'top-center',
        duration: 2000
      });
      
      return true;
    } catch (error: any) {
      // 오류 메시지 표시
      ToastService.error(error.message || '장바구니 추가 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      
      return false;
    }
  };
  
  // 장바구니에서 상품 제거 (인증 체크 포함)
  const removeFromCart = async (productId: number) => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        return false;
      }
      
      await cartStore.removeFromCart(productId);
      return true;
    } catch (error: any) {
      ToastService.error(error.message || '상품 제거 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      return false;
    }
  };
  
  // 장바구니 상품 수량 변경 (인증 체크 포함)
  const updateQuantity = async (productId: number, quantity: number) => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        return false;
      }
      
      await cartStore.updateQuantity(productId, quantity);
      return true;
    } catch (error: any) {
      ToastService.error(error.message || '수량 변경 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      return false;
    }
  };
  
  // 장바구니 비우기 (인증 체크 포함)
  const clearCart = async () => {
    try {
      // 인증 상태 확인
      if (!checkAuth()) {
        ToastService.warning('로그인 후 이용해주세요.', {
          position: 'top-center',
          duration: 3000
        });
        return false;
      }
      
      await cartStore.clearCart();
      return true;
    } catch (error: any) {
      ToastService.error(error.message || '장바구니 비우기 중 오류가 발생했습니다.', {
        position: 'top-center',
        duration: 3000
      });
      return false;
    }
  };
  
  // 장바구니 초기화 (앱 시작 시)
  const initializeCart = async () => {
    if (checkAuth()) {
      await cartStore.initializeCart();
    }
  };

  return {
    // 원래 cartStore에서 가져온 getter들
    items: cartStore.items,
    totalItems: cartStore.totalItems,
    originalSubtotal: cartStore.originalSubtotal,
    subtotal: cartStore.subtotal,
    totalDiscount: cartStore.totalDiscount,
    discountRate: cartStore.discountRate,
    shippingInfo: cartStore.shippingInfo,
    total: cartStore.total,
    isEmpty: cartStore.isEmpty,
    getItemById: cartStore.getItemById,
    
    // 인증 체크가 포함된 메서드들
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    initializeCart,
    
    // 인증 상태
    isLoggedIn: checkAuth
  };
}
