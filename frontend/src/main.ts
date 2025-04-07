// process polyfill 먼저 가져오기
import './shared/shims/process'

import { createApp } from 'vue'
import App from './app/App.vue'
import router from './app/router'
import store from './app/store'

// Tailwind CSS 가져오기
import './app/styles/main.css'

createApp(App)
  .use(store)
  .use(router)
  .mount('#app')