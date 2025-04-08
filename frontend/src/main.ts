import { createApp } from 'vue';
import { App, router, store } from './app';
import './shared/assets/styles/main.css';

createApp(App)
  .use(router)
  .use(store)
  .mount('#app');
