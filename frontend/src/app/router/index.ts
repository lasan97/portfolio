import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { HomePage } from '@pages/home'
import { LoginPage, OAuthCallbackPage } from '@pages/auth'
import { ProfilePage } from '@pages/profile'
import { ROUTES } from '@shared/config'

const routes: Array<RouteRecordRaw> = [
  {
    path: ROUTES.HOME,
    name: 'home',
    component: HomePage
  },
  {
    path: ROUTES.LOGIN,
    name: 'login',
    component: LoginPage
  },
  {
    path: ROUTES.OAUTH_CALLBACK,
    name: 'oauth-callback',
    component: OAuthCallbackPage
  },
  {
    path: ROUTES.PROFILE,
    name: 'profile',
    component: ProfilePage,
    meta: { requiresAuth: true }
  },
  {
    path: ROUTES.NOT_FOUND,
    name: 'notFound',
    component: () => import('@pages/notFound/ui/NotFoundPage.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: ROUTES.NOT_FOUND
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL as string || '/'),
  routes
})

// 인증 미들웨어
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      next({ name: 'login' });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router
