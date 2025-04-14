import { Product } from '@entities/product';

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface CartState {
  items: CartItem[];
}

export interface ShippingInfo {
  required: boolean;
  cost: number;
}
