import { createApp, h, ref, reactive } from 'vue';
import ToastNotification from './ToastNotification.vue';

export interface ToastOptions {
  message: string;
  type?: 'success' | 'error' | 'warning' | 'info';
  position?: 'top-left' | 'top-center' | 'top-right' | 'bottom-left' | 'bottom-center' | 'bottom-right';
  duration?: number;
  showClose?: boolean;
  autoClose?: boolean;
}

export interface ToastInstance {
  id: number;
  close: () => void;
}

// 전역 toast ID 카운터
let toastIdCounter = 0;

// 활성 토스트 컨테이너
const activeToasts = reactive<Record<number, ToastInstance>>({});

// 토스트 생성 함수
function createToast(options: ToastOptions): ToastInstance {
  const id = ++toastIdCounter;
  
  // 컨테이너 요소 생성
  const container = document.createElement('div');
  container.setAttribute('id', `toast-${id}`);
  document.body.appendChild(container);
  
  // 컴포넌트 마운트
  const app = createApp({
    setup() {
      const onClose = () => {
        // 토스트 닫힐 때 처리
        app.unmount();
        container.remove();
        delete activeToasts[id];
      };
      
      return () => h(ToastNotification, {
        ...options,
        onClose,
      });
    }
  });
  
  app.mount(container);
  
  // 토스트 인스턴스 생성
  const toastInstance = {
    id,
    close: () => {
      app.unmount();
      container.remove();
      delete activeToasts[id];
    }
  };
  
  // 활성 토스트 목록에 추가
  activeToasts[id] = toastInstance;
  
  return toastInstance;
}

export const ToastService = {
  // 기본 토스트 생성
  show(options: ToastOptions | string): ToastInstance {
    const normalizedOptions = typeof options === 'string' 
      ? { message: options } 
      : options;
    
    return createToast(normalizedOptions);
  },
  
  // 성공 토스트
  success(message: string, options?: Omit<ToastOptions, 'message' | 'type'>): ToastInstance {
    return createToast({
      message,
      type: 'success',
      ...options
    });
  },
  
  // 오류 토스트
  error(message: string, options?: Omit<ToastOptions, 'message' | 'type'>): ToastInstance {
    return createToast({
      message,
      type: 'error',
      ...options
    });
  },
  
  // 경고 토스트
  warning(message: string, options?: Omit<ToastOptions, 'message' | 'type'>): ToastInstance {
    return createToast({
      message,
      type: 'warning',
      ...options
    });
  },
  
  // 정보 토스트
  info(message: string, options?: Omit<ToastOptions, 'message' | 'type'>): ToastInstance {
    return createToast({
      message,
      type: 'info',
      ...options
    });
  },
  
  // 모든 토스트 닫기
  closeAll(): void {
    Object.values(activeToasts).forEach(toast => toast.close());
  }
};

export default ToastService;
