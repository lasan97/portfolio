import { RouteRecordRaw } from 'vue-router';

import { LoginPage, OAuthCallbackPage } from '@pages/auth'
import { NotFoundPage } from '@pages/notFound'
import {SsrPage} from "@pages/ssr";
import {ProductDetailPage, ProductManagementPage, ProductsPage} from "@pages/products";
import {
  OrderPage,
  OrderHistoryPage,
  OrderDetailPage,
  OrderCompletePage
} from "@pages/order";
import { CartPage } from '@entities/cart';


// 인증 관련 routeer
const authRouteConfig: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'login',
        component: LoginPage
    },
    {
        path: '/oauth2/callback',
        name: 'oauth-callback',
        component: OAuthCallbackPage
    },
];

// error 관련 routeer
const errorRouteConfig: RouteRecordRaw[] = [
    {
        path: '/404',
        name: 'notFound',
        component: NotFoundPage
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/404'
    },
];

// 샘플 관련 routeer
const sampleRouteConfig: RouteRecordRaw[] = [
    {
        path: '/ssr',
        name: 'ssr',
        component: SsrPage,
        meta: { ssr: true } // SSR로 처리할 페이지
    },
];

// 주문 관련 router
const orderRouteConfig: RouteRecordRaw[] = [
    {
        path: '/products',
        name: 'products',
        component: ProductsPage
    },
    {
        path: '/products/:id',
        name: 'product-detail',
        component: ProductDetailPage
    },
    {
        path: '/products/manage',
        name: 'product-management',
        component: ProductManagementPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/cart',
        name: 'cart',
        component: CartPage
    },
    {
        path: '/order',
        name: 'order',
        component: OrderPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/order-complete',
        name: 'order-complete',
        component: OrderCompletePage,
        meta: { requiresAuth: true }
    },
    {
        path: '/orders',
        name: 'order-history',
        component: OrderHistoryPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/order/:id',
        name: 'order-detail',
        component: OrderDetailPage,
        meta: { ssr: true, requiresAuth: true}
    }
];

export {
    authRouteConfig,
    errorRouteConfig,
    sampleRouteConfig,
    orderRouteConfig
};