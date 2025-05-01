import { createRouter, createWebHistory, createMemoryHistory, RouteRecordRaw } from 'vue-router'
import { HomePage } from '@pages/home'
import { ProfilePage } from '@pages/profile'
import { IntroductionPage } from '@pages/introduction'

import { authRouteConfig, errorRouteConfig, sampleRouteConfig, orderRouteConfig } from './routeConfig';

// 인증 관련 유틸리티 import (통합된 auth-utils 사용)
import {storeToRefs} from 'pinia';
import {useAuthStore} from '@features/auth';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: HomePage
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfilePage,
    meta: { requiresAuth: true }
  },
  {
    path: '/introduction',
    name: 'introduction',
    component: IntroductionPage,
    meta: { ssr: true }
  },
  ...authRouteConfig,
  ...errorRouteConfig,
  ...sampleRouteConfig,
  ...orderRouteConfig
]

// SSR을 위한 라우터 히스토리 분기 처리
const isServer = typeof window === 'undefined'
const history = isServer
  ? createMemoryHistory()
  : createWebHistory(import.meta.env.BASE_URL as string || '/')

const router = createRouter({
  history,
  routes
})

// 인증 미들웨어 (서버와 클라이언트 모두 동작)
router.beforeEach(async (to, from, next) => {
  // 클라이언트 환경에서만 실행되는 코드
  if (typeof window !== 'undefined') {
    // 인증 상태를 실시간으로 확인
    const authStore = useAuthStore();
    const { isAuthenticated } = storeToRefs(authStore);
    
    // 인증이 필요한 페이지인지 확인
    if (to.matched.some(record => record.meta.requiresAuth)) {
      if (!isAuthenticated.value) {
        // 비인증 상태에서 인증이 필요한 페이지 접근 시 로그인 페이지로 리다이렉트
        next({ name: 'login', query: { returnUrl: to.fullPath } });
        return;
      }
    } else if (to.name === 'login' && isAuthenticated.value) {
      // 이미 로그인된 상태에서 로그인 페이지 접근 시 홈으로 리다이렉트
      next({ name: 'home' });
      return;
    }
  }
  
  // 다른 모든 경우에는 정상적으로 페이지 이동
  next();
});

export default router