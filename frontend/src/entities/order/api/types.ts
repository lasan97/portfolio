// API 요청 타입
export namespace OrderRequest {
  // 주문 생성 요청
  export interface Create {
    totalPrice:  number;
    deliveryInfo: {
      name: string;
      phone: string;
      address: {
        address: string;
        detailAddress?: string;
        postCode: string;
      };
      deliveryRequest?: string;
    };
  }
}

// API 응답 타입
export namespace OrderResponse {
  // 주문 응답
  export interface Get {
    id: string;
    orderStatus: string;
    deliveryInfo: {
      name: string;
      phone: string;
      deliveryRequest?: string;
      address: {
        address: string;
        detailAddress?: string;
        postCode: string;
      };
    };
    totalPrice: number;
    orderItems: OrderItem[];
    createdAt: string;
  }
  
  // 주문 상품 정보
  export interface OrderItem {
    product: {
      id: number;
      name: string;
      originalPrice: number;
      price: number;
      thumbnailImageUrl?: string;
    };
    quantity: number;
  }
}
