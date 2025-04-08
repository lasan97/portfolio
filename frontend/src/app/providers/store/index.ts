import { createStore } from 'vuex';
import { authModule } from '@/features/auth';
import { userModule } from '@/entities/user';

export default createStore({
  state: {
    // 전역 상태
  },
  mutations: {
    // 전역 뮤테이션
  },
  actions: {
    // 전역 액션
  },
  modules: {
    auth: authModule,
    user: userModule
  }
});
