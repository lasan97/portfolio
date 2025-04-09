// vue 컴포넌트를 위한 SSR 관련 타입 확장
import { RouteLocationNormalizedLoaded } from 'vue-router';
import { Pinia } from 'pinia';

declare global {
  interface Window {
    __INITIAL_STATE__?: any;
  }
}

export interface SSRContext {
  route: RouteLocationNormalizedLoaded;
  store: Pinia;
}

declare module 'vue' {
  interface ComponentCustomOptions {
    ssrPrefetch?: (context: SSRContext) => Promise<any>;
  }
}
