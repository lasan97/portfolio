import { createApp } from 'vue';
import { App, router, pinia } from './app';
import './shared/assets/styles/main.css';

createApp(App)
  .use(router)
  .use(pinia)
  .mount('#app');
