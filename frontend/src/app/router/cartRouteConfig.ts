import { CartPage } from '@entities/cart';
import { RouteRecordRaw } from 'vue-router';

// 장바구니 관련 router
const cartRouteConfig: RouteRecordRaw[] = [
  {
    path: '/cart',
    name: 'cart',
    component: CartPage
  }
];

export {
  cartRouteConfig
};
