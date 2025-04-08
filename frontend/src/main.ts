import { createApp } from 'vue'
import App from './app/App.vue'
import router from './app/router'
import store from './app/store'

// Tailwind CSS 가져오기
import './app/styles/main.css'

// 앱 생성 및 마운트
const app = createApp(App)

// 플러그인 사용
app.use(store)
app.use(router)

// 앱 마운트
app.mount('#app')