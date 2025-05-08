// 주문 상태 열거형
export enum OrderStatus {
  PENDING = 'PENDING',               // 대기
  PAID = 'PAID',                     // 결제완료
  FAILED = 'FAILED',                 // 주문 실패
  CANCELLED = 'CANCELLED',           // 주문 취소
  CANCELING = 'CANCELING',           // 주문 취소 요청
  ORDERED = 'ORDERED'                // 주문 완료
}

// OrderStatus Namespace
export namespace OrderStatus {
  // 설명 매핑
  export const descriptions: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: '대기',
    [OrderStatus.PAID]: '결제완료',
    [OrderStatus.FAILED]: '주문 실패',
    [OrderStatus.CANCELLED]: '주문 취소',
    [OrderStatus.CANCELING]: '주문 취소 요청',
    [OrderStatus.ORDERED]: '주문 완료'
  };

  // 상태별 스타일 클래스 매핑
  export const statusClasses: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: 'bg-blue-100 text-blue-800',
    [OrderStatus.PAID]: 'bg-green-100 text-green-800',
    [OrderStatus.FAILED]: 'bg-red-100 text-red-800',
    [OrderStatus.CANCELLED]: 'bg-red-100 text-red-800',
    [OrderStatus.CANCELING]: 'bg-orange-100 text-orange-800',
    [OrderStatus.ORDERED]: 'bg-purple-100 text-purple-800'
  };

  // getDescription 메서드
  export function getDescription(status: OrderStatus): string {
    return descriptions[status] || status;
  }

  // getStatusClass 메서드
  export function getStatusClass(status: OrderStatus): string {
    return statusClasses[status] || 'bg-gray-100 text-gray-800';
  }

  // entries 메서드 - key-value 쌍 배열 반환
  export function entries(): Array<{ code: OrderStatus; description: string }> {
    return Object.values(OrderStatus)
      .filter(value => typeof value === 'string') // enum에 추가되는 숫자 값 필터링
      .map(code => ({
        code: code as OrderStatus,
        description: descriptions[code as OrderStatus] || code as string
      }));
  }

  // values 메서드 - 모든 enum 값 반환
  export function values(): OrderStatus[] {
    return Object.values(OrderStatus)
      .filter(value => typeof value === 'string') as OrderStatus[];
  }
}

// 주문 상품 정보
export interface OrderProduct {
  id: number;
  name: string;
  originalPrice: number;
  price: number;
  thumbnailImageUrl?: string;
}

// 주문 아이템
export interface OrderItem {
  product: OrderProduct;
  quantity: number;
}

// 배송 정보
export interface DeliveryInfo {
  name: string;
  phone: string;
  deliveryRequest?: string;
  address: {
    address: string;
    detailAddress?: string;
    postCode: string;
  };
}

// 주문 정보
export interface Order {
  id: string;
  orderStatus: OrderStatus;
  deliveryInfo: DeliveryInfo;
  totalPrice: number;
  orderItems: OrderItem[];
  createdAt: string;
}

// 주문 스토어 상태
export interface OrderState {
  orders: Order[];
  currentOrder: Order | null;
  loading: boolean;
  error: string | null;
}
