import { createApp } from 'vue'
import App from './app'  // FSD 아키텍처 경로로 수정
import router from './app/router'
import store from './app/store'

createApp(App)
  .use(store)
  .use(router)
  .mount('#app')
