import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import HomePage from '@/pages/home'
import { LoginForm } from '@/features/auth/login'
import { RegisterForm } from '@/features/auth/register'
import { ROUTES } from '@/shared/config'

const routes: Array<RouteRecordRaw> = [
  {
    path: ROUTES.HOME,
    name: 'home',
    component: HomePage
  },
  {
    path: ROUTES.LOGIN,
    name: 'login',
    component: LoginForm
  },
  {
    path: ROUTES.REGISTER,
    name: 'register',
    component: RegisterForm
  },
  {
    path: ROUTES.NOT_FOUND,
    name: 'notFound',
    component: () => import('@/pages/notFound/ui/NotFoundPage.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: ROUTES.NOT_FOUND
  }
]

const router = createRouter({
  // Vite에서는 import.meta.env를 사용합니다
  history: createWebHistory(import.meta.env.BASE_URL as string || '/'),
  routes
})

export default router
