import { ProductStatus } from '@entities/product';

// API 요청 타입
export namespace CartRequest {
  export interface AddItem {
    productId: number;
    quantity: number;
  }

  export interface RemoveItem {
    productId: number;
  }
}

// API 응답 타입
export namespace CartResponse {
  export interface Item {
    product: {
      id: number;
      name: string;
      price: number;
      originalPrice: number;
      thumbnailImageUrl: string;
      status: string;
    };
    quantity: number;
  }
}
