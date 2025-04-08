import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { HomePage } from '@/pages/home';
import { LoginPage, OAuthCallbackPage } from '@/pages/auth';
import { ProfilePage } from '@/pages/profile';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage
  },
  {
    path: '/oauth2/callback',
    name: 'OAuthCallback',
    component: OAuthCallbackPage
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfilePage,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      next({ name: 'Login' });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;
