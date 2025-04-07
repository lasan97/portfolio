import { createStore } from 'vuex'

// 빈 인터페이스 대신 실제 상태 속성 정의
interface RootState {
  version: string;
}

export default createStore<RootState>({
  state: {
    version: '1.0.0'
  },
  getters: {
    // Getters
  },
  mutations: {
    // Mutations
  },
  actions: {
    // Actions
  },
  modules: {
    // Modules
  }
})
